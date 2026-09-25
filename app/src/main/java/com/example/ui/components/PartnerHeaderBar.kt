package com.example.ui.components

import android.content.Intent
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.session.Session
import com.example.data.sync.SyncStatus
import com.example.ui.theme.AccentHoney
import com.example.ui.theme.BoyPrimary
import com.example.ui.theme.GirlPrimary
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.PrimaryRose

@Composable
fun PartnerHeaderBar(
    session: Session,
    syncStatus: SyncStatus,
    busy: Boolean,
    onCreatePairCode: () -> Unit,
    onJoinPartner: (String) -> Unit,
    onUnlink: () -> Unit,
    onRename: (String) -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPairModal by remember { mutableStateOf(false) }

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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Avatar(session.myName.ifBlank { "You" }, GirlPrimary)
                if (session.isPaired) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = PrimaryRose,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(14.dp)
                    )
                    Avatar(session.partnerName ?: "Partner", BoyPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                SyncDot(syncStatus, onClick = onSyncNow)
            }

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
                        contentDescription = "Partner link",
                        tint = PrimaryRose,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            session.isPaired -> "Linked"
                            session.pairCode != null -> session.pairCode
                            else -> "Link partner"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRose
                    )
                }
            }
        }
    }

    if (showPairModal) {
        PartnerLinkDialog(
            session = session,
            busy = busy,
            onCreatePairCode = onCreatePairCode,
            onJoinPartner = onJoinPartner,
            onUnlink = onUnlink,
            onRename = onRename,
            onDismiss = { showPairModal = false }
        )
    }
}

@Composable
private fun Avatar(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = name.take(1).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun SyncDot(status: SyncStatus, onClick: () -> Unit) {
    val (color, label) = when (status) {
        SyncStatus.SYNCED -> LikeGreen to "Synced"
        SyncStatus.SYNCING -> AccentHoney to "Syncing"
        SyncStatus.OFFLINE -> Color.Gray to "Offline"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag("sync_status")
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun PartnerLinkDialog(
    session: Session,
    busy: Boolean,
    onCreatePairCode: () -> Unit,
    onJoinPartner: (String) -> Unit,
    onUnlink: () -> Unit,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var joinCode by remember { mutableStateOf("") }
    var myName by remember { mutableStateOf(session.myName) }
    var confirmUnlink by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = PrimaryRose)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Partner Sync", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (session.isPaired) {
                    Text(
                        text = "You're linked with ${session.partnerName}. Swipes, matches, notes, and added names sync between your phones.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else if (session.pairCode != null) {
                    Text(
                        text = "Have your partner open Kindred on their phone and enter this code:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.LightGray.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = session.pairCode,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryRose,
                                modifier = Modifier.testTag("pair_code_text")
                            )
                            Row {
                                IconButton(onClick = { clipboardManager.setText(AnnotatedString(session.pairCode)) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy code", tint = PrimaryRose)
                                }
                                IconButton(onClick = {
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Let's pick baby names together on Kindred! Our pair code: ${session.pairCode}")
                                    }
                                    context.startActivity(Intent.createChooser(send, "Share pair code"))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share code", tint = PrimaryRose)
                                }
                            }
                        }
                    }
                    Text(
                        text = "Waiting for your partner to join…",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    Text(
                        text = "Link with your partner so you both swipe the same deck and see shared favorites.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onCreatePairCode,
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_pair_code_button")
                    ) {
                        Text("Create a pair code")
                    }
                }

                if (!session.isPaired) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "…or enter your partner's code", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = joinCode,
                            onValueChange = { joinCode = it.uppercase().take(7) },
                            placeholder = { Text("ABC-234") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("join_code_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onJoinPartner(joinCode) },
                            enabled = !busy && joinCode.count { it.isLetterOrDigit() } == 6,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose),
                            modifier = Modifier.testTag("join_code_button")
                        ) {
                            Text("Join")
                        }
                    }
                }

                if (busy) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryRose)
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Your name", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = myName,
                        onValueChange = { myName = it.take(40) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onRename(myName) },
                        enabled = !busy && myName.isNotBlank() && myName.trim() != session.myName
                    ) {
                        Text("Save")
                    }
                }

                if (session.isPaired) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (confirmUnlink) {
                        Text(
                            text = "Unlink from ${session.partnerName}? You'll stop seeing each other's swipes and matches.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Row {
                            TextButton(onClick = { onUnlink(); onDismiss() }) { Text("Unlink", color = PrimaryRose) }
                            TextButton(onClick = { confirmUnlink = false }) { Text("Cancel") }
                        }
                    } else {
                        TextButton(onClick = { confirmUnlink = true }) { Text("Unlink partner", color = Color.Gray) }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
