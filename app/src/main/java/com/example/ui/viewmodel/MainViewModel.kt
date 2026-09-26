package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.KindredApp
import com.example.data.local.SwipeEntity
import com.example.data.model.AlgorithmConfig
import com.example.data.model.AlgorithmExplanation
import com.example.data.model.BabyName
import com.example.data.model.Gender
import com.example.data.model.LengthPreference
import com.example.data.model.PopularityTier
import com.example.data.remote.ApiException
import com.example.data.remote.DeepDive
import com.example.data.remote.DeepDiveRequest
import com.example.data.remote.IdeaRequest
import com.example.data.remote.NameBrief
import com.example.data.remote.NewNameRequest
import com.example.data.remote.RemoteName
import com.example.data.remote.TasteRequest
import com.example.data.repository.MatchWithDetails
import com.example.data.seed.NameCatalog
import com.example.data.session.Session
import com.example.data.sync.SyncStatus
import com.example.ui.screens.LikedNameWithPartnerStatus
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.IOException

enum class AiTask { SUGGEST, DESCRIBE, TASTE }

data class DeepDiveState(
    val babyName: BabyName,
    val lastName: String,
    val loading: Boolean = false,
    val result: DeepDive? = null,
    val error: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as KindredApp).container
    private val repository = container.repository
    private val sync = container.sync
    private val sessionStore = container.session

    val session: StateFlow<Session> = sessionStore.session
    val syncStatus: StateFlow<SyncStatus> = sync.status

    private val myId: String get() = session.value.deviceId ?: LOCAL_USER
    private val partnerId: String get() = session.value.partnerId ?: NO_PARTNER

    fun myName(): String = session.value.myName.ifBlank { "You" }
    fun partnerName(): String = session.value.partnerName ?: "your partner"

    // Filter states
    val genderFilter = MutableStateFlow<Gender?>(null)
    val lengthFilter = MutableStateFlow(LengthPreference.ANY)
    val popularityFilter = MutableStateFlow<PopularityTier?>(null)

    // Algorithm Config & Explanation
    val algorithmConfig = MutableStateFlow(sessionStore.algorithmConfig)
    val algorithmExplanation = MutableStateFlow(
        AlgorithmExplanation(
            primaryFactors = listOf("Cold start: Blending popular multicultural classics and fresh rising stars."),
            originBiasDescription = "Balanced multicultural distribution.",
            styleBiasDescription = "Broad aesthetic exploration.",
            matchRecommendationNote = PRIVACY_NOTE
        )
    )

    private val _currentQueue = MutableStateFlow<List<BabyName>>(emptyList())
    val currentQueue: StateFlow<List<BabyName>> = _currentQueue.asStateFlow()

    private val _sharedMatches = MutableStateFlow<List<MatchWithDetails>>(emptyList())
    val sharedMatches: StateFlow<List<MatchWithDetails>> = _sharedMatches.asStateFlow()

    private val _myLikes = MutableStateFlow<List<LikedNameWithPartnerStatus>>(emptyList())
    val myLikes: StateFlow<List<LikedNameWithPartnerStatus>> = _myLikes.asStateFlow()

    val matchCelebrationBabyName = MutableStateFlow<BabyName?>(null)

    // Onboarding & pairing
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    /** True between registering and finishing/skipping the pairing step. */
    val showPairingStep = MutableStateFlow(false)

    // AI
    private val _aiTask = MutableStateFlow<AiTask?>(null)
    val aiTask: StateFlow<AiTask?> = _aiTask.asStateFlow()
    private val _tasteSummary = MutableStateFlow<String?>(null)
    val tasteSummary: StateFlow<String?> = _tasteSummary.asStateFlow()
    private val _deepDive = MutableStateFlow<DeepDiveState?>(null)
    val deepDive: StateFlow<DeepDiveState?> = _deepDive.asStateFlow()

    /** Names the AI just added; shown first in the deck. */
    private var priorityIds: List<String> = emptyList()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        NotificationHelper.createNotificationChannel(application)

        viewModelScope.launch {
            repository.seedDatabaseIfEmpty(withContext(Dispatchers.IO) { NameCatalog.load(getApplication()) })
            reloadAll(keepTopCard = false)
        }

        viewModelScope.launch {
            repository.getAllMatches().collect { _sharedMatches.value = it }
        }

        viewModelScope.launch {
            sync.changes.collect { reloadAll(keepTopCard = true) }
        }

        viewModelScope.launch {
            sync.newMatches.collect { ids -> celebrateRemoteMatches(ids) }
        }
    }

    private suspend fun reloadAll(keepTopCard: Boolean) {
        reloadQueue(keepTopCard)
        reloadMyLikes()
        refreshExplanation()
    }

    // --- onboarding & pairing ---

    fun register(displayName: String) = runBusy {
        sync.register(displayName.trim())
        showPairingStep.value = true
    }

    fun createPairCode() = runBusy { sync.createPairCode() }

    fun joinPartner(code: String) = runBusy {
        sync.joinPartner(code.trim())
        showPairingStep.value = false
        _messages.emit("Linked with ${partnerName()} 💕")
    }

    fun unlinkPartner() = runBusy {
        sync.unlink()
        _messages.emit("Unlinked")
    }

    fun renameMe(displayName: String) = runBusy { sync.rename(displayName.trim()) }

    fun finishPairingStep() {
        showPairingStep.value = false
    }

    fun syncNow() = sync.requestSync()

    private fun runBusy(block: suspend () -> Unit) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                block()
            } catch (e: IOException) {
                _messages.emit(friendlyError(e))
            } finally {
                _busy.value = false
            }
        }
    }

    // --- filters & tuning ---

    fun setGenderFilter(gender: Gender?) {
        genderFilter.value = gender
        viewModelScope.launch { reloadQueue(keepTopCard = false) }
    }

    fun setLengthFilter(lengthPref: LengthPreference) {
        lengthFilter.value = lengthPref
        viewModelScope.launch { reloadQueue(keepTopCard = false) }
    }

    fun setPopularityFilter(tier: PopularityTier?) {
        popularityFilter.value = tier
        viewModelScope.launch { reloadQueue(keepTopCard = false) }
    }

    fun updateAlgorithmConfig(config: AlgorithmConfig) {
        algorithmConfig.value = config
        sessionStore.algorithmConfig = config
        viewModelScope.launch {
            reloadQueue(keepTopCard = false)
            refreshExplanation()
            _messages.emit("Deck recalibrated ✨")
        }
    }

    fun resetAlgorithmDefaults() = updateAlgorithmConfig(AlgorithmConfig())

    // --- swiping ---

    fun swipe(babyName: BabyName, isLiked: Boolean) {
        // Optimistically remove from queue immediately for 60fps responsiveness
        _currentQueue.value = _currentQueue.value.filter { it.id != babyName.id }

        viewModelScope.launch {
            val isMutualMatch = repository.recordSwipe(myId, partnerId, babyName.id, isLiked)
            sync.enqueueSwipe(babyName.id, isLiked, System.currentTimeMillis())
            if (isMutualMatch) celebrate(babyName)
            reloadMyLikes()
            refreshExplanation()
        }
    }

    fun undoSwipe() {
        viewModelScope.launch {
            val reverted = repository.undoLastSwipe(myId) ?: return@launch
            sync.enqueueUnswipe(reverted.id)
            priorityIds = listOf(reverted.id) + priorityIds
            reloadQueue(keepTopCard = false)
            reloadMyLikes()
        }
    }

    fun removeLike(nameId: String) {
        viewModelScope.launch {
            repository.removeSwipe(myId, nameId)
            sync.enqueueUnswipe(nameId)
            reloadQueue(keepTopCard = true)
            reloadMyLikes()
        }
    }

    fun addCustomName(
        name: String,
        gender: Gender,
        origin: String,
        meaning: String,
        pronunciation: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val (added, isMatch) = repository.addCustomName(
                nameText = name,
                gender = gender,
                origin = origin,
                meaning = meaning,
                pronunciation = pronunciation,
                tags = tags,
                activeUserId = myId,
                partnerId = partnerId
            )
            sync.enqueueAddName(
                NewNameRequest(
                    id = added.id,
                    name = added.name,
                    gender = added.gender.name,
                    origin = added.origin,
                    meaning = added.meaning,
                    pronunciation = added.pronunciation,
                    styleTags = added.styleTags
                )
            )
            sync.enqueueSwipe(added.id, true, System.currentTimeMillis())
            if (isMatch) celebrate(added)
            reloadQueue(keepTopCard = true)
            reloadMyLikes()
            _messages.emit("${added.name} added to ${partnerName()}'s deck")
        }
    }

    // --- shared matches ---

    fun updateMatchRating(nameId: String, rating: Int) {
        viewModelScope.launch {
            repository.updateMatchRating(nameId, rating)
            sync.enqueueMatchPatch(nameId, rating = rating)
        }
    }

    fun updateMatchNotes(nameId: String, notes: String) {
        viewModelScope.launch {
            repository.updateMatchNotes(nameId, notes)
            sync.enqueueMatchPatch(nameId, notes = notes)
        }
    }

    fun deleteMatch(nameId: String) {
        viewModelScope.launch {
            repository.deleteMatch(nameId)
            sync.enqueueMatchDismiss(nameId)
        }
    }

    fun dismissMatchCelebration() {
        matchCelebrationBabyName.value = null
    }

    private fun celebrate(babyName: BabyName) {
        sync.markMatchesSeen(listOf(babyName.id))
        NotificationHelper.sendMatchNotification(
            context = getApplication(),
            name = babyName.name,
            partnerName = partnerName(),
            meaning = babyName.meaning
        )
        matchCelebrationBabyName.value = babyName
    }

    private suspend fun celebrateRemoteMatches(ids: List<String>) {
        val names = repository.getAllNames().first().associateBy { it.id }
        val first = ids.firstNotNullOfOrNull { names[it] } ?: return
        celebrate(first)
        if (ids.size > 1) _messages.emit("${ids.size} new shared favorites!")
    }

    // --- AI ---

    fun aiSuggest() = runAi(AiTask.SUGGEST) {
        val names = container.api.aiSuggest(buildIdeaRequest(prompt = null)).names
        addAiNames(names)
    }

    fun aiDescribe(prompt: String) = runAi(AiTask.DESCRIBE) {
        val names = container.api.aiDescribe(buildIdeaRequest(prompt = prompt.trim())).names
        addAiNames(names)
    }

    fun aiTaste() = runAi(AiTask.TASTE) {
        val allNames = repository.getAllNames().first().associateBy { it.id }
        val mine = repository.getSwipesForUser(myId).first()
        val theirs = repository.getSwipesForUser(partnerId).first()
        fun briefs(swipes: List<SwipeEntity>, liked: Boolean, limit: Int) = swipes
            .filter { it.isLiked == liked }
            .sortedByDescending { it.timestamp }
            .take(limit)
            .mapNotNull { allNames[it.nameId]?.toBrief() }
        // Only names you've both liked are shared with Claude, so the summary can't reveal
        // anything your partner liked that you haven't.
        val myLikedIds = mine.filter { it.isLiked }.map { it.nameId }.toSet()
        _tasteSummary.value = container.api.aiTaste(
            TasteRequest(
                myLikes = briefs(mine, true, 120),
                partnerLikes = briefs(theirs.filter { it.nameId in myLikedIds }, true, 120),
                dislikes = briefs(mine, false, 120)
            )
        ).summary
    }

    fun openDeepDive(babyName: BabyName) {
        val state = DeepDiveState(babyName, sessionStore.lastName)
        _deepDive.value = state
        loadDeepDive(state)
    }

    fun retryDeepDive(lastName: String) {
        val current = _deepDive.value ?: return
        sessionStore.lastName = lastName.trim()
        loadDeepDive(current.copy(lastName = lastName.trim()))
    }

    fun closeDeepDive() {
        _deepDive.value = null
    }

    private fun loadDeepDive(state: DeepDiveState) {
        _deepDive.value = state.copy(loading = true, error = null)
        viewModelScope.launch {
            _deepDive.value = try {
                val result = container.api.aiDeepDive(
                    DeepDiveRequest(state.babyName.toBrief(), state.lastName.ifBlank { null })
                )
                state.copy(loading = false, result = result)
            } catch (e: IOException) {
                state.copy(loading = false, error = friendlyError(e))
            }
        }
    }

    private fun runAi(task: AiTask, block: suspend () -> Unit) {
        if (_aiTask.value != null) return
        viewModelScope.launch {
            _aiTask.value = task
            try {
                block()
            } catch (e: IOException) {
                _messages.emit(friendlyError(e))
            } finally {
                _aiTask.value = null
            }
        }
    }

    private suspend fun buildIdeaRequest(prompt: String?): IdeaRequest {
        val allNames = repository.getAllNames().first()
        val byId = allNames.associateBy { it.id }
        val swipes = (repository.getSwipesForUser(myId).first() + repository.getSwipesForUser(partnerId).first())
            .sortedByDescending { it.timestamp }
        return IdeaRequest(
            liked = swipes.filter { it.isLiked }.take(150).mapNotNull { byId[it.nameId]?.toBrief() },
            disliked = swipes.filterNot { it.isLiked }.take(80).mapNotNull { byId[it.nameId]?.toBrief() },
            avoid = allNames.map { it.name }.distinct(),
            gender = genderFilter.value?.name,
            count = 10,
            prompt = prompt
        )
    }

    private suspend fun addAiNames(names: List<RemoteName>) {
        if (names.isEmpty()) {
            _messages.emit("Claude didn't find any new names this time — try rewording")
            return
        }
        repository.upsertNames(names.map { it.toDomain() })
        priorityIds = names.map { it.id } + priorityIds
        reloadQueue(keepTopCard = false)
        _messages.emit("Added ${names.size} names from Claude to your deck ✨")
    }

    // --- queue & derived lists ---

    private suspend fun reloadQueue(keepTopCard: Boolean) {
        var queue = repository.computeQueueForUser(
            activeUserId = myId,
            partnerId = partnerId,
            genderFilter = genderFilter.value,
            lengthFilter = lengthFilter.value,
            popularityTierFilter = popularityFilter.value,
            algorithmConfig = algorithmConfig.value
        )

        val eligible = queue.map { it.id }.toSet()
        priorityIds = priorityIds.filter { it in eligible }
        if (priorityIds.isNotEmpty()) {
            val (boosted, rest) = queue.partition { it.id in priorityIds }
            queue = boosted.sortedBy { priorityIds.indexOf(it.id) } + rest
        }

        // Don't yank the card the user is looking at when partner changes arrive
        val top = _currentQueue.value.firstOrNull()
        if (keepTopCard && top != null && queue.firstOrNull()?.id != top.id) {
            queue.firstOrNull { it.id == top.id }?.let { keep -> queue = listOf(keep) + queue.filter { it.id != keep.id } }
        }
        _currentQueue.value = queue
    }

    private suspend fun reloadMyLikes() {
        val allNamesMap = repository.getAllNames().first().associateBy { it.id }
        val userLikedSwipes = repository.getSwipesForUser(myId).first().filter { it.isLiked }
        val partnerSwipeMap = repository.getSwipesForUser(partnerId).first().associate { it.nameId to it.isLiked }

        _myLikes.value = userLikedSwipes.sortedByDescending { it.timestamp }.mapNotNull { swipe ->
            val name = allNamesMap[swipe.nameId] ?: return@mapNotNull null
            LikedNameWithPartnerStatus(
                babyName = name,
                // Only reveal the partner's vote once it's a match; passes stay private
                partnerLiked = if (partnerSwipeMap[swipe.nameId] == true) true else null,
                timestamp = swipe.timestamp
            )
        }
    }

    private suspend fun refreshExplanation() {
        algorithmExplanation.value = repository.getAlgorithmExplanation(myId, partnerId, algorithmConfig.value)
    }

    private fun friendlyError(e: IOException): String = when {
        e is ApiException && e.code == "ai_unavailable" -> "AI isn't set up on the server yet: ${e.message}"
        e is ApiException && e.code == "ai_quota" -> e.message ?: "Daily AI limit reached — try again tomorrow"
        e is ApiException && e.code == "ai_refused" -> "Claude couldn't help with that request — try rewording it"
        e is ApiException && e.code == "ai_rate_limited" -> "Claude is busy right now — try again in a minute"
        e is ApiException && e.code == "code_not_found" -> "That code doesn't match any couple — double-check it"
        e is ApiException && e.code == "couple_full" -> "That couple already has two people"
        e is ApiException && e.code == "already_paired" -> "You're already linked with a partner"
        e is ApiException -> "Server error (${e.status}) — try again"
        else -> "Can't reach the server — check your connection"
    }

    companion object {
        private const val LOCAL_USER = "me"
        private const val NO_PARTNER = "no_partner"
        const val PRIVACY_NOTE = "Your partner's picks stay secret until you like the same name — then it's a match!"
    }
}

private fun BabyName.toBrief() = NameBrief(name, gender.name, origin, meaning, styleTags)

private fun RemoteName.toDomain() = BabyName(
    id = id,
    name = name,
    gender = runCatching { Gender.valueOf(gender) }.getOrDefault(Gender.UNISEX),
    origin = origin,
    meaning = meaning,
    pronunciation = pronunciation,
    popularityRank = popularityRank,
    styleTags = styleTags,
    isUserAdded = true,
    addedByUserId = addedBy
)
