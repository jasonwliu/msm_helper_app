package com.gcirl.msmhelper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gcirl.msmhelper.viewmodel.MSMHelperViewModel
import com.gcirl.msmhelper.theme.*

@Composable
fun MastercraftTrackerScreen(viewModel: MSMHelperViewModel) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // State selections
    var selectedGear by remember { mutableStateOf("necro") } // "necro", "inherit", "chaos", "absolab", "arcane"
    var selectedScrolls by remember { mutableStateOf<List<Int>>(emptyList()) } // e.g. [5, 10]
    var isRefined by remember { mutableStateOf(false) }

    // Constants & business rules
    val baseRate = when (selectedGear) {
        "necro" -> 4
        "inherit" -> 30
        "chaos" -> 30
        "absolab" -> 12
        "arcane" -> 10
        else -> 0
    }

    val canRefine = when (selectedGear) {
        "necro", "chaos" -> true
        else -> false
    }

    val refinementBonus = if (canRefine && isRefined) {
        when (selectedGear) {
            "necro" -> 4
            "chaos" -> 10
            else -> 0
        }
    } else {
        0
    }

    val isScrollMultiSelect = when (selectedGear) {
        "necro", "inherit" -> true
        else -> false
    }

    val scrollOptions = when (selectedGear) {
        "necro", "inherit" -> listOf(3, 5, 7, 10, 15)
        else -> listOf(3, 5, 7, 10)
    }

    // Helper to calculate total rate
    val scrollBonus = selectedScrolls.sum()
    val totalRate = baseRate + refinementBonus + scrollBonus

    // Reset parameters on gear type change to prevent invalid states
    fun onGearTypeChange(newGear: String) {
        selectedGear = newGear
        selectedScrolls = emptyList()
        // If the new gear can't be refined, force it to false
        if (newGear != "necro" && newGear != "chaos") {
            isRefined = false
        }
    }

    // Toggle lucky scrolls
    fun toggleScroll(percent: Int) {
        if (isScrollMultiSelect) {
            if (selectedScrolls.contains(percent)) {
                selectedScrolls = selectedScrolls.filter { it != percent }
            } else {
                if (selectedScrolls.size >= 2) {
                    // Keep the newest, discard the oldest to enforce max 2
                    selectedScrolls = listOf(selectedScrolls.last(), percent)
                } else {
                    selectedScrolls = selectedScrolls + percent
                }
            }
        } else {
            // Single-select: toggle selection
            selectedScrolls = if (selectedScrolls.contains(percent)) {
                emptyList()
            } else {
                listOf(percent)
            }
        }
    }

    // Log attempt helper
    fun recordAttempt(isSuccess: Boolean) {
        viewModel.logMastercraftAttempt(
            gearType = selectedGear,
            isRefined = isRefined,
            luckyScrolls = selectedScrolls,
            totalRate = totalRate,
            isSuccess = isSuccess
        )
        val gearNameFormatted = selectedGear.replaceFirstChar { it.uppercase() }
        val outcomeText = if (isSuccess) "Success" else "Failure"
        Toast.makeText(context, "Logged $gearNameFormatted Mastercraft $outcomeText ($totalRate%)", Toast.LENGTH_SHORT).show()
    }

    if (isLandscape) {
        // Landscape Mode: 4 Columns side-by-side
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Column 1: Gear Type
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "GEAR TIER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            listOf(
                                "necro" to "Necro (4% Base)",
                                "inherit" to "Inherit (30% Base)",
                                "chaos" to "Chaos (30% Base)",
                                "absolab" to "Absolab (12% Base)",
                                "arcane" to "Arcane (10% Base)"
                            ).forEach { (key, label) ->
                                val active = selectedGear == key
                                Button(
                                    onClick = { onGearTypeChange(key) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (active) PrimaryPurple else DarkSurfaceVariant,
                                        contentColor = if (active) Color.Black else TextPrimary
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, if (active) PrimaryPurple else DarkBorder),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Column 2: Lucky Scrolls
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LUCKY SCROLLS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (isScrollMultiSelect) "Max 2" else "Max 1",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            scrollOptions.forEach { value ->
                                val active = selectedScrolls.contains(value)
                                Button(
                                    onClick = { toggleScroll(value) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (active) SecondaryTeal else DarkSurfaceVariant,
                                        contentColor = if (active) Color.Black else TextPrimary
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, if (active) SecondaryTeal else DarkBorder),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("+$value% Scroll", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Column 3: Refinement Toggle
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "REFINEMENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Unrefined Option
                            val unrefinedActive = !isRefined
                            Button(
                                onClick = { if (canRefine) isRefined = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (canRefine) 1f else 0.5f),
                                enabled = canRefine,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (unrefinedActive) PrimaryPurple else DarkSurfaceVariant,
                                    contentColor = if (unrefinedActive) Color.Black else TextPrimary
                                ),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (unrefinedActive && canRefine) PrimaryPurple else DarkBorder),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Unrefined (+0%)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Refined Option
                            val refinedActive = isRefined
                            val refinedBonusLabel = when (selectedGear) {
                                "necro" -> "+4%"
                                "chaos" -> "+10%"
                                else -> "+0%"
                            }
                            Button(
                                onClick = { if (canRefine) isRefined = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (canRefine) 1f else 0.5f),
                                enabled = canRefine,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (refinedActive) PrimaryPurple else DarkSurfaceVariant,
                                    contentColor = if (refinedActive) Color.Black else TextPrimary
                                ),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (refinedActive && canRefine) PrimaryPurple else DarkBorder),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Refined ($refinedBonusLabel)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            if (!canRefine) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Refinement has no effect on success rate for ${selectedGear.replaceFirstChar { it.uppercase() }}.",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Column 4: Pass or Fail Actions
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "PROBABILITY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )

                            // Rate Breakdown Card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceVariant, shape = RoundedCornerShape(4.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Base Rate", color = TextMuted, fontSize = 12.sp)
                                    Text("$baseRate%", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Refinement", color = TextMuted, fontSize = 12.sp)
                                    Text("+$refinementBonus%", color = if (refinementBonus > 0) PrimaryPurple else TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Lucky Scrolls", color = TextMuted, fontSize = 12.sp)
                                    Text("+$scrollBonus%", color = if (scrollBonus > 0) SecondaryTeal else TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                                HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Total Success", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("$totalRate%", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Success Button
                            Button(
                                onClick = { recordAttempt(true) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UpGreen,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("SUCCESS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // Failure Button
                            Button(
                                onClick = { recordAttempt(false) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BreakRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("FAILURE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Portrait Mode: Stacked Cards/Lists
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Gear Tier
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
                            text = "GEAR TIER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("necro" to "Necro\n(4%)", "inherit" to "Inherit\n(30%)").forEach { (key, label) ->
                                val active = selectedGear == key
                                Button(
                                    onClick = { onGearTypeChange(key) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (active) PrimaryPurple else DarkSurfaceVariant,
                                        contentColor = if (active) Color.Black else TextPrimary
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, if (active) PrimaryPurple else DarkBorder),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("chaos" to "Chaos\n(30%)", "absolab" to "Absolab\n(12%)", "arcane" to "Arcane\n(10%)").forEach { (key, label) ->
                                val active = selectedGear == key
                                Button(
                                    onClick = { onGearTypeChange(key) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (active) PrimaryPurple else DarkSurfaceVariant,
                                        contentColor = if (active) Color.Black else TextPrimary
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, if (active) PrimaryPurple else DarkBorder),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }

            // Card 2: Lucky Scrolls
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LUCKY SCROLLS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isScrollMultiSelect) "Select up to 2" else "Select up to 1",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        
                        // Scroll Buttons Grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            scrollOptions.chunked(3).forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    chunk.forEach { value ->
                                        val active = selectedScrolls.contains(value)
                                        Button(
                                            onClick = { toggleScroll(value) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (active) SecondaryTeal else DarkSurfaceVariant,
                                                contentColor = if (active) Color.Black else TextPrimary
                                            ),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, if (active) SecondaryTeal else DarkBorder),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("+$value%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                    // Pad empty spaces in row
                                    if (chunk.size < 3) {
                                        repeat(3 - chunk.size) {
                                            Box(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 3: Refinement Toggle
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
                            text = "REFINEMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val unrefinedActive = !isRefined
                            Button(
                                onClick = { if (canRefine) isRefined = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (canRefine) 1f else 0.5f),
                                enabled = canRefine,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (unrefinedActive) PrimaryPurple else DarkSurfaceVariant,
                                    contentColor = if (unrefinedActive) Color.Black else TextPrimary
                                ),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (unrefinedActive && canRefine) PrimaryPurple else DarkBorder)
                            ) {
                                Text("Unrefined (+0%)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            val refinedActive = isRefined
                            val refinedBonusLabel = when (selectedGear) {
                                "necro" -> "+4%"
                                "chaos" -> "+10%"
                                else -> "+0%"
                            }
                            Button(
                                onClick = { if (canRefine) isRefined = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (canRefine) 1f else 0.5f),
                                enabled = canRefine,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (refinedActive) PrimaryPurple else DarkSurfaceVariant,
                                    contentColor = if (refinedActive) Color.Black else TextPrimary
                                ),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (refinedActive && canRefine) PrimaryPurple else DarkBorder)
                            ) {
                                Text("Refined ($refinedBonusLabel)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (!canRefine) {
                            Text(
                                text = "Refinement does not affect ${selectedGear.replaceFirstChar { it.uppercase() }} mastercraft success rates.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Card 4: Pass or Fail / Results Card
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
                            text = "TOTAL SUCCESS RATE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${selectedGear.replaceFirstChar { it.uppercase() }} Mastercraft",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Base $baseRate% + Refine $refinementBonus% + Scrolls $scrollBonus%",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "$totalRate%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                        }

                        HorizontalDivider(color = DarkBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { recordAttempt(true) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UpGreen,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("SUCCESS", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { recordAttempt(false) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BreakRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("FAILURE", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
