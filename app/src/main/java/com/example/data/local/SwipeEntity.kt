package com.example.data.local

import androidx.room.Entity

@Entity(
    tableName = "swipes",
    primaryKeys = ["userId", "nameId"]
)
data class SwipeEntity(
    val userId: String,
    val nameId: String,
    val isLiked: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
