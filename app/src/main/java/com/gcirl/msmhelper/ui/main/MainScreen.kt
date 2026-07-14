package com.gcirl.msmhelper.ui.main

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gcirl.msmhelper.theme.*
import com.gcirl.msmhelper.ui.screens.DailyTrackerTab
import com.gcirl.msmhelper.ui.screens.OverviewTab
import com.gcirl.msmhelper.ui.screens.StoneMaximizerTab
import com.gcirl.msmhelper.ui.screens.NecroTrackerStatsScreen
import com.gcirl.msmhelper.ui.screens.ReadyToCraftTab
import com.gcirl.msmhelper.ui.screens.BackupRestoreTab
import com.gcirl.msmhelper.ui.screens.MastercraftTrackerScreen
import com.gcirl.msmhelper.ui.screens.MastercraftStatsScreen
import com.gcirl.msmhelper.ui.screens.BossAccessoryScreen
import com.gcirl.msmhelper.ui.screens.DamageCalculatorScreen
import com.gcirl.msmhelper.viewmodel.MSMHelperViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: MSMHelperViewModel = viewModel {
        MSMHelperViewModel(context.applicationContext as Application)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val necroHistory by viewModel.necroHistory.collectAsState()
    val mastercraftHistory by viewModel.mastercraftHistory.collectAsState()
    val bossAccessoryHistory by viewModel.bossAccessoryHistory.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface,
                drawerContentColor = TextPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "MSM HELPER",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPurple,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)
                    )
                    Text(
                        text = "Tools & Utilities Menu",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 28.dp).padding(bottom = 20.dp)
                    )
                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- NECRO CATEGORY ---
                    Text(
                        text = "NECRO CRYSTALS & STONES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp)
                    )

                    // Item 0: Daily Tracker
                    NavigationDrawerItem(
                        label = { Text("Daily Tracker", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("💎", fontSize = 20.sp) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0x22C59BFF),
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Item 1: Character Overview
                    NavigationDrawerItem(
                        label = { Text("Character Overview", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("👥", fontSize = 20.sp) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0x22C59BFF),
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Item 2: Ready to Craft
                    NavigationDrawerItem(
                        label = { Text("Ready to Craft", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("🛠️", fontSize = 20.sp) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0x22C59BFF),
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Item 3: Stone Maximizer
                    NavigationDrawerItem(
                        label = { Text("Stone Maximizer", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == 3,
                        onClick = {
                            selectedTab = 3
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("🧮", fontSize = 20.sp) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0x22C59BFF),
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Item 4: Necro Tracker
                    NavigationDrawerItem(
                        label = { Text("Necro Tracker", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == 4,
                        onClick = {
                            selectedTab = 4
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("📊", fontSize = 20.sp) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0x22C59BFF),
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    // --- MASTERCRAFTING CATEGORY ---
                    Text(
                        text = "MASTERCRAFTING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp)
                    )

                    // Item 5: Mastercraft Tracker
                    NavigationDrawerItem(
                        label = { Text("Mastercraft Tracker", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == 5,
                        onClick = {
                            selectedTab = 5
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("⚔️", fontSize = 20.sp) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0x22C59BFF),
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Item 6: Mastercraft Stats
                    NavigationDrawerItem(
                        label = { Text("Mastercraft Stats", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == 6,
                        onClick = {
                            selectedTab = 6
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("📈", fontSize = 20.sp) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0x22C59BFF),
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    // --- BOSSING & DAMAGE CATEGORY ---
                    Text(
                        text = "BOSSING & DAMAGE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp)
                    )

                    // Item 7: Boss Accessory Tracker
                    NavigationDrawerItem(
                        label = { Text("Boss Accessory Tracker", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == 7,
                        onClick = {
                            selectedTab = 7
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("🏆", fontSize = 20.sp) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0x22C59BFF),
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Item 8: Damage Calculator
                    NavigationDrawerItem(
                        label = { Text("Damage Calculator", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == 8,
                        onClick = {
                            selectedTab = 8
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("💥", fontSize = 20.sp) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0x22C59BFF),
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    // --- SYSTEM CATEGORY ---
                    Text(
                        text = "SYSTEM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp)
                    )

                    // Item 9: Backup & Restore
                    NavigationDrawerItem(
                        label = { Text("Backup & Restore", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedTab == 9,
                        onClick = {
                            selectedTab = 9
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("💾", fontSize = 20.sp) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0x22C59BFF),
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Necro Crystal Pieces"
                                1 -> "Character Overview"
                                2 -> "Ready to Craft"
                                3 -> "Stone Maximizer"
                                4 -> "Necro Tracker"
                                5 -> "Mastercraft Tracker"
                                6 -> "Mastercraft Stats"
                                7 -> "Boss Accessory Tracker"
                                8 -> "Damage Calculator"
                                else -> "Backup & Restore"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryPurple
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open navigation menu",
                                tint = PrimaryPurple
                            )
                        }
                    },
                    actions = {
                        val isMastercraftTab = selectedTab == 5 || selectedTab == 6
                        val isBossAccessoryTab = selectedTab == 7
                        val hasHistory = when {
                            isMastercraftTab -> mastercraftHistory.isNotEmpty()
                            isBossAccessoryTab -> bossAccessoryHistory.isNotEmpty()
                            else -> necroHistory.isNotEmpty()
                        }
                        if (hasHistory) {
                            IconButton(onClick = {
                                val msg = when {
                                    isMastercraftTab -> viewModel.undoLastMastercraftAttempt()
                                    isBossAccessoryTab -> viewModel.undoLastBossAccessoryAttempt()
                                    else -> viewModel.undoLastNecroAction()
                                }
                                if (msg != null) {
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo last action",
                                    tint = PrimaryPurple
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface,
                        titleContentColor = PrimaryPurple
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> DailyTrackerTab(viewModel = viewModel, onNavigateToOverview = { selectedTab = 1 })
                    1 -> OverviewTab(viewModel = viewModel)
                    2 -> ReadyToCraftTab(viewModel = viewModel)
                    3 -> StoneMaximizerTab(viewModel = viewModel)
                    4 -> NecroTrackerStatsScreen(viewModel = viewModel)
                    5 -> MastercraftTrackerScreen(viewModel = viewModel)
                    6 -> MastercraftStatsScreen(viewModel = viewModel)
                    7 -> BossAccessoryScreen(viewModel = viewModel)
                    8 -> DamageCalculatorScreen(viewModel = viewModel)
                    9 -> BackupRestoreTab(viewModel = viewModel)
                }
            }
        }
    }
}
