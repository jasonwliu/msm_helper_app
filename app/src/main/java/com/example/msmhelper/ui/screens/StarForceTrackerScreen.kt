package com.example.msmhelper.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.TextStyle
import com.example.msmhelper.data.*
import com.example.msmhelper.theme.*
import com.example.msmhelper.viewmodel.MSMHelperViewModel

// Advertised Rates Mapping
val advertisedRates = mapOf(
    9 to mapOf("up" to 55, "maintain" to 45, "derank" to 0, "break" to 0),
    10 to mapOf("up" to 50, "maintain" to 40, "derank" to 10, "break" to 0),
    11 to mapOf("up" to 45, "maintain" to 45, "derank" to 10, "break" to 0),
    12 to mapOf("up" to 40, "maintain" to 40, "derank" to 15, "break" to 5),
    13 to mapOf("up" to 35, "maintain" to 45, "derank" to 15, "break" to 5),
    14 to mapOf("up" to 30, "maintain" to 45, "derank" to 20, "break" to 5),
    15 to mapOf("up" to 25, "maintain" to 50, "derank" to 20, "break" to 5),
    16 to mapOf("up" to 20, "maintain" to 50, "derank" to 25, "break" to 5),
    17 to mapOf("up" to 15, "maintain" to 55, "derank" to 25, "break" to 5),
    18 to mapOf("up" to 10, "maintain" to 55, "derank" to 30, "break" to 5)
)

