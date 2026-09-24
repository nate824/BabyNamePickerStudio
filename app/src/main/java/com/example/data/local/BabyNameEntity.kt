package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BabyName
import com.example.data.model.Gender

@Entity(tableName = "baby_names")
data class BabyNameEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val gender: String, // "BOY", "GIRL", "UNISEX"
    val origin: String,
    val meaning: String,
    val pronunciation: String,
    val popularityRank: Int,
    val styleTags: String, // comma separated
    val isUserAdded: Boolean = false,
    val addedByUserId: String? = null
) {
    fun toDomain(): BabyName {
        val parsedGender = try {
            Gender.valueOf(gender)
        } catch (_: Exception) {
            Gender.UNISEX
        }
        val tags = if (styleTags.isBlank()) emptyList() else styleTags.split(",").map { it.trim() }
        return BabyName(
            id = id,
            name = name,
            gender = parsedGender,
            origin = origin,
            meaning = meaning,
            pronunciation = pronunciation,
            popularityRank = popularityRank,
            styleTags = tags,
            isUserAdded = isUserAdded,
            addedByUserId = addedByUserId
        )
    }

    companion object {
        fun fromDomain(domain: BabyName): BabyNameEntity {
            return BabyNameEntity(
                id = domain.id,
                name = domain.name,
                gender = domain.gender.name,
                origin = domain.origin,
                meaning = domain.meaning,
                pronunciation = domain.pronunciation,
                popularityRank = domain.popularityRank,
                styleTags = domain.styleTags.joinToString(","),
                isUserAdded = domain.isUserAdded,
                addedByUserId = domain.addedByUserId
            )
        }
    }
}
