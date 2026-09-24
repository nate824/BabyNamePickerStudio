package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddNameDialog
import com.example.ui.components.MatchCelebrationDialog
import com.example.ui.components.PartnerHeaderBar
import com.example.ui.screens.AlgorithmTuneScreen
import com.example.ui.screens.MyLikesScreen
import com.example.ui.screens.SharedMatchesScreen
import com.example.ui.screens.SwipeDeckScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryRose
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

enum class AppTab(val title: String) {
    SWIPE("Explore"),
    MATCHES("Shared"),
    MY_LIKES("My Likes"),
    ALGO("AI Tune")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showAddNameDialog by remember { mutableStateOf(false) }

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _ -> }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // State collections
    val activeUserId by viewModel.activeUserId.collectAsState()
    val partner1Name by viewModel.partner1Name.collectAsState()
    val partner2Name by viewModel.partner2Name.collectAsState()
    val pairCode by viewModel.pairCode.collectAsState()

    val currentQueue by viewModel.currentQueue.collectAsState()
    val partnerLikedIds by viewModel.partnerLikedNameIds.collectAsState()
    val sharedMatches by viewModel.sharedMatches.collectAsState()
    val myLikes by viewModel.myLikes.collectAsState()

    val genderFilter by viewModel.genderFilter.collectAsState()
    val lengthFilter by viewModel.lengthFilter.collectAsState()
    val popularityFilter by viewModel.popularityFilter.collectAsState()

    val algoConfig by viewModel.algorithmConfig.collectAsState()
    val algoExplanation by viewModel.algorithmExplanation.collectAsState()

    val matchCelebrationName by viewModel.matchCelebrationBabyName.collectAsState()

    val activeUserName = viewModel.getActivePartnerName()
    val partnerName = viewModel.getInactivePartnerName()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryRose,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Kindred",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Baby Name Picker",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showAddNameDialog = true },
                            modifier = Modifier.testTag("top_bar_add_name_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add custom baby name",
                                tint = PrimaryRose
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Partner switcher & sync bar
                PartnerHeaderBar(
                    activeUserId = activeUserId,
                    partner1Name = partner1Name,
                    partner2Name = partner2Name,
                    pairCode = pairCode,
                    onSwitchUser = { newId -> viewModel.switchActiveUser(newId) },
                    onRenamePartners = { p1, p2 -> viewModel.renamePartners(p1, p2) },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Swipe Deck
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Style, contentDescription = "Explore") },
                    label = { Text("Explore", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryRose,
                        selectedTextColor = PrimaryRose,
                        indicatorColor = PrimaryRose.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_explore")
                )

                // Tab 1: Shared Matches
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (sharedMatches.isNotEmpty()) {
                                    Badge(containerColor = PrimaryRose) {
                                        Text("${sharedMatches.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Shared Loves")
                        }
                    },
                    label = { Text("Shared", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryRose,
                        selectedTextColor = PrimaryRose,
                        indicatorColor = PrimaryRose.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_shared")
                )

                // Tab 2: My Likes
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (myLikes.isNotEmpty()) {
                                    Badge(containerColor = Color.Gray) {
                                        Text("${myLikes.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = "My Likes")
                        }
                    },
                    label = { Text("My Likes", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryRose,
                        selectedTextColor = PrimaryRose,
                        indicatorColor = PrimaryRose.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_my_likes")
                )

                // Tab 3: AI Tune
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (algoConfig.isEnabled) Icons.Default.AutoAwesome else Icons.Default.Psychology,
                            contentDescription = "AI Tune"
                        )
                    },
                    label = { Text("AI Tune", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryRose,
                        selectedTextColor = PrimaryRose,
                        indicatorColor = PrimaryRose.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_ai_tune")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    SwipeDeckScreen(
                        currentQueue = currentQueue,
                        partnerLikedNameIds = partnerLikedIds,
                        activeUserName = activeUserName,
                        partnerName = partnerName,
                        genderFilter = genderFilter,
                        lengthFilter = lengthFilter,
                        popularityFilter = popularityFilter,
                        isAlgoEnabled = algoConfig.isEnabled,
                        onSelectGenderFilter = { viewModel.setGenderFilter(it) },
                        onSelectLengthFilter = { viewModel.setLengthFilter(it) },
                        onSelectPopularityFilter = { viewModel.setPopularityFilter(it) },
                        onSwipeLeft = { babyName -> viewModel.swipe(babyName, isLiked = false, context = context) },
                        onSwipeRight = { babyName -> viewModel.swipe(babyName, isLiked = true, context = context) },
                        onUndo = { viewModel.undoSwipe() },
                        onOpenAddName = { showAddNameDialog = true },
                        onSwitchPartner = {
                            val otherId = viewModel.getPartnerId(activeUserId)
                            viewModel.switchActiveUser(otherId)
                        }
                    )
                }
                1 -> {
                    SharedMatchesScreen(
                        matches = sharedMatches,
                        partner1Name = partner1Name,
                        partner2Name = partner2Name,
                        onUpdateRating = { nameId, rating -> viewModel.updateMatchRating(nameId, rating) },
                        onUpdateNotes = { nameId, notes -> viewModel.updateMatchNotes(nameId, notes) },
                        onDeleteMatch = { nameId -> viewModel.deleteMatch(nameId) }
                    )
                }
                2 -> {
                    MyLikesScreen(
                        likedNames = myLikes,
                        activeUserName = activeUserName,
                        partnerName = partnerName,
                        onRemoveLike = { nameId -> viewModel.undoSwipe() }
                    )
                }
                3 -> {
                    AlgorithmTuneScreen(
                        currentConfig = algoConfig,
                        explanation = algoExplanation,
                        onSaveConfig = { newConfig -> viewModel.updateAlgorithmConfig(newConfig) },
                        onResetDefaults = { viewModel.resetAlgorithmDefaults() }
                    )
                }
            }

            // Match Celebration Dialog Popup
            matchCelebrationName?.let { matchBabyName ->
                MatchCelebrationDialog(
                    babyName = matchBabyName,
                    activeUserName = activeUserName,
                    partnerName = partnerName,
                    onDismiss = { viewModel.dismissMatchCelebration() },
                    onViewSharedMatches = {
                        viewModel.dismissMatchCelebration()
                        selectedTab = 1 // Open Shared Matches tab
                    }
                )
            }

            // Add Custom Name Modal
            if (showAddNameDialog) {
                AddNameDialog(
                    partnerName = partnerName,
                    onDismiss = { showAddNameDialog = false },
                    onAddName = { name, gender, origin, meaning, pronunciation, tags ->
                        viewModel.addCustomName(
                            name = name,
                            gender = gender,
                            origin = origin,
                            meaning = meaning,
                            pronunciation = pronunciation,
                            tags = tags,
                            context = context
                        )
                        showAddNameDialog = false
                    }
                )
            }
        }
    }
}
