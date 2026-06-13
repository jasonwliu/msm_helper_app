package com.example.msmhelper.ui.main

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import com.example.msmhelper.theme.*
import com.example.msmhelper.ui.screens.DailyTrackerTab
import com.example.msmhelper.ui.screens.OverviewTab
import com.example.msmhelper.ui.screens.StoneMaximizerTab
import com.example.msmhelper.ui.screens.StarForceTrackerScreen
import com.example.msmhelper.ui.screens.ReadyToCraftTab
import com.example.msmhelper.viewmodel.MSMHelperViewModel
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface,
                drawerContentColor = TextPrimary
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

                // Item 4: Star Force Tracker
                NavigationDrawerItem(
                    label = { Text("Star Force Tracker", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        scope.launch { drawerState.close() }
                    },
                    icon = { Text("⭐", fontSize = 20.sp) },
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
                                else -> "Star Force Rates"
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
                    4 -> StarForceTrackerScreen(viewModel = viewModel)
                }
            }
        }
    }
}
