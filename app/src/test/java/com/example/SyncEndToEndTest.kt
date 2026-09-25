package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.remote.ApiClient
import com.example.data.repository.BabyNameRepository
import com.example.data.session.SessionStore
import com.example.data.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

/**
 * Two simulated phones talking to a real sync server.
 * Run the server locally (cd server && npm run dev) and set SYNC_E2E_URL=http://127.0.0.1:4180.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SyncEndToEndTest {

    private val baseUrl: String? = System.getenv("SYNC_E2E_URL")

    private class Phone(context: Context, baseUrl: String) {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val session = SessionStore(context, "e2e_" + UUID.randomUUID())
        val api = ApiClient(baseUrl, tokenProvider = { session.session.value.token })
        val repo = BabyNameRepository(db.appDao())
        val sync = SyncManager(db.appDao(), api, session, CoroutineScope(SupervisorJob() + Dispatchers.IO))
        val id: String get() = session.session.value.deviceId!!
        val partnerId: String get() = session.session.value.partnerId ?: "none"

        suspend fun swipe(nameId: String, liked: Boolean): Boolean {
            val match = repo.recordSwipe(id, partnerId, nameId, liked)
            sync.enqueueSwipe(nameId, liked, System.currentTimeMillis())
            return match
        }
    }

    @Test
    fun `two phones pair, sync swipes, and both see the match`() = runBlocking {
        assumeTrue("SYNC_E2E_URL not set", baseUrl != null)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val nate = Phone(context, baseUrl!!)
        val sarah = Phone(context, baseUrl)
        nate.repo.seedDatabaseIfEmpty()
        sarah.repo.seedDatabaseIfEmpty()

        nate.sync.register("Nate")
        sarah.sync.register("Sarah")
        val code = nate.sync.createPairCode()
        sarah.sync.joinPartner(code.lowercase())
        assertTrue(nate.sync.sync())

        assertEquals("Sarah", nate.session.session.value.partnerName)
        assertEquals("Nate", sarah.session.session.value.partnerName)

        // Nate likes Liam; after syncing, it is at the top of Sarah's deck
        nate.swipe("boy_liam", true)
        assertTrue(nate.sync.sync())
        assertTrue(sarah.sync.sync())
        val sarahQueue = sarah.repo.computeQueueForUser(
            sarah.id, sarah.partnerId, null,
            com.example.data.model.LengthPreference.ANY, null,
            com.example.data.model.AlgorithmConfig(isEnabled = false)
        )
        assertEquals("boy_liam", sarahQueue.first().id)

        // Sarah likes it too: she detects the match locally, and Nate gets it via sync
        val nateNewMatches = async { withTimeout(10_000) { nate.sync.newMatches.first() } }
        assertTrue("Sarah should see the match immediately", sarah.swipe("boy_liam", true))
        assertTrue(sarah.sync.sync())
        assertTrue(nate.sync.sync())
        assertEquals(listOf("boy_liam"), nateNewMatches.await())
        assertNotNull(nate.db.appDao().getMatch("boy_liam"))

        // A custom name Sarah adds shows up in Nate's database
        val (added, _) = sarah.repo.addCustomName("Zephyr", com.example.data.model.Gender.BOY, "Greek", "West wind", "ZEF-er", listOf("Unique"), sarah.id, sarah.partnerId)
        sarah.sync.enqueueAddName(
            com.example.data.remote.NewNameRequest(added.id, added.name, added.gender.name, added.origin, added.meaning, added.pronunciation, added.styleTags)
        )
        sarah.sync.enqueueSwipe(added.id, true, System.currentTimeMillis())
        assertTrue(sarah.sync.sync())
        assertTrue(nate.sync.sync())
        assertEquals("Zephyr", nate.db.appDao().getBabyNameById(added.id)?.name)

        // Notes are shared
        nate.repo.updateMatchNotes("boy_liam", "Middle name: James")
        nate.sync.enqueueMatchPatch("boy_liam", notes = "Middle name: James")
        assertTrue(nate.sync.sync())
        assertTrue(sarah.sync.sync())
        assertEquals("Middle name: James", sarah.db.appDao().getMatch("boy_liam")?.notes)

        // Undo removes the match on both sides
        sarah.repo.removeSwipe(sarah.id, "boy_liam")
        sarah.sync.enqueueUnswipe("boy_liam")
        assertTrue(sarah.sync.sync())
        assertTrue(nate.sync.sync())
        assertEquals(null, nate.db.appDao().getMatch("boy_liam"))
    }

    @Test
    fun `swipes made offline are kept and pushed later`() = runBlocking {
        assumeTrue("SYNC_E2E_URL not set", baseUrl != null)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val phone = Phone(context, baseUrl!!)
        phone.sync.register("Solo")

        // Point a second SyncManager for the same phone at a dead server to simulate no signal
        val offlineApi = ApiClient("http://127.0.0.1:9", tokenProvider = { phone.session.session.value.token })
        val offline = SyncManager(phone.db.appDao(), offlineApi, phone.session, CoroutineScope(Dispatchers.IO))
        phone.repo.recordSwipe(phone.id, "none", "girl_emma", true)
        phone.db.appDao().insertPendingOp(com.example.data.local.PendingOpEntity(type = "swipe", payload = """{"nameId":"girl_emma","liked":true,"ts":${System.currentTimeMillis()}}"""))
        assertEquals(false, offline.sync())
        assertEquals(1, phone.db.appDao().getPendingOps().size)
        assertTrue(phone.db.appDao().getSwipe(phone.id, "girl_emma")!!.isLiked)

        assertTrue(phone.sync.sync())
        assertEquals(0, phone.db.appDao().getPendingOps().size)
        assertTrue(phone.db.appDao().getSwipe(phone.id, "girl_emma")!!.isLiked)
    }
}