@Composable
fun StarForceTrackerScreen(
    viewModel: MSMHelperViewModel,
    modifier: Modifier = Modifier
) {
    val currentSf by viewModel.currentSf.collectAsState()
    val starCatchActive = remember { mutableStateOf(false) }
    val sfHistory by viewModel.sfHistory.collectAsState()
    val sfStats by viewModel.sfStats.collectAsState()

    var startSfInput by remember(currentSf) { mutableStateOf(currentSf.toString()) }
    var historyLimitText by remember { mutableStateOf("5") }
    var chartsVisible by remember { mutableStateOf(true) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title
        item {
            Text(
                text = "Star Force Tracker",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        // 2. SF Setup & Gold Star Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Starting SF (0-30):", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = startSfInput,
                                onValueChange = { startSfInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(70.dp),
                                textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = {
                                    val parsed = startSfInput.toIntOrNull()
                                    if (parsed != null && parsed in 0..30) {
                                        viewModel.setInitialSf(parsed)
                                    } else {
                                        Toast.makeText(context, "Enter SF between 0 and 30", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text("Set", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Huge Gold Star Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "★",
                            fontSize = 44.sp,
                            color = StarGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentSf.toString(),
                            fontSize = 48.sp,
                            color = StarGold,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 3. Outcomes Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Outcomes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Star Catch Toggle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { starCatchActive.value = !starCatchActive.value }
                            ) {
                                Checkbox(
                                    checked = starCatchActive.value,
                                    onCheckedChange = { starCatchActive.value = it },
                                    colors = CheckboxDefaults.colors(checkedColor = StarGold)
                                )
                                Text(
                                    text = "💫 Catch (+5%)",
                                    fontSize = 12.sp,
                                    color = if (starCatchActive.value) StarGold else TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Undo Button
                            Button(
                                onClick = { viewModel.undoLastAction() },
                                colors = ButtonDefaults.buttonColors(containerColor = BreakRed, contentColor = Color.White),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Undo Last", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 2x2 Upgrade Buttons Grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.recordOutcome("up", starCatchActive.value) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = UpGreen, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Star Up", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Button(
                                onClick = { viewModel.recordOutcome("maintain", starCatchActive.value) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaintainGrey, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Maintain", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.recordOutcome("derank", starCatchActive.value) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DerankOrange, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Derank", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Button(
                                onClick = { viewModel.recordOutcome("break", starCatchActive.value) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BreakRed, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Break", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // 4. Success Rates vs Advertised
        item {
            val modeLabel = if (starCatchActive.value) "CATCH" else "NORMAL"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rates Comparison ($modeLabel)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Hide/Show
                            Button(
                                onClick = { chartsVisible = !chartsVisible },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = TextPrimary),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(if (chartsVisible) "Hide" else "Show", fontSize = 11.sp)
                            }

                            // Export CSV
                            Button(
                                onClick = {
                                    val csvContent = generateStatsCsv(sfStats, sfHistory)
                                    clipboardManager.setText(AnnotatedString(csvContent))
                                    Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Export CSV", fontSize = 11.sp)
                            }
                        }
                    }

                    AnimatedVisibility(visible = chartsVisible) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val activeMode = if (starCatchActive.value) "catch" else "normal"

                            // A. Break Rate Summary
                            val breakRates = advertisedRates.values.map { it["break"] ?: 0 }.filter { it > 0 }.distinct().sorted()
                            val breakStatsList = breakRates.mapNotNull { rate ->
                                val targetSfs = advertisedRates.filter { it.value["break"] == rate }.keys
                                var totalAttempts = 0
                                var totalBreaks = 0
                                targetSfs.forEach { sf ->
                                    val stat = sfStats[sf]
                                    if (stat != null) {
                                        val modeStat = if (activeMode == "catch") stat.catchStats else stat.normal
                                        totalAttempts += modeStat.total
                                        totalBreaks += modeStat.breakCount
                                    }
                                }
                                if (totalAttempts > 0) {
                                    Triple(rate, totalAttempts, totalBreaks)
                                } else null
                            }

                            if (breakStatsList.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Break Rate Summary ($modeLabel)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StarGold
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        breakStatsList.forEach { (advRate, attempts, breaks) ->
                                            val observed = (breaks.toDouble() / attempts * 100)
                                            Text(
                                                text = "$advRate% Adv Break Rate (Attempts: $attempts)",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            DoubleProgressBar(
                                                label = "Break Rate",
                                                adv = advRate.toDouble(),
                                                obs = observed,
                                                color = BreakRed
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }

                            // B. Individual SF level stats
                            val activeSfs = sfStats.keys.filter {
                                val s = sfStats[it]
                                s != null && (if (activeMode == "catch") s.catchStats.total else s.normal.total) > 0
                            }.sorted()

                            if (activeSfs.isEmpty()) {
                                Text(
                                    "No stats accumulated in $modeLabel mode yet. Make some upgrade attempts!",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(10.dp)
                                )
                            } else {
                                activeSfs.forEach { sf ->
                                    val levelStats = sfStats[sf]!!
                                    val outcomeStats = if (activeMode == "catch") levelStats.catchStats else levelStats.normal
                                    val adv = advertisedRates[sf] ?: mapOf("up" to 0, "maintain" to 0, "derank" to 0, "break" to 0)

                                    // Star catch adjusts advertised success up +5%, maintain down -5%
                                    val isCatch = activeMode == "catch"
                                    val advUp = if (isCatch) minOf(100, (adv["up"] ?: 0) + 5) else (adv["up"] ?: 0)
                                    val advMaint = if (isCatch) maxOf(0, (adv["maintain"] ?: 0) - 5) else (adv["maintain"] ?: 0)
                                    val advDerank = adv["derank"] ?: 0

                                    val obsUp = if (outcomeStats.total > 0) (outcomeStats.up.toDouble() / outcomeStats.total * 100) else 0.0
                                    val obsMaint = if (outcomeStats.total > 0) (outcomeStats.maintain.toDouble() / outcomeStats.total * 100) else 0.0
                                    val obsDerank = if (outcomeStats.total > 0) (outcomeStats.derank.toDouble() / outcomeStats.total * 100) else 0.0

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "SF $sf (Attempts: ${outcomeStats.total})",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = StarGold
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            DoubleProgressBar("Up (Success)", advUp.toDouble(), obsUp, UpGreen)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            DoubleProgressBar("Maintain", advMaint.toDouble(), obsMaint, MaintainGrey)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            DoubleProgressBar("Derank", advDerank.toDouble(), obsDerank, DerankOrange)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Sequence History
        item {
            val limit = historyLimitText.toIntOrNull() ?: 5
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sequence History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Show last:", fontSize = 11.sp, color = TextMuted)
                            OutlinedTextField(
                                value = historyLimitText,
                                onValueChange = { historyLimitText = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(60.dp),
                                textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary)
                            )
                        }
                    }

                    if (sfHistory.isEmpty()) {
                        Text(
                            text = "No history recorded yet.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(10.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            sfHistory.take(limit).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "[${item.time}]  SF ${item.fromSf} →",
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )

                                    val badgeBg = when (item.outcome) {
                                        "up" -> UpGreen
                                        "maintain" -> MaintainGrey
                                        "derank" -> DerankOrange
                                        "break" -> BreakRed
                                        else -> DarkBorder
                                    }
                                    val badgeText = when (item.outcome) {
                                        "up" -> "Up"
                                        "maintain" -> "Maintain"
                                        "derank" -> "Derank"
                                        "break" -> "Break"
                                        else -> item.outcome
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = badgeBg),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (item.isCatch) "💫 $badgeText" else badgeText,
                                            color = if (item.outcome == "maintain") Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun DoubleProgressBar(
    label: String,
    adv: Double,
    obs: Double,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(
                text = "Adv: ${adv.toInt()}% | Obs: ${String.format("%.1f", obs)}%",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Advertised bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF222232), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((adv / 100).toFloat().coerceIn(0f, 1f))
                        .background(Color(0x33FFFFFF), RoundedCornerShape(3.dp))
                )
            }
            // Observed bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF222232), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((obs / 100).toFloat().coerceIn(0f, 1f))
                        .background(color, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

// Stats CSV exporter
fun generateStatsCsv(stats: Map<Int, SfLevelStats>, history: List<SfHistoryItem>): String {
    val sb = StringBuilder()
    sb.append("--- SUMMARY STATS ---\n")
    sb.append("SF Level,Mode,Total,Up,Up %,Maintain,Maintain %,Derank,Derank %,Break,Break %\n")

    stats.keys.sorted().forEach { sf ->
        val levelStats = stats[sf]!!
        listOf("normal" to levelStats.normal, "catch" to levelStats.catchStats).forEach { (mode, outcome) ->
            if (outcome.total > 0) {
                val total = outcome.total.toDouble()
                val upPct = outcome.up / total * 100
                val maintPct = outcome.maintain / total * 100
                val derankPct = outcome.derank / total * 100
                val breakPct = outcome.breakCount / total * 100

                sb.append("$sf,$mode,${outcome.total},${outcome.up},${String.format("%.1f", upPct)}%,")
                sb.append("${outcome.maintain},${String.format("%.1f", maintPct)}%,")
                sb.append("${outcome.derank},${String.format("%.1f", derankPct)}%,")
                sb.append("${outcome.breakCount},${String.format("%.1f", breakPct)}%\n")
            }
        }
    }

    sb.append("\n--- SEQUENCE HISTORY ---\n")
    sb.append("Timestamp,From SF,Outcome,Star Catch\n")
    history.forEach { item ->
        sb.append("${item.time},${item.fromSf},${item.outcome},${item.isCatch}\n")
    }

    return sb.toString()
}
