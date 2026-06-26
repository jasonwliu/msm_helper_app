package com.gcirl.msmhelper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gcirl.msmhelper.data.*
import com.gcirl.msmhelper.theme.*
import com.gcirl.msmhelper.viewmodel.MSMHelperViewModel

@Composable
fun NecroTrackerStatsScreen(
    viewModel: MSMHelperViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val necroHistory by viewModel.necroHistory.collectAsState()

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Filter to only drop addition runs
    val addRuns = remember(necroHistory) {
        necroHistory.filter { it.actionType == "add_drop" }
    }
    val totalRuns = addRuns.size

    // Calculate core statistics
    val avgBasePieces = remember(addRuns) {
        if (totalRuns > 0) addRuns.map { it.base }.average() else 0.0
    }
    val clusterRuns = remember(addRuns) {
        addRuns.count { it.cluster > 0 }
    }
    val clusterFrequencyPct = remember(addRuns, clusterRuns) {
        if (totalRuns > 0) (clusterRuns.toDouble() / totalRuns * 100) else 0.0
    }
    val avgClusterSize = remember(addRuns, clusterRuns) {
        if (clusterRuns > 0) addRuns.filter { it.cluster > 0 }.map { it.cluster }.average() else 0.0
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear History?") },
            text = { Text("Are you sure you want to clear all tracking history? This will reset all stats to 0 and cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearNecroHistory()
                        showClearConfirmDialog = false
                        Toast.makeText(context, "History cleared!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear All", color = BreakRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = TextPrimary)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 1. Core Summary Stats Grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Necro Tracker Stats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aggregated performance across all logged piece drops.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMiniCard(
                            label = "Avg Base / Run",
                            value = String.format("%.2f", avgBasePieces),
                            subText = "excl. clusters",
                            modifier = Modifier.weight(1f),
                            color = PrimaryPurple
                        )
                        StatMiniCard(
                            label = "Cluster Rate",
                            value = String.format("%.1f%%", clusterFrequencyPct),
                            subText = "$clusterRuns / $totalRuns runs",
                            modifier = Modifier.weight(1f),
                            color = SecondaryTeal
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMiniCard(
                            label = "Avg Cluster Drop",
                            value = String.format("%.1f", avgClusterSize),
                            subText = "per cluster drop",
                            modifier = Modifier.weight(1f),
                            color = StarGold
                        )
                        StatMiniCard(
                            label = "Total Runs",
                            value = totalRuns.toString(),
                            subText = "runs tracked",
                            modifier = Modifier.weight(1f),
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // 2. Base Choices running totals
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
                        text = "Base Drop Choices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Distribution of your selected base piece counts (3, 5, or 7).",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                    listOf(3, 5, 7).forEach { baseVal ->
                        val count = addRuns.count { it.base == baseVal }
                        val percentage = if (totalRuns > 0) (count.toDouble() / totalRuns * 100) else 0.0
                        
                        DistributionRow(
                            label = "$baseVal Pieces",
                            count = count,
                            percentage = percentage,
                            color = PrimaryPurple
                        )
                    }
                }
            }
        }

        // 3. Cluster Choices running totals
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
                        text = "Cluster Drop Choices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Distribution of cluster piece drops (No Cluster or 20-80).",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                    // No Cluster
                    val noClusterCount = addRuns.count { it.cluster == 0 }
                    val noClusterPct = if (totalRuns > 0) (noClusterCount.toDouble() / totalRuns * 100) else 0.0
                    DistributionRow(
                        label = "No Cluster",
                        count = noClusterCount,
                        percentage = noClusterPct,
                        color = MaintainGrey
                    )

                    // Rest of cluster values
                    listOf(20, 30, 40, 50, 60, 70, 80).forEach { clusterVal ->
                        val count = addRuns.count { it.cluster == clusterVal }
                        val percentage = if (totalRuns > 0) (count.toDouble() / totalRuns * 100) else 0.0
                        DistributionRow(
                            label = "$clusterVal Cluster",
                            count = count,
                            percentage = percentage,
                            color = SecondaryTeal
                        )
                    }
                }
            }
        }

        // 4. Clear history action card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Danger Zone",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BreakRed,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "Clearing tracking history will reset all averages and distribution data. Characters and their piece counts will remain unaffected.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showClearConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BreakRed, contentColor = Color.White),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Tracking History", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StatMiniCard(
    label: String,
    value: String,
    subText: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subText, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
fun DistributionRow(
    label: String,
    count: Int,
    percentage: Double,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(
                text = "$count (${String.format("%.1f", percentage)}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF222232), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((percentage / 100).toFloat().coerceIn(0f, 1f))
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}
