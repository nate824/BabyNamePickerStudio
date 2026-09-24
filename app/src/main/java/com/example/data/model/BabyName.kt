package com.example.data.model

enum class Gender {
    BOY,
    GIRL,
    UNISEX;

    fun displayName(): String = when (this) {
        BOY -> "Boy"
        GIRL -> "Girl"
        UNISEX -> "Unisex"
    }
}

enum class PopularityTier(val label: String) {
    TOP_50("Top 50 Trend"),
    RISING("Rising Fast"),
    TIMELESS("Timeless Classic"),
    UNIQUE("Rare & Unique");

    companion object {
        fun fromRank(rank: Int): PopularityTier = when {
            rank in 1..50 -> TOP_50
            rank in 51..150 -> RISING
            rank in 151..300 -> TIMELESS
            else -> UNIQUE
        }
    }
}

data class BabyName(
    val id: String,
    val name: String,
    val gender: Gender,
    val origin: String,
    val meaning: String,
    val pronunciation: String,
    val popularityRank: Int,
    val styleTags: List<String>,
    val isUserAdded: Boolean = false,
    val addedByUserId: String? = null
) {
    val length: Int get() = name.length
    val popularityTier: PopularityTier get() = PopularityTier.fromRank(popularityRank)
}

data class PartnerProfile(
    val id: String,
    val name: String,
    val avatarInitials: String,
    val pairCode: String
)

data class AlgorithmConfig(
    val isEnabled: Boolean = true,
    val targetOrigins: Set<String> = emptySet(),
    val targetStyles: Set<String> = emptySet(),
    val preferredLength: LengthPreference = LengthPreference.ANY,
    val popularityFocus: PopularityFocus = PopularityFocus.BALANCED,
    val userCustomKeywords: String = ""
)

enum class LengthPreference(val label: String) {
    ANY("Any Length"),
    SHORT("Short (3-4)"),
    MEDIUM("Medium (5-6)"),
    LONG("Long (7+)")
}

enum class PopularityFocus(val label: String) {
    BALANCED("Balanced Mix"),
    TRENDING("Modern & Trending"),
    CLASSIC("Timeless Classics"),
    UNIQUE("Unique & Rare")
}

data class AlgorithmExplanation(
    val primaryFactors: List<String>,
    val originBiasDescription: String,
    val styleBiasDescription: String,
    val matchRecommendationNote: String
)
