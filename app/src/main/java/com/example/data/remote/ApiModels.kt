package com.example.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val displayName: String)

@Serializable
data class RegisterResponse(val deviceId: String, val token: String)

@Serializable
data class PairCodeResponse(val pairCode: String)

@Serializable
data class JoinRequest(val code: String)

@Serializable
data class RenameRequest(val displayName: String)

@Serializable
data class Member(val id: String, val displayName: String)

@Serializable
data class RemoteSwipe(val deviceId: String, val nameId: String, val liked: Boolean, val ts: Long)

@Serializable
data class RemoteMatch(val nameId: String, val matchedAt: Long, val rating: Int, val notes: String)

@Serializable
data class RemoteName(
    val id: String,
    val name: String,
    val gender: String,
    val origin: String = "",
    val meaning: String = "",
    val pronunciation: String = "",
    val popularityRank: Int = 999,
    val styleTags: List<String> = emptyList(),
    val addedBy: String = "",
    val source: String = "user"
)

@Serializable
data class RemoteState(
    val me: Member,
    val partner: Member? = null,
    val pairCode: String? = null,
    val swipes: List<RemoteSwipe>,
    val matches: List<RemoteMatch>,
    val names: List<RemoteName>
)

@Serializable
data class SwipeInput(val nameId: String, val liked: Boolean, val ts: Long)

@Serializable
data class SwipesRequest(val swipes: List<SwipeInput>)

@Serializable
data class SwipesResponse(val newMatches: List<String>)

@Serializable
data class NewNameRequest(
    val id: String,
    val name: String,
    val gender: String,
    val origin: String,
    val meaning: String,
    val pronunciation: String,
    val styleTags: List<String>
)

@Serializable
data class MatchPatch(val rating: Int? = null, val notes: String? = null)

@Serializable
data class NameBrief(
    val name: String,
    val gender: String,
    val origin: String = "",
    val meaning: String = "",
    val styleTags: List<String> = emptyList()
)

@Serializable
data class IdeaRequest(
    val liked: List<NameBrief>,
    val disliked: List<NameBrief>,
    val avoid: List<String>,
    val gender: String? = null,
    val count: Int = 10,
    val prompt: String? = null
)

@Serializable
data class IdeasResponse(val names: List<RemoteName>)

@Serializable
data class TasteRequest(
    val myLikes: List<NameBrief>,
    val partnerLikes: List<NameBrief>,
    val dislikes: List<NameBrief>
)

@Serializable
data class TasteResponse(val summary: String)

@Serializable
data class DeepDiveRequest(val name: NameBrief, val lastName: String?)

@Serializable
data class DeepDive(
    val nicknames: List<String> = emptyList(),
    val lastNameFit: String = "",
    val famousNamesakes: List<String> = emptyList(),
    val middleNameIdeas: List<String> = emptyList(),
    val sibling: String = "",
    val history: String = ""
)

@Serializable
data class ErrorBody(val error: String = "", val detail: kotlinx.serialization.json.JsonElement? = null)
