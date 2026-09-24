package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AlgorithmConfig
import com.example.data.model.Gender
import com.example.data.model.LengthPreference
import com.example.data.repository.BabyNameRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kindred Baby Names", appName)
  }

  @Test
  fun `verify collaborative partner queue and mutual match`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val repo = BabyNameRepository(db.appDao())

    // 1. Seed database
    repo.seedDatabaseIfEmpty()

    // 2. Partner 1 ("Alex") swipes on names and likes "boy_liam"
    val isMatchPartner1 = repo.recordSwipe(
      userId = "partner_1",
      partnerId = "partner_2",
      nameId = "boy_liam",
      isLiked = true
    )
    assertFalse("First swipe should not yet be a mutual match", isMatchPartner1)

    // 3. Check Partner 2's queue: "boy_liam" should be the FIRST name in queue!
    val partner2Queue = repo.computeQueueForUser(
      activeUserId = "partner_2",
      partnerId = "partner_1",
      genderFilter = null,
      lengthFilter = LengthPreference.ANY,
      popularityTierFilter = null,
      algorithmConfig = AlgorithmConfig(isEnabled = false)
    )

    assertTrue("Partner 2 queue should have names", partner2Queue.isNotEmpty())
    assertEquals("First name in Partner 2's session must be Alex's liked name!", "boy_liam", partner2Queue[0].id)

    // 4. Partner 2 ("Sam") also likes "boy_liam" -> Mutual Match!
    val isMatchPartner2 = repo.recordSwipe(
      userId = "partner_2",
      partnerId = "partner_1",
      nameId = "boy_liam",
      isLiked = true
    )
    assertTrue("Second swipe on same name must create a mutual match", isMatchPartner2)

    db.close()
  }

  @Test
  fun `verify add custom name automatically likes and queues for partner`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val repo = BabyNameRepository(db.appDao())
    repo.seedDatabaseIfEmpty()

    // Partner 1 adds a custom name
    val (addedName, isMatch) = repo.addCustomName(
      nameText = "Zephyr",
      gender = Gender.BOY,
      origin = "Greek",
      meaning = "West wind of spring",
      pronunciation = "ZEF-er",
      tags = listOf("Celestial", "Unique"),
      activeUserId = "partner_1",
      partnerId = "partner_2"
    )

    assertNotNull(addedName.id)
    assertFalse(isMatch)

    // Partner 2 queue should now have "Zephyr" at the top!
    val p2Queue = repo.computeQueueForUser(
      activeUserId = "partner_2",
      partnerId = "partner_1",
      genderFilter = null,
      lengthFilter = LengthPreference.ANY,
      popularityTierFilter = null,
      algorithmConfig = AlgorithmConfig()
    )

    assertEquals("Zephyr", p2Queue[0].name)

    db.close()
  }
}

