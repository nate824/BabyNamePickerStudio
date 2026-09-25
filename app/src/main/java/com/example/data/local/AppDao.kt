package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT nameId FROM matches")
    suspend fun getAllMatchIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipes(swipes: List<SwipeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBabyNames(names: List<BabyNameEntity>)

    @Query("DELETE FROM swipes")
    suspend fun deleteAllSwipes()

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()

    /** Replace the local mirror of shared state with what the server says. */
    @Transaction
    suspend fun replaceSharedState(swipes: List<SwipeEntity>, matches: List<MatchEntity>, names: List<BabyNameEntity>) {
        upsertBabyNames(names)
        deleteAllSwipes()
        insertSwipes(swipes)
        deleteAllMatches()
        insertMatches(matches)
    }

    @Insert
    suspend fun insertPendingOp(op: PendingOpEntity)

    @Query("SELECT * FROM pending_ops ORDER BY id")
    suspend fun getPendingOps(): List<PendingOpEntity>

    @Query("SELECT COUNT(*) FROM pending_ops")
    fun pendingOpCount(): Flow<Int>

    @Query("DELETE FROM pending_ops WHERE id IN (:ids)")
    suspend fun deletePendingOps(ids: List<Long>)
}
