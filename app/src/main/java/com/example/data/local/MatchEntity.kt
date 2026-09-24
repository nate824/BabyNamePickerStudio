package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey
    val nameId: String,
    val matchedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val rating: Int = 5 // 1 to 5 stars
)
