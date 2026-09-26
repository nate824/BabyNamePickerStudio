package com.example.data.seed

import android.content.Context
import com.example.data.model.BabyName
import com.example.data.model.Gender
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Large bundled name list (assets/catalog_girls.json): recent US Social Security popularity data,
 * with origin/meaning/pronunciation filled in once by Claude. See scripts/catalog/README.md.
 */
object NameCatalog {
    const val ASSET = "catalog_girls.json"

    @Serializable
    private data class Entry(
        val name: String,
        val origin: String,
        val meaning: String,
        val pronunciation: String,
        val styleTags: List<String>,
        val rank: Int,
        val unisex: Boolean = false
    )

    private val json = Json { ignoreUnknownKeys = true }

    fun load(context: Context): List<BabyName> {
        val text = runCatching { context.assets.open(ASSET).bufferedReader().use { it.readText() } }.getOrNull()
            ?: return emptyList()
        return parse(text)
    }

    fun parse(text: String): List<BabyName> =
        json.decodeFromString<List<Entry>>(text).map { e ->
            BabyName(
                id = "ssa_g_" + e.name.lowercase(),
                name = e.name,
                gender = if (e.unisex) Gender.UNISEX else Gender.GIRL,
                origin = e.origin,
                meaning = e.meaning,
                pronunciation = e.pronunciation,
                popularityRank = e.rank,
                styleTags = e.styleTags
            )
        }
}
