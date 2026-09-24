package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BabyName
import com.example.data.model.Gender
import com.example.ui.theme.BoyCardBg
import com.example.ui.theme.BoyPrimary
import com.example.ui.theme.GirlCardBg
import com.example.ui.theme.GirlPrimary
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.UnisexCardBg
import com.example.ui.theme.UnisexPrimary

data class LikedNameWithPartnerStatus(
    val babyName: BabyName,
    val partnerLiked: Boolean?, // true: matched, false: passed, null: not swiped yet
    val timestamp: Long
)

@Composable
fun MyLikesScreen(
    likedNames: List<LikedNameWithPartnerStatus>,
    activeUserName: String,
    partnerName: String,
    onRemoveLike: (nameId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("my_likes_screen")
    ) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
            Text(
                text = "$activeUserName's Likes",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${likedNames.size} names chosen • Track if $partnerName matches with you",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        if (likedNames.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No likes yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Text(
                        text = "Swipe right on names in the Explore tab to save them here!",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(likedNames, key = { it.babyName.id }) { item ->
                    val babyName = item.babyName
                    val cardBg = when (babyName.gender) {
                        Gender.GIRL -> GirlCardBg
                        Gender.BOY -> BoyCardBg
                        Gender.UNISEX -> UnisexCardBg
                    }
                    val primaryColor = when (babyName.gender) {
                        Gender.GIRL -> GirlPrimary
                        Gender.BOY -> BoyPrimary
                        Gender.UNISEX -> UnisexPrimary
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = babyName.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = primaryColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${babyName.origin}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }

                                Text(
                                    text = "“${babyName.meaning}”",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                                )

                                // Partner Status Pill
                                when (item.partnerLiked) {
                                    true -> {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = LikeGreen.copy(alpha = 0.2f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = LikeGreen,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Match! Both love $partnerName",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF065F46)
                                                )
                                            }
                                        }
                                    }
                                    false -> {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.LightGray.copy(alpha = 0.3f)
                                        ) {
                                            Text(
                                                text = "$partnerName passed",
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    null -> {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryRose.copy(alpha = 0.12f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.HourglassEmpty,
                                                    contentDescription = null,
                                                    tint = PrimaryRose,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Waiting for $partnerName's turn",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = PrimaryRose
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onRemoveLike(babyName.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove like",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
