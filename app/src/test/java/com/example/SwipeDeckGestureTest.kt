package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.example.data.model.BabyName
import com.example.data.model.Gender
import com.example.data.model.LengthPreference
import com.example.ui.screens.SwipeDeckScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w400dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SwipeDeckGestureTest {

    @get:Rule
    val compose = createComposeRule()

    private fun name(id: String, n: String) =
        BabyName(id, n, Gender.GIRL, "Latin", "meaning", "say-$id", 10, listOf("Classic"))

    @Test
    fun `next card is on screen after a drag swipe`() {
        compose.setContent {
            var queue by remember { mutableStateOf(listOf(name("a", "Aurora"), name("b", "Beatrix"), name("c", "Clara"))) }
            SwipeDeckScreen(
                currentQueue = queue,
                partnerLikedNameIds = emptySet(),
                activeUserName = "Nate",
                partnerName = "Sarah",
                genderFilter = null,
                lengthFilter = LengthPreference.ANY,
                popularityFilter = null,
                isAlgoEnabled = true,
                onSelectGenderFilter = {},
                onSelectLengthFilter = {},
                onSelectPopularityFilter = {},
                onSwipeLeft = { n -> queue = queue - n },
                onSwipeRight = { n -> queue = queue - n },
                onUndo = {},
                onOpenAddName = {},
                onAskAi = {}
            )
        }

        repeat(2) { i ->
            val current = listOf("Aurora", "Beatrix")[i]
            compose.onNodeWithText(current).performTouchInput { swipeRight(startX = centerX, endX = centerX + 600f) }
            compose.waitForIdle()
        }

        val rootWidth = compose.onRoot().fetchSemanticsNode().boundsInRoot.width
        val cardBounds = compose.onNodeWithText("Clara").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Third card should be on screen after two drag swipes, but was at x=${cardBounds.left} (root width $rootWidth)",
            cardBounds.left >= 0f && cardBounds.right <= rootWidth
        )
    }
}
