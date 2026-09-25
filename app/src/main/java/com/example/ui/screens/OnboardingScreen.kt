package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.session.Session
import com.example.ui.theme.PrimaryRose

/** First-run flow: pick a display name (registers this phone), then link with a partner. */
@Composable
fun OnboardingScreen(
    session: Session,
    busy: Boolean,
    onRegister: (String) -> Unit,
    onCreatePairCode: () -> Unit,
    onJoinPartner: (String) -> Unit,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = PrimaryRose, modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Kindred", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Serif)
            Text("Pick a baby name together", fontSize = 15.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))

            if (!session.isRegistered) {
                NameStep(busy = busy, onRegister = onRegister)
            } else {
                PairStep(
                    session = session,
                    busy = busy,
                    onCreatePairCode = onCreatePairCode,
                    onJoinPartner = onJoinPartner,
                    onFinish = onFinish
                )
            }
        }
    }
}

@Composable
private fun NameStep(busy: Boolean, onRegister: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    Text("What's your name?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "Your partner will see this when you're linked.",
        fontSize = 13.sp,
        color = Color.Gray,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = name,
        onValueChange = { name = it.take(40) },
        placeholder = { Text("e.g. Nate") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_name_input")
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { onRegister(name) },
        enabled = !busy && name.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("onboarding_continue_button")
    ) {
        if (busy) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        else Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PairStep(
    session: Session,
    busy: Boolean,
    onCreatePairCode: () -> Unit,
    onJoinPartner: (String) -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var code by rememberSaveable { mutableStateOf("") }

    Text("Hi ${session.myName}! Link with your partner", fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "One of you creates a code, the other enters it. You'll see each other's picks and get shared matches.",
        fontSize = 13.sp,
        color = Color.Gray,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))

    if (session.isPaired) {
        Text("🎉 Linked with ${session.partnerName}!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryRose)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onFinish,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Start swiping", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        return
    }

    if (session.pairCode != null) {
        Surface(shape = RoundedCornerShape(16.dp), color = PrimaryRose.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Your pair code", fontSize = 12.sp, color = Color.Gray)
                Text(
                    session.pairCode,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryRose,
                    modifier = Modifier.testTag("onboarding_pair_code")
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Let's pick baby names together on Kindred! Our pair code: ${session.pairCode}")
                    }
                    context.startActivity(Intent.createChooser(send, "Share pair code"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send to partner")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = PrimaryRose)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Waiting for them to join…", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    } else {
        Button(
            onClick = onCreatePairCode,
            enabled = !busy,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_create_code_button")
        ) { Text("Create a pair code", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Text("Partner already has a code?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase().take(7) },
            placeholder = { Text("ABC-234") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("onboarding_join_input")
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { onJoinPartner(code) },
            enabled = !busy && code.count { it.isLetterOrDigit() } == 6,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose),
            modifier = Modifier.height(52.dp).testTag("onboarding_join_button")
        ) { Text("Join") }
    }

    Spacer(modifier = Modifier.height(20.dp))
    TextButton(onClick = onFinish) {
        Text("Skip for now — link later from the top bar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
