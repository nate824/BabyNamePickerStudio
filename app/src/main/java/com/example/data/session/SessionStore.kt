package com.example.data.session

import android.content.Context
import com.example.data.model.AlgorithmConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

data class Session(
    val token: String? = null,
    val deviceId: String? = null,
    val myName: String = "",
    val partnerId: String? = null,
    val partnerName: String? = null,
    val pairCode: String? = null
) {
    val isRegistered: Boolean get() = token != null && deviceId != null
    val isPaired: Boolean get() = partnerId != null
}

/** Small persisted key/value state: identity, pairing, and per-device preferences. */
class SessionStore(context: Context, prefsName: String = "kindred_session") {
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _session = MutableStateFlow(load())
    val session: StateFlow<Session> = _session.asStateFlow()

    private fun load() = Session(
        token = prefs.getString("token", null),
        deviceId = prefs.getString("deviceId", null),
        myName = prefs.getString("myName", "") ?: "",
        partnerId = prefs.getString("partnerId", null),
        partnerName = prefs.getString("partnerName", null),
        pairCode = prefs.getString("pairCode", null)
    )

    fun update(transform: (Session) -> Session) {
        val next = transform(_session.value)
        prefs.edit()
            .putString("token", next.token)
            .putString("deviceId", next.deviceId)
            .putString("myName", next.myName)
            .putString("partnerId", next.partnerId)
            .putString("partnerName", next.partnerName)
            .putString("pairCode", next.pairCode)
            .apply()
        _session.value = next
    }

    var seenMatchIds: Set<String>
        get() = prefs.getStringSet("seenMatchIds", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("seenMatchIds", value).apply()

    var initialSyncDone: Boolean
        get() = prefs.getBoolean("initialSyncDone", false)
        set(value) = prefs.edit().putBoolean("initialSyncDone", value).apply()

    var lastName: String
        get() = prefs.getString("lastName", "") ?: ""
        set(value) = prefs.edit().putString("lastName", value).apply()

    var algorithmConfig: AlgorithmConfig
        get() = prefs.getString("algorithmConfig", null)
            ?.let { runCatching { json.decodeFromString(AlgorithmConfig.serializer(), it) }.getOrNull() }
            ?: AlgorithmConfig()
        set(value) = prefs.edit().putString("algorithmConfig", json.encodeToString(AlgorithmConfig.serializer(), value)).apply()
}
