package com.example.data.repository

import com.example.data.local.AppDao
import com.example.data.local.BabyNameEntity
import com.example.data.local.MatchEntity
import com.example.data.local.SwipeEntity
import com.example.data.model.AlgorithmConfig
import com.example.data.model.AlgorithmExplanation
import com.example.data.model.BabyName
import com.example.data.model.Gender
import com.example.data.model.LengthPreference
import com.example.data.model.PopularityFocus
import com.example.data.model.PopularityTier
import com.example.data.seed.SeedBabyNames
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.random.Random

data class MatchWithDetails(
    val match: MatchEntity,
    val babyName: BabyName
)

class BabyNameRepository(private val appDao: AppDao) {

    companion object {
        private const val PARTNER_PICK_WINDOW = 20
    }

    /** Insert any built-in names the database doesn't have yet (existing rows are left untouched). */
    suspend fun seedDatabaseIfEmpty(extraNames: List<BabyName> = emptyList()) {
        val all = SeedBabyNames.initialNames + extraNames
        appDao.insertBabyNames(all.map { BabyNameEntity.fromDomain(it) })
    }

    fun getAllNames(): Flow<List<BabyName>> =
        appDao.getAllBabyNames().map { list -> list.map { it.toDomain() } }

    suspend fun upsertNames(names: List<BabyName>) {
        appDao.upsertBabyNames(names.map { BabyNameEntity.fromDomain(it) })
    }

    fun getAllMatches(): Flow<List<MatchWithDetails>> {
        return appDao.getAllMatches().map { matches ->
            matches.mapNotNull { match ->
                val nameEntity = appDao.getBabyNameById(match.nameId)
                nameEntity?.let {
                    MatchWithDetails(match, it.toDomain())
                }
            }
        }
    }

    fun getSwipesForUser(userId: String): Flow<List<SwipeEntity>> =
        appDao.getSwipesForUser(userId)

    fun getAllSwipes(): Flow<List<SwipeEntity>> =
        appDao.getAllSwipes()

    suspend fun recordSwipe(
        userId: String,
        partnerId: String,
        nameId: String,
        isLiked: Boolean
    ): Boolean {
        // Record swipe
        appDao.insertSwipe(
            SwipeEntity(
                userId = userId,
                nameId = nameId,
                isLiked = isLiked,
                timestamp = System.currentTimeMillis()
            )
        )

        // If liked, check if the partner also liked this name!
        if (isLiked) {
            val partnerSwipe = appDao.getSwipe(partnerId, nameId)
            if (partnerSwipe != null && partnerSwipe.isLiked) {
                // Mutual match!
                appDao.insertMatch(
                    MatchEntity(
                        nameId = nameId,
                        matchedAt = System.currentTimeMillis()
                    )
                )
                return true
            }
        } else {
            // If disliked, if it was in matches, we can keep or remove, but keeping it clean:
            val existingMatch = appDao.getMatch(nameId)
            if (existingMatch != null) {
                appDao.deleteMatch(nameId)
            }
        }
        return false
    }

    suspend fun undoLastSwipe(userId: String): BabyName? {
        val lastSwipe = appDao.getLastSwipeForUser(userId) ?: return null
        appDao.deleteSwipe(userId, lastSwipe.nameId)
        val match = appDao.getMatch(lastSwipe.nameId)
        if (match != null) {
            appDao.deleteMatch(lastSwipe.nameId)
        }
        val entity = appDao.getBabyNameById(lastSwipe.nameId)
        return entity?.toDomain()
    }

    /** Remove one specific swipe (and any match it created). */
    suspend fun removeSwipe(userId: String, nameId: String) {
        appDao.deleteSwipe(userId, nameId)
        if (appDao.getMatch(nameId) != null) appDao.deleteMatch(nameId)
    }

    suspend fun addCustomName(
        nameText: String,
        gender: Gender,
        origin: String,
        meaning: String,
        pronunciation: String,
        tags: List<String>,
        activeUserId: String,
        partnerId: String
    ): Pair<BabyName, Boolean> {
        val id = "custom_" + UUID.randomUUID().toString().take(8)
        val cleanPronunciation = pronunciation.ifBlank { nameText }
        val babyName = BabyName(
            id = id,
            name = nameText.trim().replaceFirstChar { it.uppercase() },
            gender = gender,
            origin = origin.ifBlank { "Unknown" },
            meaning = meaning.ifBlank { "No meaning added yet" },
            pronunciation = cleanPronunciation,
            popularityRank = 999,
            styleTags = tags,
            isUserAdded = true,
            addedByUserId = activeUserId
        )

        // Insert into database
        appDao.insertBabyName(BabyNameEntity.fromDomain(babyName))

        // Automatically like it for the adding user!
        val isMatch = recordSwipe(
            userId = activeUserId,
            partnerId = partnerId,
            nameId = id,
            isLiked = true
        )

        return Pair(babyName, isMatch)
    }

    suspend fun updateMatchNotes(nameId: String, notes: String) {
        appDao.updateMatchNotes(nameId, notes)
    }

