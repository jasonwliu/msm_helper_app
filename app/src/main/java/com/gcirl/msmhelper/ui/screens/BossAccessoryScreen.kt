package com.gcirl.msmhelper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gcirl.msmhelper.theme.*
import com.gcirl.msmhelper.viewmodel.MSMHelperViewModel
import java.util.Locale

@Composable
fun BossAccessoryScreen(
    viewModel: MSMHelperViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val history by viewModel.bossAccessoryHistory.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Computations
    val totalRuns = history.size
    val totalDrops = history.count { it.isSuccess }
    val totalNoDrops = totalRuns - totalDrops
    val dropRatePct = if (totalRuns > 0) (totalDrops.toDouble() / totalRuns * 100) else 0.0

    // Clear dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Accessory History", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete all logged runs for Boss Accessories? This cannot be undone.", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearBossAccessoryHistory()
                        showClearConfirmDialog = false
                        Toast.makeText(context, "Accessory history cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BreakRed, contentColor = Color.White)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    if (isLandscape) {
        // Landscape Mode layout: side-by-side columns
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Column: Stats & Log actions
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "BOSS RUN STATISTICS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Total runs
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Total Runs", fontSize = 11.sp, color = TextMuted)
                                        Text("$totalRuns", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                }

                                // Total drops
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Drops", fontSize = 11.sp, color = TextMuted)
                                        Text("$totalDrops", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                                    }
                                }
                            }

                            // Drop Rate Pct
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Running Drop Rate", fontSize = 11.sp, color = TextMuted)
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.2f%%", dropRatePct),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryPurple
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "LOG RUN RESULT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.logBossAccessoryAttempt(true)
                                        Toast.makeText(context, "Logged Drop (Success)", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = UpGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("DROP SUCCESS", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.logBossAccessoryAttempt(false)
                                        Toast.makeText(context, "Logged No Drop (Failure)", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BreakRed, contentColor = Color.White),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("NO DROP", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BreakRed.copy(alpha = 0.15f), contentColor = BreakRed),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, BreakRed.copy(alpha = 0.5f))
                    ) {
                        Text("Reset Tracking Stats", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Right Column: History Log
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "RUN HISTORY LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkSurface, shape = RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, DarkBorder), shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No runs logged yet.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkSurface, shape = RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, DarkBorder), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { run ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceVariant, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (run.isSuccess) "🏆 Drop Obtained" else "❌ No Drop",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (run.isSuccess) UpGreen else TextPrimary
                                    )
                                    Text(
                                        text = run.timestamp,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Portrait Mode layout: vertical stack
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "BOSS RUN STATISTICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Runs", fontSize = 11.sp, color = TextMuted)
                                Text("$totalRuns", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Drops", fontSize = 11.sp, color = TextMuted)
                                Text("$totalDrops", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("No Drops", fontSize = 11.sp, color = TextMuted)
                                Text("$totalNoDrops", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        HorizontalDivider(color = DarkBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Running Drop Rate", fontSize = 13.sp, color = TextMuted)
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f%%", dropRatePct),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                        }
                    }
                }
            }

            // Actions panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "LOG RUN RESULT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.logBossAccessoryAttempt(true)
                                    Toast.makeText(context, "Logged Drop (Success)", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = UpGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("DROP", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.logBossAccessoryAttempt(false)
                                    Toast.makeText(context, "Logged No Drop (Failure)", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = BreakRed, contentColor = Color.White),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("NO DROP", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // History Log section
            item {
                Text(
                    text = "RUN HISTORY LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            if (history.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(DarkSurface, shape = RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, DarkBorder), shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No runs logged yet.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                items(history.take(15)) { run ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, shape = RoundedCornerShape(4.dp))
                            .border(BorderStroke(1.dp, DarkBorder), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (run.isSuccess) "🏆 Drop Obtained" else "❌ No Drop",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (run.isSuccess) UpGreen else TextPrimary
                            )
                            Text(
                                text = run.timestamp,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Reset tracking button at the very bottom
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BreakRed.copy(alpha = 0.15f), contentColor = BreakRed),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, BreakRed.copy(alpha = 0.5f))
                ) {
                    Text("Reset Tracking Stats", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
