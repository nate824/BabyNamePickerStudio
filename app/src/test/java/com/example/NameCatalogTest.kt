package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Gender
import com.example.data.repository.BabyNameRepository
import com.example.data.seed.NameCatalog
import com.example.data.seed.SeedBabyNames
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NameCatalogTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `bundled catalog is complete and does not overlap the seed names`() {
        val catalog = NameCatalog.load(context)
        assertTrue("expected ~1500 catalog names, got ${catalog.size}", catalog.size >= 1400)
        assertEquals("duplicate ids", catalog.size, catalog.map { it.id }.toSet().size)

        val seedNames = SeedBabyNames.initialNames.map { it.name.lowercase() }.toSet()
        val overlap = catalog.filter { it.name.lowercase() in seedNames }
        assertTrue("overlaps seed names: ${overlap.take(5).map { it.name }}", overlap.isEmpty())

        catalog.forEach { n ->
            assertTrue("incomplete entry: $n", n.origin.isNotBlank() && n.meaning.isNotBlank() && n.pronunciation.isNotBlank())
            assertTrue("no tags: ${n.name}", n.styleTags.isNotEmpty())
            assertTrue("not a girl/unisex name: ${n.name}", n.gender != Gender.BOY)
        }
    }

    @Test
    fun `catalog names reach the deck without disturbing existing swipes`() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val repo = BabyNameRepository(db.appDao())
        repo.seedDatabaseIfEmpty()
        repo.recordSwipe("me", "partner", "girl_olivia", true)

        val catalog = NameCatalog.load(context)
        repo.seedDatabaseIfEmpty(catalog)

        assertEquals(SeedBabyNames.initialNames.size + catalog.size, db.appDao().getBabyNamesCount())
        assertTrue(db.appDao().getSwipe("me", "girl_olivia")!!.isLiked)
        val names = repo.getAllNames().first().map { it.id }.toSet()
        assertTrue(catalog.all { it.id in names })
        db.close()
    }
}
