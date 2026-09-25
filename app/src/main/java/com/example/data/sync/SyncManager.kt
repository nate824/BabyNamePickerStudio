package com.example.data.sync

import android.util.Log
import com.example.data.local.AppDao
import com.example.data.local.BabyNameEntity
import com.example.data.local.MatchEntity
import com.example.data.local.PendingOpEntity
import com.example.data.local.SwipeEntity
import com.example.data.remote.ApiClient
import com.example.data.remote.ApiException
import com.example.data.remote.MatchPatch
import com.example.data.remote.NewNameRequest
import com.example.data.remote.RemoteState
import com.example.data.remote.SwipeInput
import com.example.data.session.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException

enum class SyncStatus { OFFLINE, SYNCING, SYNCED }

@Serializable
private data class UnswipePayload(val nameId: String)

@Serializable
private data class MatchPatchPayload(val nameId: String, val rating: Int? = null, val notes: String? = null)

/**
 * Keeps the local Room mirror in step with the sync server.
 *
 * Local edits are written to Room immediately and queued as [PendingOpEntity]s; [sync] pushes the
 * queue in order, then pulls the couple's full state and replaces the local mirror with it.
 */
class SyncManager(
    private val dao: AppDao,
    private val api: ApiClient,
    private val session: SessionStore,
    private val scope: CoroutineScope
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    private val _status = MutableStateFlow(SyncStatus.OFFLINE)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    /** Fires after remote state has been applied locally. */
    private val _changes = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val changes: SharedFlow<Unit> = _changes.asSharedFlow()

    /** Matches that appeared through the partner's swipes (not already celebrated locally). */
    private val _newMatches = MutableSharedFlow<List<String>>(extraBufferCapacity = 8)
    val newMatches: SharedFlow<List<String>> = _newMatches.asSharedFlow()

    @Volatile private var socket: WebSocket? = null
    private var socketJob: Job? = null
    @Volatile private var socketOpen = false
    @Volatile private var foreground = false

    // --- queueing local changes ---

    suspend fun enqueueSwipe(nameId: String, liked: Boolean, ts: Long) =
        enqueue(PendingOpEntity.SWIPE, json.encodeToString(SwipeInput.serializer(), SwipeInput(nameId, liked, ts)))

    suspend fun enqueueUnswipe(nameId: String) =
        enqueue(PendingOpEntity.UNSWIPE, json.encodeToString(UnswipePayload.serializer(), UnswipePayload(nameId)))

    suspend fun enqueueAddName(name: NewNameRequest) =
        enqueue(PendingOpEntity.ADD_NAME, json.encodeToString(NewNameRequest.serializer(), name))

    suspend fun enqueueMatchPatch(nameId: String, rating: Int? = null, notes: String? = null) =
        enqueue(PendingOpEntity.MATCH_PATCH, json.encodeToString(MatchPatchPayload.serializer(), MatchPatchPayload(nameId, rating, notes)))

    suspend fun enqueueMatchDismiss(nameId: String) =
        enqueue(PendingOpEntity.MATCH_DISMISS, json.encodeToString(UnswipePayload.serializer(), UnswipePayload(nameId)))

    private suspend fun enqueue(type: String, payload: String) {
        dao.insertPendingOp(PendingOpEntity(type = type, payload = payload))
        requestSync()
    }

    fun markMatchesSeen(ids: Collection<String>) {
        session.seenMatchIds = session.seenMatchIds + ids
    }

    fun requestSync() {
        scope.launch { sync() }
    }

    /** Push queued changes, then pull and apply the server state. Returns true when fully in sync. */
    suspend fun sync(): Boolean = mutex.withLock {
        if (!session.session.value.isRegistered) return false
        _status.value = SyncStatus.SYNCING
        val ok = try {
            flushPendingOps() && run { applyState(api.state()); true }
        } catch (e: IOException) {
            Log.w(TAG, "sync failed: ${e.message}")
            false
        }
        _status.value = if (ok) SyncStatus.SYNCED else SyncStatus.OFFLINE
        ok
    }

    private suspend fun flushPendingOps(): Boolean {
        while (true) {
            val ops = dao.getPendingOps()
            if (ops.isEmpty()) return true
            // Consecutive swipes go up as one batch
            val batch = if (ops[0].type == PendingOpEntity.SWIPE) ops.takeWhile { it.type == PendingOpEntity.SWIPE } else listOf(ops[0])
            try {
                pushBatch(batch)
            } catch (e: ApiException) {
                if (e.isRetryable || e.status == 401) throw e
                Log.w(TAG, "dropping rejected op ${batch.first().type}: ${e.message}")
            }
            dao.deletePendingOps(batch.map { it.id })
        }
    }

    private suspend fun pushBatch(batch: List<PendingOpEntity>) {
        val op = batch.first()
        when (op.type) {
            PendingOpEntity.SWIPE -> {
                val res = api.putSwipes(batch.map { json.decodeFromString(SwipeInput.serializer(), it.payload) })
                // Matches created by our own swipe were already celebrated locally
                markMatchesSeen(res.newMatches)
            }
            PendingOpEntity.UNSWIPE -> api.deleteSwipe(json.decodeFromString(UnswipePayload.serializer(), op.payload).nameId)
            PendingOpEntity.ADD_NAME -> api.addName(json.decodeFromString(NewNameRequest.serializer(), op.payload))
            PendingOpEntity.MATCH_PATCH -> {
                val p = json.decodeFromString(MatchPatchPayload.serializer(), op.payload)
                api.patchMatch(p.nameId, MatchPatch(p.rating, p.notes))
            }
            PendingOpEntity.MATCH_DISMISS -> api.dismissMatch(json.decodeFromString(UnswipePayload.serializer(), op.payload).nameId)
        }
    }

    private suspend fun applyState(state: RemoteState) {
        val swipes = state.swipes.map { SwipeEntity(it.deviceId, it.nameId, it.liked, it.ts) }
        val matches = state.matches.map { MatchEntity(it.nameId, it.matchedAt, it.notes, it.rating) }
        val names = state.names.map {
            BabyNameEntity(
                id = it.id,
                name = it.name,
                gender = it.gender,
                origin = it.origin,
                meaning = it.meaning,
                pronunciation = it.pronunciation,
                popularityRank = it.popularityRank,
                styleTags = it.styleTags.joinToString(","),
                isUserAdded = true,
                addedByUserId = it.addedBy
            )
        }
        dao.replaceSharedState(swipes, matches, names)
        overlayPendingOps(state.me.id)

        val pairingChanged = session.session.value.partnerId != state.partner?.id
        session.update {
            it.copy(
                myName = state.me.displayName,
                partnerId = state.partner?.id,
                partnerName = state.partner?.displayName,
                pairCode = state.pairCode
            )
        }
        if (pairingChanged) reconnectSocket()

        val currentIds = state.matches.map { it.nameId }
        if (!session.initialSyncDone) {
            // Don't celebrate everything that already existed before this install
            session.initialSyncDone = true
            markMatchesSeen(currentIds)
        } else {
            val fresh = currentIds.filterNot { it in session.seenMatchIds }
            if (fresh.isNotEmpty()) {
                markMatchesSeen(fresh)
                _newMatches.emit(fresh)
            }
        }
        _changes.emit(Unit)
    }

    /** Re-apply edits made while the sync was in flight so they don't flicker away. */
    private suspend fun overlayPendingOps(myId: String) {
        for (op in dao.getPendingOps()) {
            when (op.type) {
                PendingOpEntity.SWIPE -> {
                    val s = json.decodeFromString(SwipeInput.serializer(), op.payload)
                    dao.insertSwipe(SwipeEntity(myId, s.nameId, s.liked, s.ts))
                }
                PendingOpEntity.UNSWIPE ->
                    dao.deleteSwipe(myId, json.decodeFromString(UnswipePayload.serializer(), op.payload).nameId)
            }
        }
    }

    // --- account & pairing ---

    suspend fun register(displayName: String) {
        val res = api.register(displayName)
        session.update { it.copy(token = res.token, deviceId = res.deviceId, myName = displayName) }
        sync()
        reconnectSocket()
    }

    suspend fun createPairCode(): String {
        val code = api.createCouple().pairCode
        session.update { it.copy(pairCode = code) }
        reconnectSocket()
        sync()
        return code
    }

    suspend fun joinPartner(code: String) {
        api.joinCouple(code)
        reconnectSocket()
        sync()
    }

    suspend fun unlink() {
        api.leaveCouple()
        session.update { it.copy(partnerId = null, partnerName = null, pairCode = null) }
        reconnectSocket()
        sync()
    }

    suspend fun rename(displayName: String) {
        api.rename(displayName)
        session.update { it.copy(myName = displayName) }
    }

    // --- live updates while the app is visible ---

    fun onForeground() {
        foreground = true
        requestSync()
        startSocketLoop()
    }

    fun onBackground() {
        foreground = false
        socketJob?.cancel()
        socketJob = null
        socket?.close(1000, "background")
        socket = null
    }

    private fun reconnectSocket() {
        val old = socket
        socket = null
        socketOpen = false
        old?.close(1000, "reconnect")
        if (foreground) {
            socketJob?.cancel()
            socketJob = null
            startSocketLoop()
        }
    }

    private fun startSocketLoop() {
        if (socketJob?.isActive == true) return
        socketJob = scope.launch {
            var backoffMs = 2_000L
            while (isActive && foreground) {
                if (socket == null && session.session.value.isRegistered) {
                    socket = api.openSocket(listener)
                }
                delay(if (socketOpen) 30_000 else backoffMs)
                if (socketOpen) {
                    backoffMs = 2_000L
                } else {
                    // Not connected: poll so changes still arrive, and retry the socket
                    socket?.cancel()
                    socket = null
                    sync()
                    backoffMs = (backoffMs * 2).coerceAtMost(60_000L)
                }
            }
        }
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (webSocket !== socket) return
            socketOpen = true
            requestSync()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (text.contains("\"changed\"")) requestSync()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = dropSocket(webSocket)

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) = dropSocket(webSocket)

        private fun dropSocket(webSocket: WebSocket) {
            if (webSocket !== socket) return
            socketOpen = false
            socket = null
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
