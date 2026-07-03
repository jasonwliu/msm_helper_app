package com.gcirl.msmhelper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gcirl.msmhelper.viewmodel.MSMHelperViewModel
import com.gcirl.msmhelper.theme.*
import java.util.Locale

@Composable
fun MastercraftStatsScreen(viewModel: MSMHelperViewModel) {
    val context = LocalContext.current
    val history by viewModel.mastercraftHistory.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var showConfirmClear by remember { mutableStateOf(false) }

    // Computations
    val totalAttempts = history.size
    val successes = history.count { it.isSuccess }
    val failures = totalAttempts - successes
    val actualRate = if (totalAttempts > 0) (successes.toDouble() / totalAttempts * 100) else 0.0
    val expectedRate = if (totalAttempts > 0) (history.map { it.totalRate }.average()) else 0.0

    // Confirmation dialog
    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            title = { Text("Clear Mastercraft History", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently clear all logged mastercraft attempts? This cannot be undone.", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearMastercraftHistory()
                        showConfirmClear = false
                        Toast.makeText(context, "Mastercraft history cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BreakRed, contentColor = Color.White)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    if (totalAttempts == 0) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📊 No Mastercraft History Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Log your mastercraft successes and failures in the Mastercraft Tracker tab to view success rate metrics here.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    if (isLandscape) {
        // Landscape Mode: Left side overall stats, Right side breakdown
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Half: Core statistics
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
                                text = "OVERALL METRICS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Total attempts
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Attempts", fontSize = 11.sp, color = TextMuted)
                                        Text("$totalAttempts", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                }

                                // Success / Failure
                                Card(
                                    modifier = Modifier.weight(1.2f),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Success / Fail", fontSize = 11.sp, color = TextMuted)
                                        Text("$successes / $failures", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Actual Success Rate
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Actual Rate", fontSize = 11.sp, color = TextMuted)
                                        Text(String.format(Locale.getDefault(), "%.1f%%", actualRate), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                                    }
                                }

                                // Expected Success Rate
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Expected Rate", fontSize = 11.sp, color = TextMuted)
                                        Text(String.format(Locale.getDefault(), "%.1f%%", expectedRate), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                    }
                                }
                            }
                        }
                    }
                }

                // Danger zone in left half
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, BreakRed.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Button(
                                onClick = { showConfirmClear = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BreakRed.copy(alpha = 0.15f), contentColor = BreakRed),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, BreakRed.copy(alpha = 0.5f))
                            ) {
                                Text("Clear Mastercraft History", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Right Half: Breakdown by gear type
            LazyColumn(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "BREAKDOWN BY TIER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }

                val gearTypes = listOf("necro", "inherit", "chaos", "absolab", "arcane")
                gearTypes.forEach { type ->
                    val typeHistory = history.filter { it.gearType == type }
                    if (typeHistory.isNotEmpty()) {
                        item {
                            val typeAttempts = typeHistory.size
                            val typeSuccesses = typeHistory.count { it.isSuccess }
                            val typeFailures = typeAttempts - typeSuccesses
                            val typeActual = (typeSuccesses.toDouble() / typeAttempts * 100)
                            val typeExpected = typeHistory.map { it.totalRate }.average()

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = BorderStroke(1.dp, DarkBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = type.replaceFirstChar { it.uppercase() },
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "$typeAttempts attempts ($typeSuccesses W / $typeFailures L)",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Actual Rate", fontSize = 10.sp, color = TextMuted)
                                            Text(String.format(Locale.getDefault(), "%.1f%%", typeActual), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Expected Rate", fontSize = 10.sp, color = TextMuted)
                                            Text(String.format(Locale.getDefault(), "%.1f%%", typeExpected), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Portrait Mode: Single stack of cards
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Core Metrics
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
                            text = "OVERALL METRICS",
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
                                Text("Total Attempts", fontSize = 11.sp, color = TextMuted)
                                Text("$totalAttempts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Success / Failure", fontSize = 11.sp, color = TextMuted)
                                Text("$successes / $failures", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        Divider(color = DarkBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Actual Rate", fontSize = 11.sp, color = TextMuted)
                                Text(String.format(Locale.getDefault(), "%.1f%%", actualRate), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Expected Rate", fontSize = 11.sp, color = TextMuted)
                                Text(String.format(Locale.getDefault(), "%.1f%%", expectedRate), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                            }
                        }
                    }
                }
            }

            // Card 2: Breakdown list
            item {
                Text(
                    text = "BREAKDOWN BY TIER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            val gearTypes = listOf("necro", "inherit", "chaos", "absolab", "arcane")
            gearTypes.forEach { type ->
                val typeHistory = history.filter { it.gearType == type }
                if (typeHistory.isNotEmpty()) {
                    item {
                        val typeAttempts = typeHistory.size
                        val typeSuccesses = typeHistory.count { it.isSuccess }
                        val typeFailures = typeAttempts - typeSuccesses
                        val typeActual = (typeSuccesses.toDouble() / typeAttempts * 100)
                        val typeExpected = typeHistory.map { it.totalRate }.average()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = type.replaceFirstChar { it.uppercase() },
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "$typeAttempts attempts ($typeSuccesses W / $typeFailures L)",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Actual Rate", fontSize = 11.sp, color = TextMuted)
                                        Text(String.format(Locale.getDefault(), "%.1f%%", typeActual), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Expected Rate", fontSize = 11.sp, color = TextMuted)
                                        Text(String.format(Locale.getDefault(), "%.1f%%", typeExpected), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 3: Danger zone
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, BreakRed.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Button(
                            onClick = { showConfirmClear = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BreakRed.copy(alpha = 0.15f), contentColor = BreakRed),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, BreakRed.copy(alpha = 0.5f))
                        ) {
                            Text("Clear Mastercraft History", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
