package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBabyNames(names: List<BabyNameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBabyName(name: BabyNameEntity)

    @Query("SELECT * FROM baby_names")
    fun getAllBabyNames(): Flow<List<BabyNameEntity>>

    @Query("SELECT * FROM baby_names WHERE id = :id LIMIT 1")
    suspend fun getBabyNameById(id: String): BabyNameEntity?

    @Query("SELECT COUNT(*) FROM baby_names")
    suspend fun getBabyNamesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipe(swipe: SwipeEntity)

    @Query("DELETE FROM swipes WHERE userId = :userId AND nameId = :nameId")
    suspend fun deleteSwipe(userId: String, nameId: String)

    @Query("DELETE FROM swipes WHERE userId = :userId AND isLiked = 0")
    suspend fun deleteDislikedSwipesForUser(userId: String)

    @Query("SELECT * FROM swipes WHERE userId = :userId")
    fun getSwipesForUser(userId: String): Flow<List<SwipeEntity>>

    @Query("SELECT * FROM swipes WHERE userId = :userId AND isLiked = 1")
    fun getLikedSwipesForUser(userId: String): Flow<List<SwipeEntity>>

    @Query("SELECT * FROM swipes")
    fun getAllSwipes(): Flow<List<SwipeEntity>>

    @Query("SELECT * FROM swipes WHERE userId = :userId AND nameId = :nameId LIMIT 1")
    suspend fun getSwipe(userId: String, nameId: String): SwipeEntity?

    @Query("SELECT * FROM swipes WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSwipeForUser(userId: String): SwipeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMatch(match: MatchEntity)

    @Query("SELECT * FROM matches ORDER BY matchedAt DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE nameId = :nameId LIMIT 1")
    suspend fun getMatch(nameId: String): MatchEntity?

    @Query("UPDATE matches SET notes = :notes WHERE nameId = :nameId")
    suspend fun updateMatchNotes(nameId: String, notes: String)

    @Query("UPDATE matches SET rating = :rating WHERE nameId = :nameId")
    suspend fun updateMatchRating(nameId: String, rating: Int)

    @Query("DELETE FROM matches WHERE nameId = :nameId")
    suspend fun deleteMatch(nameId: String)
}