    suspend fun updateMatchRating(nameId: String, rating: Int) {
        appDao.updateMatchRating(nameId, rating)
    }

    suspend fun deleteMatch(nameId: String) {
        appDao.deleteMatch(nameId)
    }

    suspend fun computeQueueForUser(
        activeUserId: String,
        partnerId: String,
        genderFilter: Gender?, // null = ALL
        lengthFilter: LengthPreference,
        popularityTierFilter: PopularityTier?,
        algorithmConfig: AlgorithmConfig,
        random: Random = Random.Default
    ): List<BabyName> {
        val allNames = appDao.getAllBabyNames().first().map { it.toDomain() }
        val userSwipes = appDao.getSwipesForUser(activeUserId).first()
        val partnerSwipes = appDao.getSwipesForUser(partnerId).first()

        val swipedNameIdsByUser = userSwipes.map { it.nameId }.toSet()
        val partnerLikedNameIds = partnerSwipes.filter { it.isLiked }.map { it.nameId }.toSet()

        // Filter unswiped names according to user's criteria
        val eligibleNames = allNames.filter { babyName ->
            // Must NOT have been swiped by the active user
            if (swipedNameIdsByUser.contains(babyName.id)) return@filter false

            // Gender filter
            if (genderFilter != null && genderFilter != Gender.UNISEX) {
                if (babyName.gender != genderFilter && babyName.gender != Gender.UNISEX) return@filter false
            }

            // Length filter
            when (lengthFilter) {
                LengthPreference.ANY -> true
                LengthPreference.SHORT -> babyName.length <= 4
                LengthPreference.MEDIUM -> babyName.length in 5..6
                LengthPreference.LONG -> babyName.length >= 7
            }.let { if (!it) return@filter false }

            // Popularity tier filter
            if (popularityTierFilter != null) {
                if (babyName.popularityTier != popularityTierFilter) return@filter false
            }

            true
        }

        // Names the partner liked surface early so matches happen sooner, but they're scattered
        // at random through the next stretch of cards so nothing gives the partner's picks away.
        val (partnerPicks, remainingCandidates) = eligibleNames.partition {
            partnerLikedNameIds.contains(it.id)
        }

        val sortedRemaining = if (!algorithmConfig.isEnabled) {
            // Random / Natural shuffle when Algo is turned off
            remainingCandidates.shuffled()
        } else {
            // Apply Smart AI Recommendation Algorithm based on swipe history and tuning
            scoreAndSortByAlgorithm(
                candidates = remainingCandidates,
                userSwipes = userSwipes,
                partnerSwipes = partnerSwipes,
                allNames = allNames,
                config = algorithmConfig
            )
        }

        return scatterIntoFront(partnerPicks.shuffled(random), sortedRemaining, random)
    }

    /** Place [picks] at random slots among the first max(20, 3 × picks) cards of [rest]. */
    private fun scatterIntoFront(picks: List<BabyName>, rest: List<BabyName>, random: Random): List<BabyName> {
        if (picks.isEmpty()) return rest
        val total = picks.size + rest.size
        val window = minOf(total, maxOf(PARTNER_PICK_WINDOW, picks.size * 3))
        val slots = (0 until window).shuffled(random).take(picks.size).toSet()
        val pickIter = picks.iterator()
        val restIter = rest.iterator()
        return List(total) { i ->
            if ((i in slots || !restIter.hasNext()) && pickIter.hasNext()) pickIter.next() else restIter.next()
        }
    }

