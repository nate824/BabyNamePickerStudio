package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BoyPrimary
import com.example.ui.theme.GirlPrimary
import com.example.ui.theme.PrimaryRose

@Composable
fun PartnerHeaderBar(
    activeUserId: String,
    partner1Name: String,
    partner2Name: String,
    pairCode: String,
    onSwitchUser: (String) -> Unit,
    onRenamePartners: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPairModal by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    var copiedToast by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("partner_header_bar"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile switcher pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f))
                    .padding(4.dp)
            ) {
                // Partner 1 Pill
                val isP1Active = activeUserId == "partner_1"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isP1Active) GirlPrimary else Color.Transparent,
                    modifier = Modifier
                        .clickable { onSwitchUser("partner_1") }
                        .testTag("partner_1_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(if (isP1Active) Color.White else GirlPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = partner1Name.take(1),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isP1Active) GirlPrimary else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = partner1Name,
                            fontSize = 12.sp,
                            fontWeight = if (isP1Active) FontWeight.Bold else FontWeight.Normal,
                            color = if (isP1Active) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Switch icon
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Switch profile",
                    tint = Color.Gray,
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(16.dp)
                )

                // Partner 2 Pill
                val isP2Active = activeUserId == "partner_2"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isP2Active) BoyPrimary else Color.Transparent,
                    modifier = Modifier
                        .clickable { onSwitchUser("partner_2") }
                        .testTag("partner_2_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(if (isP2Active) Color.White else BoyPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = partner2Name.take(1),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isP2Active) BoyPrimary else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = partner2Name,
                            fontSize = 12.sp,
                            fontWeight = if (isP2Active) FontWeight.Bold else FontWeight.Normal,
                            color = if (isP2Active) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Sync Code / Pair modal trigger
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryRose.copy(alpha = 0.1f),
                modifier = Modifier
                    .clickable { showPairModal = true }
                    .testTag("partner_pair_code_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Pairing Code",
                        tint = PrimaryRose,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = pairCode,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRose
                    )
                }
            }
        }
    }

    if (showPairModal) {
        var editP1 by remember { mutableStateOf(partner1Name) }
        var editP2 by remember { mutableStateOf(partner2Name) }

        AlertDialog(
            onDismissRequest = { showPairModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = PrimaryRose)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Partner Sync & Profiles", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Share this invite code with your partner to link lists in real-time:",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.LightGray.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = pairCode,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryRose
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(pairCode))
                                    copiedToast = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = PrimaryRose
                                )
                            }
                        }
                    }

                    if (copiedToast) {
                        Text(
                            text = "✓ Code copied to clipboard!",
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Customize Partner Names",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = editP1,
                        onValueChange = { editP1 = it },
                        label = { Text("Partner 1 Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editP2,
                        onValueChange = { editP2 = it },
                        label = { Text("Partner 2 Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenamePartners(editP1.trim().ifBlank { "Alex" }, editP2.trim().ifBlank { "Sam" })
                        showPairModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose)
                ) {
                    Text("Save & Close")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPairModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
