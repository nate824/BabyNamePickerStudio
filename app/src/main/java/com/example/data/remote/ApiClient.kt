package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Thrown for any non-2xx response; [isRetryable] separates "try again later" from "this will never work". */
class ApiException(val status: Int, val code: String, message: String) : IOException(message) {
    val isRetryable: Boolean get() = status == 408 || status == 429 || status >= 500
}

class ApiClient(
    private val baseUrl: String,
    private val tokenProvider: () -> String?,
    private val http: OkHttpClient = defaultHttpClient()
) {
    val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }

    suspend fun register(displayName: String): RegisterResponse =
        post("/api/devices", RegisterRequest(displayName), auth = false)

    suspend fun state(): RemoteState = send("GET", "/api/state", null)

    suspend fun rename(displayName: String): Unit = patch("/api/me", RenameRequest(displayName))

    suspend fun createCouple(): PairCodeResponse = post("/api/couples", emptyMap<String, String>())

    suspend fun joinCouple(code: String): RemoteState = post("/api/couples/join", JoinRequest(code))

    suspend fun leaveCouple(): Unit = post("/api/couples/leave", emptyMap<String, String>())

    suspend fun putSwipes(swipes: List<SwipeInput>): SwipesResponse =
        send("PUT", "/api/swipes", json.encodeToString(SwipesRequest.serializer(), SwipesRequest(swipes)))

    suspend fun deleteSwipe(nameId: String): Unit = send("DELETE", "/api/swipes/${enc(nameId)}", null)

    suspend fun addName(name: NewNameRequest): RemoteName = post("/api/names", name)

    suspend fun patchMatch(nameId: String, patch: MatchPatch): Unit = patch("/api/matches/${enc(nameId)}", patch)

    suspend fun dismissMatch(nameId: String): Unit = send("DELETE", "/api/matches/${enc(nameId)}", null)

    suspend fun aiSuggest(req: IdeaRequest): IdeasResponse = post("/api/ai/suggest", req)

    suspend fun aiDescribe(req: IdeaRequest): IdeasResponse = post("/api/ai/describe", req)

    suspend fun aiTaste(req: TasteRequest): TasteResponse = post("/api/ai/taste", req)

    suspend fun aiDeepDive(req: DeepDiveRequest): DeepDive = post("/api/ai/deep-dive", req)

    fun openSocket(listener: WebSocketListener): WebSocket? {
        val token = tokenProvider() ?: return null
        val wsUrl = baseUrl.replaceFirst("https://", "wss://").replaceFirst("http://", "ws://") + "/api/ws"
        val request = Request.Builder().url(wsUrl).header("Authorization", "Bearer $token").build()
        return http.newWebSocket(request, listener)
    }

    private fun enc(s: String) = java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    private suspend inline fun <reified B, reified T> post(path: String, body: B, auth: Boolean = true): T =
        send("POST", path, json.encodeToString(kotlinx.serialization.serializer<B>(), body), auth)

    private suspend inline fun <reified B> patch(path: String, body: B): Unit =
        send("PATCH", path, json.encodeToString(kotlinx.serialization.serializer<B>(), body))

    private suspend inline fun <reified T> send(method: String, path: String, body: String?, auth: Boolean = true): T {
        val text = sendRaw(method, path, body, auth)
        @Suppress("UNCHECKED_CAST")
        return if (T::class == Unit::class) Unit as T else json.decodeFromString(kotlinx.serialization.serializer<T>(), text)
    }

    private suspend fun sendRaw(method: String, path: String, body: String?, auth: Boolean): String =
        withContext(Dispatchers.IO) {
            val requestBody = (body ?: if (method == "GET" || method == "DELETE") null else "{}")
                ?.toRequestBody(JSON_TYPE)
            val builder = Request.Builder().url(baseUrl + path).method(method, requestBody)
            if (auth) tokenProvider()?.let { builder.header("Authorization", "Bearer $it") }
            http.newCall(builder.build()).execute().use { res ->
                val text = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    val err = runCatching { json.decodeFromString(ErrorBody.serializer(), text) }.getOrNull()
                    val detail = (err?.detail as? JsonPrimitive)?.content
                    throw ApiException(res.code, err?.error ?: "http_${res.code}", detail ?: err?.error ?: "HTTP ${res.code}")
                }
                text
            }
        }

    companion object {
        private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()

        fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            // AI calls can take a while to generate a batch of names
            .readTimeout(120, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
    }
}