    private fun scoreAndSortByAlgorithm(
        candidates: List<BabyName>,
        userSwipes: List<SwipeEntity>,
        partnerSwipes: List<SwipeEntity>,
        allNames: List<BabyName>,
        config: AlgorithmConfig
    ): List<BabyName> {
        val namesMap = allNames.associateBy { it.id }

        // Analyze liked names across both partners
        val likedNameIds = (userSwipes.filter { it.isLiked } + partnerSwipes.filter { it.isLiked })
            .map { it.nameId }
            .toSet()

        val dislikedNameIds = (userSwipes.filter { !it.isLiked } + partnerSwipes.filter { !it.isLiked })
            .map { it.nameId }
            .toSet()

        val likedNames = likedNameIds.mapNotNull { namesMap[it] }
        val dislikedNames = dislikedNameIds.mapNotNull { namesMap[it] }

        // Frequency maps for origins
        val likedOrigins = likedNames.groupingBy { it.origin }.eachCount()
        val dislikedOrigins = dislikedNames.groupingBy { it.origin }.eachCount()

        // Frequency maps for style tags
        val likedTags = likedNames.flatMap { it.styleTags }.groupingBy { it }.eachCount()
        val dislikedTags = dislikedNames.flatMap { it.styleTags }.groupingBy { it }.eachCount()

        // Average preferred length
        val avgLength = if (likedNames.isNotEmpty()) {
            likedNames.map { it.length }.average()
        } else 5.5

        return candidates.sortedByDescending { candidate ->
            var score = 100.0

            // 1. Origin match from swipe history
            val originLikes = likedOrigins[candidate.origin] ?: 0
            val originDislikes = dislikedOrigins[candidate.origin] ?: 0
            score += originLikes * 25.0
            score -= originDislikes * 15.0

            // 2. Style tags match from swipe history
            for (tag in candidate.styleTags) {
                score += (likedTags[tag] ?: 0) * 15.0
                score -= (dislikedTags[tag] ?: 0) * 10.0
            }

            // 3. User manual tuned origins
            if (config.targetOrigins.isNotEmpty()) {
                if (config.targetOrigins.any { candidate.origin.contains(it, ignoreCase = true) }) {
                    score += 50.0
                }
            }

            // 4. User manual tuned styles
            if (config.targetStyles.isNotEmpty()) {
                val matchedStyles = candidate.styleTags.count { style ->
                    config.targetStyles.any { it.equals(style, ignoreCase = true) }
                }
                score += matchedStyles * 30.0
            }

            // 5. Length preference proximity
            when (config.preferredLength) {
                LengthPreference.ANY -> {
                    // Slight pull toward observed average length
                    val diff = kotlin.math.abs(candidate.length - avgLength)
                    score -= diff * 5.0
                }
                LengthPreference.SHORT -> if (candidate.length <= 4) score += 40.0
                LengthPreference.MEDIUM -> if (candidate.length in 5..6) score += 40.0
                LengthPreference.LONG -> if (candidate.length >= 7) score += 40.0
            }

            // 6. Popularity focus tuning
            when (config.popularityFocus) {
                PopularityFocus.BALANCED -> {}
                PopularityFocus.TRENDING -> if (candidate.popularityRank <= 50) score += 40.0
                PopularityFocus.CLASSIC -> if (candidate.styleTags.contains("Classic") || candidate.styleTags.contains("Timeless")) score += 45.0
                PopularityFocus.UNIQUE -> if (candidate.popularityRank > 200 || candidate.styleTags.contains("Unique")) score += 50.0
            }

            // 7. Custom search/tuning prompt keywords
            if (config.userCustomKeywords.isNotBlank()) {
                val tokens = config.userCustomKeywords.split(" ", ",", ";").map { it.trim().lowercase() }.filter { it.length > 1 }
                for (token in tokens) {
                    if (candidate.name.lowercase().contains(token)) score += 60.0
                    if (candidate.meaning.lowercase().contains(token)) score += 35.0
                    if (candidate.origin.lowercase().contains(token)) score += 30.0
                    if (candidate.styleTags.any { it.lowercase().contains(token) }) score += 40.0
                }
            }

            score
        }
    }

    suspend fun getAlgorithmExplanation(
        activeUserId: String,
        partnerId: String,
        config: AlgorithmConfig
    ): AlgorithmExplanation {
        val allNames = appDao.getAllBabyNames().first().associateBy { it.id }
        // Only the active user's own likes are described, so the explanation never hints at the partner's picks
        val userSwipes = appDao.getSwipesForUser(activeUserId).first().filter { it.isLiked }

        val allLikedNames = userSwipes.mapNotNull { allNames[it.nameId]?.toDomain() }

        val topOrigins = allLikedNames.groupingBy { it.origin }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(2)
            .map { "${it.key} (${it.value} likes)" }

        val topStyles = allLikedNames.flatMap { it.styleTags }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        val factors = mutableListOf<String>()

        if (allLikedNames.isEmpty() && config.targetOrigins.isEmpty() && config.targetStyles.isEmpty()) {
            factors.add("Cold start: Blending popular multicultural classics and fresh rising stars.")
        } else {
            if (topOrigins.isNotEmpty()) {
                factors.add("Learned Origin Bias: Prioritizing ${topOrigins.joinToString(" & ")}.")
            }
            if (topStyles.isNotEmpty()) {
                factors.add("Aesthetic Alignment: Boosting '${topStyles.joinToString(", ")}' vibes.")
            }
            if (config.targetOrigins.isNotEmpty()) {
                factors.add("Manual Tune: Fostering ${config.targetOrigins.joinToString(", ")} heritage.")
            }
            if (config.targetStyles.isNotEmpty()) {
                factors.add("Custom Theme: Actively weighting ${config.targetStyles.joinToString(", ")}.")
            }
        }

        val originDesc = if (topOrigins.isNotEmpty()) {
            "Swipe history shows affinity for ${topOrigins.joinToString(", ")}."
        } else {
            "Equal weighting across Latin, Celtic, Hebrew, Nordic, Greek, Arabic, and Japanese origins."
        }

        val styleDesc = if (topStyles.isNotEmpty()) {
            "Favoring names with traits: ${topStyles.joinToString(", ")}."
        } else {
            "Broad aesthetic exploration across classic, modern, and nature tags."
        }

        val partnerNote = "Your partner's picks stay secret until you like the same name — then it's a match!"

        return AlgorithmExplanation(
            primaryFactors = factors,
            originBiasDescription = originDesc,
            styleBiasDescription = styleDesc,
            matchRecommendationNote = partnerNote
        )
    }
}
