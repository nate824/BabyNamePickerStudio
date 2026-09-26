package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BabyName
import com.example.data.model.Gender
import com.example.ui.theme.BoyCardBg
import com.example.ui.theme.BoyCardBorder
import com.example.ui.theme.BoyPrimary
import com.example.ui.theme.BoySecondary
import com.example.ui.theme.BoyTagBg
import com.example.ui.theme.DislikeRed
import com.example.ui.theme.GirlCardBg
import com.example.ui.theme.GirlCardBorder
import com.example.ui.theme.GirlPrimary
import com.example.ui.theme.GirlSecondary
import com.example.ui.theme.GirlTagBg
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.UnisexCardBg
import com.example.ui.theme.UnisexCardBorder
import com.example.ui.theme.UnisexPrimary
import com.example.ui.theme.UnisexSecondary
import com.example.ui.theme.UnisexTagBg
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwipeCard(
    babyName: BabyName,
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    // Visual theme selection according to baby gender
    val (cardBg, cardBorder, primaryColor, secondaryColor, tagBg) = when (babyName.gender) {
        Gender.GIRL -> {
            CardThemeColors(
                bg = GirlCardBg,
                border = GirlCardBorder,
                primary = GirlPrimary,
                secondary = GirlSecondary,
                tagBg = GirlTagBg
            )
        }
        Gender.BOY -> {
            CardThemeColors(
                bg = BoyCardBg,
                border = BoyCardBorder,
                primary = BoyPrimary,
                secondary = BoySecondary,
                tagBg = BoyTagBg
            )
        }
        Gender.UNISEX -> {
            CardThemeColors(
                bg = UnisexCardBg,
                border = UnisexCardBorder,
                primary = UnisexPrimary,
                secondary = UnisexSecondary,
                tagBg = UnisexTagBg
            )
        }
    }

    // Drag gesture and rotation
    val rotation = (offsetX.value / 25f).coerceIn(-20f, 20f)
    val swipeThreshold = 300f

    Box(
        modifier = modifier
            .testTag("swipe_card_${babyName.id}")
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .rotate(rotation)
            .pointerInput(babyName.id) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetX.value > swipeThreshold) {
                                offsetX.animateTo(1200f, tween(250))
                                onSwipeRight()
                            } else if (offsetX.value < -swipeThreshold) {
                                offsetX.animateTo(-1200f, tween(250))
                                onSwipeLeft()
                            } else {
                                offsetX.animateTo(0f, tween(200))
                                offsetY.animateTo(0f, tween(200))
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y * 0.4f)
                        }
                    }
                )
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, cardBorder, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Gender Tag
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = tagBg
                        ) {
                            Text(
                                text = when (babyName.gender) {
                                    Gender.GIRL -> "🌸 Girl"
                                    Gender.BOY -> "⚓ Boy"
                                    Gender.UNISEX -> "✨ Unisex"
                                },
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = tagBg
                        ) {
                            Text(
                                text = "${babyName.length} letters • Rank #${babyName.popularityRank}",
                                color = secondaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Centerpiece: Baby Name & Pronunciation
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = babyName.name,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = primaryColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(tagBg)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Pronunciation",
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = babyName.pronunciation,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = secondaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Origin Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                    ) {
                        Text(
                            text = "Origin: ${babyName.origin}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                // Meaning & Style tags
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "“${babyName.meaning}”",
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Normal,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        maxItemsInEachRow = 3
                    ) {
                        babyName.styleTags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = tagBg,
                                modifier = Modifier.padding(3.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = secondaryColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Subtle swipe hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "👈 Swipe left to pass",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Swipe right to like 👉",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Animated "LIKE" stamp when dragging right
        if (offsetX.value > 60f) {
            val stampAlpha = ((offsetX.value - 60f) / 180f).coerceIn(0f, 1f)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(32.dp)
                    .rotate(-15f),
                shape = RoundedCornerShape(12.dp),
                color = LikeGreen.copy(alpha = 0.9f * stampAlpha),
                border = androidx.compose.foundation.BorderStroke(3.dp, Color.White.copy(alpha = stampAlpha))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = stampAlpha)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIKE",
                        color = Color.White.copy(alpha = stampAlpha),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Animated "NOPE" stamp when dragging left
        if (offsetX.value < -60f) {
            val stampAlpha = ((-offsetX.value - 60f) / 180f).coerceIn(0f, 1f)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(32.dp)
                    .rotate(15f),
                shape = RoundedCornerShape(12.dp),
                color = DislikeRed.copy(alpha = 0.9f * stampAlpha),
                border = androidx.compose.foundation.BorderStroke(3.dp, Color.White.copy(alpha = stampAlpha))
            ) {
                Text(
                    text = "PASS",
                    color = Color.White.copy(alpha = stampAlpha),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private data class CardThemeColors(
    val bg: Color,
    val border: Color,
    val primary: Color,
    val secondary: Color,
    val tagBg: Color
)
