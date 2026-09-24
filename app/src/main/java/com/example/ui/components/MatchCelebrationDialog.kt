package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BabyName
import com.example.data.model.Gender
import com.example.ui.theme.BoyPrimary
import com.example.ui.theme.GirlPrimary
import com.example.ui.theme.GoldStar
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.UnisexPrimary
import kotlin.random.Random

@Composable
fun MatchCelebrationDialog(
    babyName: BabyName,
    activeUserName: String,
    partnerName: String,
    onDismiss: () -> Unit,
    onViewSharedMatches: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.7f) }
    val confettiProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
        confettiProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = LinearEasing)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .testTag("match_celebration_dialog"),
            contentAlignment = Alignment.Center
        ) {
            // Animated confetti particles
            ConfettiEffect(progress = confettiProgress.value)

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .scale(scaleAnim.value)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Match Avatars Connected
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GirlPrimary,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = activeUserName.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(36.dp)
                                .background(PrimaryRose, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = BoyPrimary,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = partnerName.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "IT'S A MATCH! 🎉",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryRose,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "You and $partnerName both love",
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name Display Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (babyName.gender) {
                            Gender.GIRL -> Color(0xFFFFF1F2)
                            Gender.BOY -> Color(0xFFEFF6FF)
                            Gender.UNISEX -> Color(0xFFF5F3FF)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = babyName.name,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Serif,
                                color = when (babyName.gender) {
                                    Gender.GIRL -> GirlPrimary
                                    Gender.BOY -> BoyPrimary
                                    Gender.UNISEX -> UnisexPrimary
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "“${babyName.meaning}”",
                                fontSize = 14.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${babyName.origin} • ${babyName.pronunciation}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    Button(
                        onClick = onViewSharedMatches,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("view_shared_loves_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose)
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Shared Loves",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("keep_swiping_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Keep Swiping ✨",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfettiEffect(progress: Float) {
    val particles = remember {
        List(40) {
            ConfettiParticle(
                xStart = Random.nextFloat(),
                yStart = Random.nextFloat() * 0.3f,
                speed = 0.5f + Random.nextFloat() * 0.8f,
                size = 12f + Random.nextFloat() * 14f,
                color = listOf(
                    Color(0xFFE11D48),
                    Color(0xFF2563EB),
                    Color(0xFF10B981),
                    Color(0xFFF59E0B),
                    Color(0xFF8B5CF6)
                ).random()
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            val curY = (p.yStart + progress * p.speed) % 1f * h
            val curX = (p.xStart + (progress * 0.2f)) % 1f * w
            drawCircle(
                color = p.color,
                radius = p.size * (1f - progress * 0.2f),
                center = Offset(curX, curY)
            )
        }
    }
}

private data class ConfettiParticle(
    val xStart: Float,
    val yStart: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)
