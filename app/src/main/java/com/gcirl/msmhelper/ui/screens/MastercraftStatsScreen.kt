package com.gcirl.msmhelper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // Helper to render stats breakdown row
    @Composable
    fun GearBreakdownItem(type: String, attempts: Int, success: Int, fail: Int, actual: Double, expected: Double) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val gearLabel = type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    Text(
                        text = gearLabel,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$attempts attempts ($success W / $fail L)",
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
                        Text(String.format(Locale.getDefault(), "%.1f%%", actual), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Expected Rate", fontSize = 10.sp, color = TextMuted)
                        Text(String.format(Locale.getDefault(), "%.1f%%", expected), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                    }
                }
            }
        }
    }

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Half: Stats Cards & Breakdown
            LazyColumn(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Core Metrics
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "OVERALL METRICS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Successes", fontSize = 11.sp, color = TextMuted)
                                    Text("$successes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                                }
                                Column {
                                    Text("Failures", fontSize = 11.sp, color = TextMuted)
                                    Text("$failures", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BreakRed)
                                }
                                Column {
                                    Text("Total Attempts", fontSize = 11.sp, color = TextMuted)
                                    Text("$totalAttempts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Actual Success Rate", fontSize = 11.sp, color = TextMuted)
                                    Text(String.format(Locale.getDefault(), "%.2f%%", actualRate), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Expected Success Rate", fontSize = 11.sp, color = TextMuted)
                                    Text(String.format(Locale.getDefault(), "%.2f%%", expectedRate), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                }
                            }
                        }
                    }
                }

                // Breakdown section
                item {
                    Text(
                        text = "BREAKDOWN BY TIER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                val gearTypes = listOf("necro", "inherit", "chaos", "absolab", "arcane")
                items(gearTypes) { type ->
                    val typeHistory = history.filter { it.gearType == type }
                    if (typeHistory.isNotEmpty()) {
                        val typeAttempts = typeHistory.size
                        val typeSuccesses = typeHistory.count { it.isSuccess }
                        val typeFailures = typeAttempts - typeSuccesses
                        val typeActual = (typeSuccesses.toDouble() / typeAttempts * 100)
                        val typeExpected = typeHistory.map { it.totalRate }.average()

                        GearBreakdownItem(
                            type = type,
                            attempts = typeAttempts,
                            success = typeSuccesses,
                            fail = typeFailures,
                            actual = typeActual,
                            expected = typeExpected
                        )
                    }
                }

                // Clear history button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
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

            // Right Half: History Log
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "MASTERCRAFTING HISTORY LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val gearFormatted = run.gearType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                val refineLabel = if (run.isRefined) "Refined" else "Unrefined"
                                val scrollsLabel = if (run.luckyScrolls.isEmpty()) "No scrolls" else "Scrolls: " + run.luckyScrolls.joinToString { "+$it%" }
                                Text(
                                    text = "$gearFormatted ($refineLabel) - $scrollsLabel",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Rate: ${run.totalRate}% | ${run.timestamp}",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (run.isSuccess) "SUCCESS" else "FAILURE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (run.isSuccess) UpGreen else BreakRed
                                )
                                IconButton(
                                    onClick = {
                                        viewModel.deleteMastercraftAttempt(run.timestamp)
                                        Toast.makeText(context, "Attempt deleted", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete attempt",
                                        tint = BreakRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Portrait Mode: Single column vertical stack
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OVERALL METRICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Successes", fontSize = 11.sp, color = TextMuted)
                                Text("$successes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                            }
                            Column {
                                Text("Failures", fontSize = 11.sp, color = TextMuted)
                                Text("$failures", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BreakRed)
                            }
                            Column {
                                Text("Attempts", fontSize = 11.sp, color = TextMuted)
                                Text("$totalAttempts", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Actual Success Rate", fontSize = 11.sp, color = TextMuted)
                                Text(String.format(Locale.getDefault(), "%.2f%%", actualRate), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = UpGreen)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Expected Success Rate", fontSize = 11.sp, color = TextMuted)
                                Text(String.format(Locale.getDefault(), "%.2f%%", expectedRate), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                            }
                        }
                    }
                }
            }

            // Breakdown header
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
            items(gearTypes) { type ->
                val typeHistory = history.filter { it.gearType == type }
                if (typeHistory.isNotEmpty()) {
                    val typeAttempts = typeHistory.size
                    val typeSuccesses = typeHistory.count { it.isSuccess }
                    val typeFailures = typeAttempts - typeSuccesses
                    val typeActual = (typeSuccesses.toDouble() / typeAttempts * 100)
                    val typeExpected = typeHistory.map { it.totalRate }.average()

                    GearBreakdownItem(
                        type = type,
                        attempts = typeAttempts,
                        success = typeSuccesses,
                        fail = typeFailures,
                        actual = typeActual,
                        expected = typeExpected
                    )
                }
            }

            // History Log header
            item {
                Text(
                    text = "MASTERCRAFTING HISTORY LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // History items
            items(history) { run ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, shape = RoundedCornerShape(4.dp))
                        .border(BorderStroke(1.dp, DarkBorder), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val gearFormatted = run.gearType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        val refineLabel = if (run.isRefined) "Refined" else "Unrefined"
                        val scrollsLabel = if (run.luckyScrolls.isEmpty()) "No scrolls" else "Scrolls: " + run.luckyScrolls.joinToString { "+$it%" }
                        Text(
                            text = "$gearFormatted ($refineLabel) - $scrollsLabel",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Rate: ${run.totalRate}% | ${run.timestamp}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (run.isSuccess) "SUCCESS" else "FAILURE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (run.isSuccess) UpGreen else BreakRed
                        )
                        IconButton(
                            onClick = {
                                viewModel.deleteMastercraftAttempt(run.timestamp)
                                Toast.makeText(context, "Attempt deleted", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete attempt",
                                tint = BreakRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Clear history button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showConfirmClear = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
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
