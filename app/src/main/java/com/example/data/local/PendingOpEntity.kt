package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A local change waiting to be pushed to the sync server, replayed in insertion order. */
@Entity(tableName = "pending_ops")
data class PendingOpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SWIPE = "swipe"
        const val UNSWIPE = "unswipe"
        const val ADD_NAME = "add_name"
        const val MATCH_PATCH = "match_patch"
        const val MATCH_DISMISS = "match_dismiss"
    }
}
