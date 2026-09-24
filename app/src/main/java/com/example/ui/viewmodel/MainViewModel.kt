package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AlgorithmConfig
import com.example.data.model.AlgorithmExplanation
import com.example.data.model.BabyName
import com.example.data.model.Gender
import com.example.data.model.LengthPreference
import com.example.data.model.PopularityTier
import com.example.data.repository.BabyNameRepository
import com.example.data.repository.MatchWithDetails
import com.example.ui.screens.LikedNameWithPartnerStatus
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BabyNameRepository

    // Partner IDs and Names
    val activeUserId = MutableStateFlow("partner_1")
    val partner1Name = MutableStateFlow("Alex")
    val partner2Name = MutableStateFlow("Sam")
    val pairCode = MutableStateFlow("KIND-7824")

    // Filter states
    val genderFilter = MutableStateFlow<Gender?>(null)
    val lengthFilter = MutableStateFlow(LengthPreference.ANY)
    val popularityFilter = MutableStateFlow<PopularityTier?>(null)

    // Algorithm Config & Explanation
    val algorithmConfig = MutableStateFlow(AlgorithmConfig())
    val algorithmExplanation = MutableStateFlow(
        AlgorithmExplanation(
            primaryFactors = listOf("Collaborative Priority: Queuing partner likes directly to your next session."),
            originBiasDescription = "Balanced multicultural distribution.",
            styleBiasDescription = "Broad aesthetic exploration.",
            matchRecommendationNote = "Partner priority active: Any name your partner likes is instantly queued to your top 10 next cards."
        )
    )

    // Main Swipe Queue
    private val _currentQueue = MutableStateFlow<List<BabyName>>(emptyList())
    val currentQueue: StateFlow<List<BabyName>> = _currentQueue.asStateFlow()

    // Partner's liked name IDs for active user's visual indicators
    private val _partnerLikedNameIds = MutableStateFlow<Set<String>>(emptySet())
    val partnerLikedNameIds: StateFlow<Set<String>> = _partnerLikedNameIds.asStateFlow()

    // Shared Matches
    private val _sharedMatches = MutableStateFlow<List<MatchWithDetails>>(emptyList())
    val sharedMatches: StateFlow<List<MatchWithDetails>> = _sharedMatches.asStateFlow()

    // My Likes for active user
    private val _myLikes = MutableStateFlow<List<LikedNameWithPartnerStatus>>(emptyList())
    val myLikes: StateFlow<List<LikedNameWithPartnerStatus>> = _myLikes.asStateFlow()

    // Match dialog trigger
    val matchCelebrationBabyName = MutableStateFlow<BabyName?>(null)

    init {
        val db = AppDatabase.getInstance(application)
        repository = BabyNameRepository(db.appDao())
        NotificationHelper.createNotificationChannel(application)

        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            refreshMatches()
            reloadQueue()
            reloadMyLikes()
            refreshExplanation()
        }

        // Listen for database changes to keep matches synchronized in real-time
        viewModelScope.launch {
            repository.getAllMatches().collect { matches ->
                _sharedMatches.value = matches
            }
        }
    }

    fun getPartnerId(userId: String): String {
        return if (userId == "partner_1") "partner_2" else "partner_1"
    }

    fun getActivePartnerName(): String {
        return if (activeUserId.value == "partner_1") partner1Name.value else partner2Name.value
    }

    fun getInactivePartnerName(): String {
        return if (activeUserId.value == "partner_1") partner2Name.value else partner1Name.value
    }

    fun switchActiveUser(newUserId: String) {
        if (activeUserId.value == newUserId) return
        activeUserId.value = newUserId
        viewModelScope.launch {
            reloadQueue()
            reloadMyLikes()
            refreshExplanation()
        }
    }

    fun renamePartners(name1: String, name2: String) {
        partner1Name.value = name1
        partner2Name.value = name2
    }

    fun setGenderFilter(gender: Gender?) {
        genderFilter.value = gender
        viewModelScope.launch { reloadQueue() }
    }

    fun setLengthFilter(lengthPref: LengthPreference) {
        lengthFilter.value = lengthPref
        viewModelScope.launch { reloadQueue() }
    }

    fun setPopularityFilter(tier: PopularityTier?) {
        popularityFilter.value = tier
        viewModelScope.launch { reloadQueue() }
    }

    fun updateAlgorithmConfig(config: AlgorithmConfig) {
        algorithmConfig.value = config
        viewModelScope.launch {
            reloadQueue()
            refreshExplanation()
        }
    }

    fun resetAlgorithmDefaults() {
        algorithmConfig.value = AlgorithmConfig()
        viewModelScope.launch {
            reloadQueue()
            refreshExplanation()
        }
    }

    fun swipe(babyName: BabyName, isLiked: Boolean, context: Context) {
        val currentActive = activeUserId.value
        val partnerId = getPartnerId(currentActive)

        // Optimistically remove from queue immediately for 60fps responsiveness
        _currentQueue.value = _currentQueue.value.filter { it.id != babyName.id }

        viewModelScope.launch {
            val isMutualMatch = repository.recordSwipe(
                userId = currentActive,
                partnerId = partnerId,
                nameId = babyName.id,
                isLiked = isLiked
            )

            if (isMutualMatch) {
                // Send system push notification!
                NotificationHelper.sendMatchNotification(
                    context = context,
                    name = babyName.name,
                    partnerName = getInactivePartnerName(),
                    meaning = babyName.meaning
                )
                // Trigger in-app celebration dialog!
                matchCelebrationBabyName.value = babyName
                refreshMatches()
            }

            reloadMyLikes()
            refreshExplanation()
        }
    }

    fun undoSwipe() {
        val currentActive = activeUserId.value
        viewModelScope.launch {
            val revertedName = repository.undoLastSwipe(currentActive)
            if (revertedName != null) {
                reloadQueue()
                reloadMyLikes()
                refreshMatches()
            }
        }
    }

    fun addCustomName(
        name: String,
        gender: Gender,
        origin: String,
        meaning: String,
        pronunciation: String,
        tags: List<String>,
        context: Context
    ) {
        val currentActive = activeUserId.value
        val partnerId = getPartnerId(currentActive)

        viewModelScope.launch {
            val (addedName, isMatch) = repository.addCustomName(
                nameText = name,
                gender = gender,
                origin = origin,
                meaning = meaning,
                pronunciation = pronunciation,
                tags = tags,
                activeUserId = currentActive,
                partnerId = partnerId
            )

            if (isMatch) {
                NotificationHelper.sendMatchNotification(
                    context = context,
                    name = addedName.name,
                    partnerName = getInactivePartnerName(),
                    meaning = addedName.meaning
                )
                matchCelebrationBabyName.value = addedName
                refreshMatches()
            }

            reloadQueue()
            reloadMyLikes()
        }
    }

    fun updateMatchRating(nameId: String, rating: Int) {
        viewModelScope.launch {
            repository.updateMatchRating(nameId, rating)
            refreshMatches()
        }
    }

    fun updateMatchNotes(nameId: String, notes: String) {
        viewModelScope.launch {
            repository.updateMatchNotes(nameId, notes)
            refreshMatches()
        }
    }

    fun deleteMatch(nameId: String) {
        viewModelScope.launch {
            repository.deleteMatch(nameId)
            refreshMatches()
        }
    }

    fun dismissMatchCelebration() {
        matchCelebrationBabyName.value = null
    }

    private suspend fun reloadQueue() {
        val currentActive = activeUserId.value
        val partnerId = getPartnerId(currentActive)

        // Get partner's liked names
        val partnerSwipes = repository.getSwipesForUser(partnerId).first()
        val partnerLikedIds = partnerSwipes.filter { it.isLiked }.map { it.nameId }.toSet()
        _partnerLikedNameIds.value = partnerLikedIds

        val queue = repository.computeQueueForUser(
            activeUserId = currentActive,
            partnerId = partnerId,
            genderFilter = genderFilter.value,
            lengthFilter = lengthFilter.value,
            popularityTierFilter = popularityFilter.value,
            algorithmConfig = algorithmConfig.value
        )
        _currentQueue.value = queue
    }

    private suspend fun reloadMyLikes() {
        val currentActive = activeUserId.value
        val partnerId = getPartnerId(currentActive)

        val allNamesMap = repository.getAllNames().first().associateBy { it.id }
        val userLikedSwipes = repository.getSwipesForUser(currentActive).first().filter { it.isLiked }
        val partnerSwipes = repository.getSwipesForUser(partnerId).first()
        val partnerSwipeMap = partnerSwipes.associate { it.nameId to it.isLiked }

        val items = userLikedSwipes.sortedByDescending { it.timestamp }.mapNotNull { swipe ->
            val name = allNamesMap[swipe.nameId] ?: return@mapNotNull null
            val partnerVote = partnerSwipeMap[swipe.nameId]
            LikedNameWithPartnerStatus(
                babyName = name,
                partnerLiked = partnerVote,
                timestamp = swipe.timestamp
            )
        }
        _myLikes.value = items
    }

    private suspend fun refreshMatches() {
        val matches = repository.getAllMatches().first()
        _sharedMatches.value = matches
    }

    private suspend fun refreshExplanation() {
        val currentActive = activeUserId.value
        val partnerId = getPartnerId(currentActive)
        val exp = repository.getAlgorithmExplanation(currentActive, partnerId, algorithmConfig.value)
        algorithmExplanation.value = exp
    }
}
