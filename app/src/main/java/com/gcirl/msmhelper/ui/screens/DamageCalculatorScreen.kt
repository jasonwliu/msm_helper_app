package com.gcirl.msmhelper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gcirl.msmhelper.data.CalcPreset
import com.gcirl.msmhelper.theme.*
import com.gcirl.msmhelper.viewmodel.MSMHelperViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DamageCalculatorScreen(
    viewModel: MSMHelperViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val presets by viewModel.calcPresets.collectAsState()
    val activeIndex by viewModel.activePresetIndex.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val activePreset = presets.getOrNull(activeIndex) ?: CalcPreset(name = "Default Preset")

    // Dialog flags
    var showAddDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    var showRenameDialog by remember { mutableStateOf(false) }
    var renamePresetName by remember { mutableStateOf("") }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Compare mode state
    var compareMode by remember { mutableStateOf(false) }

    // Delta Stats for comparison
    var deltaAtk by remember { mutableStateOf("0") }
    var deltaSkill by remember { mutableStateOf("0") }
    var deltaDmg by remember { mutableStateOf("0") }
    var deltaFd by remember { mutableStateOf("0") }
    var deltaAtkPct by remember { mutableStateOf("0") }
    var deltaBossAtk by remember { mutableStateOf("0") }
    var deltaCritDmg by remember { mutableStateOf("0") }
    var deltaMdc by remember { mutableStateOf("0") }
    var deltaDefense by remember { mutableStateOf("0") }
    var deltaIed by remember { mutableStateOf("0") }

    // Calculations helper
    fun calculateDamage(preset: CalcPreset): DamageResults {
        val atk = preset.atk
        val skill = preset.skillPct
        // Buff additions applied on top of raw stats
        val dmg = preset.dmgPct + (if (preset.buffCandy) 30.0 else 0.0) + (if (preset.buffPork) 20.0 else 0.0)
        val fd = preset.fdPct
        val atkPct = preset.atkPct + (if (preset.buffYogurt) 50.0 else 0.0)
        val bossAtk = preset.bossAtkPct + (if (preset.buffShrimp) 50.0 else 0.0) + (if (preset.buffJellyfish) 20.0 else 0.0) + (if (preset.buffBossRush) 50.0 else 0.0)
        val critDmg = preset.critDmgPct + (if (preset.buffChestnut) 30.0 else 0.0)
        val mdc = preset.mdc
        val defense = preset.bossDefensePct
        val ied = preset.iedPct

        // Formulas:
        // ATK * skill% * (1 + DMG%) * (1 + FD%) * (1 + ATK% + skill% * BossATK%)
        val skillMultiplier = skill / 100.0
        val dmgMultiplier = 1.0 + (dmg / 100.0)
        val fdMultiplier = 1.0 + (fd / 100.0)
        val baseAtkMultiplier = 1.0 + (atkPct / 100.0) + (skillMultiplier * (bossAtk / 100.0))

        val nonCritPotential = atk * skillMultiplier * dmgMultiplier * fdMultiplier * baseAtkMultiplier
        val critPotential = nonCritPotential * (1.0 + (critDmg / 100.0) + 0.2)

        // Boss defense & IED term:
        // DefenseMultiplier = 1 - Boss Defense * (Floor((1 - IED) * (1 - 0.15) * 1000)) / 1000
        val iedFactor = (1.0 - (ied / 100.0)) * 0.85
        val iedTerm = Math.floor(iedFactor * 1000.0 + 1e-9) / 1000.0
        val defMult = (1.0 - (defense / 100.0) * iedTerm).coerceAtLeast(0.0)

        val nonCritDefPotential = nonCritPotential * defMult
        val critDefPotential = critPotential * defMult

        // Capped damage
        val cappedMdc = mdc * defMult
        val nonCritCapped = Math.min(nonCritDefPotential, cappedMdc)
        val critCapped = Math.min(critDefPotential, cappedMdc)

        return DamageResults(
            nonCritPotential = nonCritDefPotential,
            critPotential = critDefPotential,
            nonCritCapped = nonCritCapped,
            critCapped = critCapped
        )
    }

    // Helper text formater
    fun formatDamage(valDouble: Double): String {
        return String.format(Locale.getDefault(), "%,.0f", valDouble)
    }

    // --- Dialog layouts ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Preset", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Preset Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DarkBorder
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            viewModel.addCalcPreset(newPresetName.trim())
                            newPresetName = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.Black)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Preset", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renamePresetName,
                    onValueChange = { renamePresetName = it },
                    label = { Text("New Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DarkBorder
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renamePresetName.isNotBlank()) {
                            viewModel.renameActivePreset(renamePresetName.trim())
                            renamePresetName = ""
                            showRenameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.Black)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Preset", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete preset '${activePreset.name}'?", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteActivePreset()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BreakRed, contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Original calculations
    val currentResults = calculateDamage(activePreset)

    // Comparison calculations
    val comparedPreset = activePreset.copy(
        atk = activePreset.atk + (deltaAtk.toDoubleOrNull() ?: 0.0),
        skillPct = activePreset.skillPct + (deltaSkill.toDoubleOrNull() ?: 0.0),
        dmgPct = activePreset.dmgPct + (deltaDmg.toDoubleOrNull() ?: 0.0),
        fdPct = activePreset.fdPct + (deltaFd.toDoubleOrNull() ?: 0.0),
        atkPct = activePreset.atkPct + (deltaAtkPct.toDoubleOrNull() ?: 0.0),
        bossAtkPct = activePreset.bossAtkPct + (deltaBossAtk.toDoubleOrNull() ?: 0.0),
        critDmgPct = activePreset.critDmgPct + (deltaCritDmg.toDoubleOrNull() ?: 0.0),
        mdc = activePreset.mdc + (deltaMdc.toDoubleOrNull() ?: 0.0),
        bossDefensePct = activePreset.bossDefensePct + (deltaDefense.toDoubleOrNull() ?: 0.0),
        iedPct = activePreset.iedPct + (deltaIed.toDoubleOrNull() ?: 0.0)
    )
    val comparedResults = calculateDamage(comparedPreset)

    @Composable
    fun CompareRow(
        label: String,
        currentVal: Double,
        comparedVal: Double
    ) {
        val diff = comparedVal - currentVal
        val pct = if (currentVal > 0) (diff / currentVal * 100.0) else 0.0
        val diffSign = if (diff >= 0) "+" else ""
        val diffColor = if (diff > 0) UpGreen else if (diff < 0) BreakRed else TextMuted

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, color = TextMuted, fontSize = 12.sp)
                Text(
                    text = "${formatDamage(currentVal)} → ${formatDamage(comparedVal)}",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
            Text(
                text = String.format(Locale.getDefault(), "%s%s (%s%.2f%%)", diffSign, formatDamage(diff), diffSign, pct),
                color = diffColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Column: Stats & Presets
            LazyColumn(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Preset manager card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "CALCULATOR PRESETS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )

                            var expandedDropdown by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1.5f)) {
                                    OutlinedButton(
                                        onClick = { expandedDropdown = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        border = BorderStroke(1.dp, DarkBorder),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                    ) {
                                        Text(activePreset.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }

                                    DropdownMenu(
                                        expanded = expandedDropdown,
                                        onDismissRequest = { expandedDropdown = false },
                                        modifier = Modifier.background(DarkSurface)
                                    ) {
                                        presets.forEachIndexed { idx, preset ->
                                            DropdownMenuItem(
                                                text = { Text(preset.name, color = TextPrimary) },
                                                onClick = {
                                                    viewModel.setActivePresetIndex(idx)
                                                    expandedDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { showAddDialog = true },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Preset", tint = PrimaryPurple)
                                }

                                IconButton(
                                    onClick = {
                                        renamePresetName = activePreset.name
                                        showRenameDialog = true
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename Preset", tint = SecondaryTeal)
                                }

                                IconButton(
                                    onClick = { showDeleteConfirm = true },
                                    enabled = presets.size > 1,
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Preset", tint = BreakRed)
                                }
                            }
                        }
                    }
                }

                // Buff Toggles
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "BUFF TOGGLES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )

                            listOf(
                                Triple("Candy Basket/Cane (DMG +30%)", activePreset.buffCandy) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffCandy = valVal)) },
                                Triple("Chestnut (Crit DMG +30%)", activePreset.buffChestnut) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffChestnut = valVal)) },
                                Triple("Fried Shrimp (Boss ATK +50%)", activePreset.buffShrimp) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffShrimp = valVal)) },
                                Triple("Fruity Yogurt/Grape (ATK +50%)", activePreset.buffYogurt) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffYogurt = valVal)) },
                                Triple("Pork/Snail (DMG +20%)", activePreset.buffPork) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffPork = valVal)) },
                                Triple("Jellyfish (Boss ATK +20%)", activePreset.buffJellyfish) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffJellyfish = valVal)) },
                                Triple("Boss Rush (Boss ATK +50%)", activePreset.buffBossRush) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffBossRush = valVal)) }
                            ).forEach { (label, checked, onToggle) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(label, fontSize = 13.sp, color = TextPrimary)
                                    Switch(
                                        checked = checked,
                                        onCheckedChange = onToggle,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PrimaryPurple,
                                            checkedTrackColor = PrimaryPurple.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Stats inputs
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "CHARACTER STATS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )

                            // List of field editors
                            listOf(
                                Triple("Base Attack (ATK)", activePreset.atk) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(atk = valVal)) },
                                Triple("Skill Damage %", activePreset.skillPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(skillPct = valVal)) },
                                Triple("Increase Damage % (DMG%)", activePreset.dmgPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(dmgPct = valVal)) },
                                Triple("Final Damage % (FD%)", activePreset.fdPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(fdPct = valVal)) },
                                Triple("Increase Attack % (ATK%)", activePreset.atkPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(atkPct = valVal)) },
                                Triple("Boss Attack % (BossATK%)", activePreset.bossAtkPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(bossAtkPct = valVal)) },
                                Triple("Critical Damage %", activePreset.critDmgPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(critDmgPct = valVal)) },
                                Triple("Max Damage Cap (MDC)", activePreset.mdc) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(mdc = valVal)) },
                                Triple("Boss Defense %", activePreset.bossDefensePct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(bossDefensePct = valVal)) },
                                Triple("Ignore Enemy Defense % (IED%)", activePreset.iedPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(iedPct = valVal)) }
                            ).forEach { (label, value, onUpdate) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                    OutlinedTextField(
                                        value = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString(),
                                        onValueChange = {
                                            val numericVal = it.toDoubleOrNull() ?: 0.0
                                            onUpdate(numericVal)
                                        },
                                        modifier = Modifier.width(120.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = PrimaryPurple,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedContainerColor = DarkSurfaceVariant,
                                            unfocusedContainerColor = DarkSurfaceVariant
                                        ),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right Column: Output Results & Compare
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Results Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.5.dp, PrimaryPurple.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "CALCULATED DAMAGE OUTPUT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )

                            // Capped Damage (MDC adjusted)
                            Text("Actual Dealt (Capped)", fontSize = 11.sp, color = TextMuted)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Non-Critical", fontSize = 10.sp, color = TextMuted)
                                        Text(formatDamage(currentResults.nonCritCapped), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Critical Hit", fontSize = 10.sp, color = TextMuted)
                                        Text(formatDamage(currentResults.critCapped), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StarGold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Potential Damage (Uncapped)
                            Text("Potential (Uncapped)", fontSize = 11.sp, color = TextMuted)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Non-Critical", fontSize = 10.sp, color = TextMuted)
                                        Text(formatDamage(currentResults.nonCritPotential), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Critical Hit", fontSize = 10.sp, color = TextMuted)
                                        Text(formatDamage(currentResults.critPotential), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SecondaryTeal)
                                    }
                                }
                            }
                        }
                    }
                }

                // Comparison card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, if (compareMode) SecondaryTeal.copy(alpha = 0.5f) else DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "STAT COMPARISON MODE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Switch(
                                    checked = compareMode,
                                    onCheckedChange = { compareMode = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SecondaryTeal,
                                        checkedTrackColor = SecondaryTeal.copy(alpha = 0.3f)
                                    )
                                )
                            }

                            if (compareMode) {
                                HorizontalDivider(color = DarkBorder)

                                // Delta Inputs
                                Text("Input Stat Increments (Delta)", fontSize = 11.sp, color = TextMuted)
                                listOf(
                                    Triple("Delta ATK", deltaAtk) { str: String -> deltaAtk = str },
                                    Triple("Delta Skill %", deltaSkill) { str: String -> deltaSkill = str },
                                    Triple("Delta DMG %", deltaDmg) { str: String -> deltaDmg = str },
                                    Triple("Delta FD %", deltaFd) { str: String -> deltaFd = str },
                                    Triple("Delta ATK %", deltaAtkPct) { str: String -> deltaAtkPct = str },
                                    Triple("Delta Boss ATK %", deltaBossAtk) { str: String -> deltaBossAtk = str },
                                    Triple("Delta Crit DMG %", deltaCritDmg) { str: String -> deltaCritDmg = str },
                                    Triple("Delta MDC", deltaMdc) { str: String -> deltaMdc = str },
                                    Triple("Delta Defense %", deltaDefense) { str: String -> deltaDefense = str },
                                    Triple("Delta IED %", deltaIed) { str: String -> deltaIed = str }
                                ).forEach { (label, value, onUpdate) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(label, fontSize = 12.sp, color = TextPrimary)
                                        OutlinedTextField(
                                            value = value,
                                            onValueChange = onUpdate,
                                            modifier = Modifier.width(100.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary,
                                                focusedBorderColor = SecondaryTeal,
                                                unfocusedBorderColor = DarkBorder,
                                                focusedContainerColor = DarkSurfaceVariant,
                                                unfocusedContainerColor = DarkSurfaceVariant
                                            ),
                                            singleLine = true
                                        )
                                    }
                                }

                                HorizontalDivider(color = DarkBorder)

                                // Output compare results
                                Text("Comparison Output", fontSize = 11.sp, color = TextMuted)
                                CompareRow("Capped Non-Critical", currentResults.nonCritCapped, comparedResults.nonCritCapped)
                                CompareRow("Capped Critical", currentResults.critCapped, comparedResults.critCapped)
                                CompareRow("Potential Non-Critical", currentResults.nonCritPotential, comparedResults.nonCritPotential)
                                CompareRow("Potential Critical", currentResults.critPotential, comparedResults.critPotential)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Portrait Layout: Stacked scrollable fields
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset manager
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
                            text = "CALCULATOR PRESETS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        var expandedDropdown by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1.5f)) {
                                OutlinedButton(
                                    onClick = { expandedDropdown = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, DarkBorder),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text(activePreset.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                DropdownMenu(
                                    expanded = expandedDropdown,
                                    onDismissRequest = { expandedDropdown = false },
                                    modifier = Modifier.background(DarkSurface)
                                ) {
                                    presets.forEachIndexed { idx, preset ->
                                        DropdownMenuItem(
                                            text = { Text(preset.name, color = TextPrimary) },
                                            onClick = {
                                                viewModel.setActivePresetIndex(idx)
                                                expandedDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { showAddDialog = true },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Preset", tint = PrimaryPurple)
                            }

                            IconButton(
                                onClick = {
                                    renamePresetName = activePreset.name
                                    showRenameDialog = true
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename Preset", tint = SecondaryTeal)
                            }

                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                enabled = presets.size > 1,
                                colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Preset", tint = BreakRed)
                            }
                        }
                    }
                }
            }

            // Buff Toggles
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "BUFF TOGGLES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        listOf(
                            Triple("Candy Basket/Cane (DMG +30%)", activePreset.buffCandy) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffCandy = valVal)) },
                            Triple("Chestnut (Crit DMG +30%)", activePreset.buffChestnut) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffChestnut = valVal)) },
                            Triple("Fried Shrimp (Boss ATK +50%)", activePreset.buffShrimp) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffShrimp = valVal)) },
                            Triple("Fruity Yogurt/Grape (ATK +50%)", activePreset.buffYogurt) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffYogurt = valVal)) },
                            Triple("Pork/Snail (DMG +20%)", activePreset.buffPork) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffPork = valVal)) },
                            Triple("Jellyfish (Boss ATK +20%)", activePreset.buffJellyfish) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffJellyfish = valVal)) },
                            Triple("Boss Rush (Boss ATK +50%)", activePreset.buffBossRush) { valVal: Boolean -> viewModel.updateActivePreset(activePreset.copy(buffBossRush = valVal)) }
                        ).forEach { (label, checked, onToggle) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, fontSize = 13.sp, color = TextPrimary)
                                Switch(
                                    checked = checked,
                                    onCheckedChange = onToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PrimaryPurple,
                                        checkedTrackColor = PrimaryPurple.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Results Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.5.dp, PrimaryPurple.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "CALCULATED DAMAGE OUTPUT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        Text("Actual Dealt (Capped)", fontSize = 11.sp, color = TextMuted)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Non-Critical", fontSize = 10.sp, color = TextMuted)
                                Text(formatDamage(currentResults.nonCritCapped), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Critical Hit", fontSize = 10.sp, color = TextMuted)
                                Text(formatDamage(currentResults.critCapped), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StarGold)
                            }
                        }

                        HorizontalDivider(color = DarkBorder)

                        Text("Potential (Uncapped)", fontSize = 11.sp, color = TextMuted)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Non-Critical", fontSize = 10.sp, color = TextMuted)
                                Text(formatDamage(currentResults.nonCritPotential), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Critical Hit", fontSize = 10.sp, color = TextMuted)
                                Text(formatDamage(currentResults.critPotential), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SecondaryTeal)
                            }
                        }
                    }
                }
            }

            // Stats inputs
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
                            text = "CHARACTER STATS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        listOf(
                            Triple("Base Attack (ATK)", activePreset.atk) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(atk = valVal)) },
                            Triple("Skill Damage %", activePreset.skillPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(skillPct = valVal)) },
                            Triple("Increase Damage % (DMG%)", activePreset.dmgPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(dmgPct = valVal)) },
                            Triple("Final Damage % (FD%)", activePreset.fdPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(fdPct = valVal)) },
                            Triple("Increase Attack % (ATK%)", activePreset.atkPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(atkPct = valVal)) },
                            Triple("Boss Attack % (BossATK%)", activePreset.bossAtkPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(bossAtkPct = valVal)) },
                            Triple("Critical Damage %", activePreset.critDmgPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(critDmgPct = valVal)) },
                            Triple("Max Damage Cap (MDC)", activePreset.mdc) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(mdc = valVal)) },
                            Triple("Boss Defense %", activePreset.bossDefensePct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(bossDefensePct = valVal)) },
                            Triple("Ignore Enemy Defense % (IED%)", activePreset.iedPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(iedPct = valVal)) }
                        ).forEach { (label, value, onUpdate) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString(),
                                    onValueChange = {
                                        val numericVal = it.toDoubleOrNull() ?: 0.0
                                        onUpdate(numericVal)
                                    },
                                    modifier = Modifier.width(110.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = PrimaryPurple,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedContainerColor = DarkSurfaceVariant,
                                        unfocusedContainerColor = DarkSurfaceVariant
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // Compare mode Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, if (compareMode) SecondaryTeal.copy(alpha = 0.5f) else DarkBorder)
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
                                text = "STAT COMPARISON MODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Switch(
                                checked = compareMode,
                                onCheckedChange = { compareMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SecondaryTeal,
                                    checkedTrackColor = SecondaryTeal.copy(alpha = 0.3f)
                                )
                            )
                        }

                        if (compareMode) {
                            HorizontalDivider(color = DarkBorder)

                            Text("Input Stat Increments (Delta)", fontSize = 11.sp, color = TextMuted)
                            listOf(
                                Triple("Delta ATK", deltaAtk) { str: String -> deltaAtk = str },
                                Triple("Delta Skill %", deltaSkill) { str: String -> deltaSkill = str },
                                Triple("Delta DMG %", deltaDmg) { str: String -> deltaDmg = str },
                                Triple("Delta FD %", deltaFd) { str: String -> deltaFd = str },
                                Triple("Delta ATK %", deltaAtkPct) { str: String -> deltaAtkPct = str },
                                Triple("Delta Boss ATK %", deltaBossAtk) { str: String -> deltaBossAtk = str },
                                Triple("Delta Crit DMG %", deltaCritDmg) { str: String -> deltaCritDmg = str },
                                Triple("Delta MDC", deltaMdc) { str: String -> deltaMdc = str },
                                Triple("Delta Defense %", deltaDefense) { str: String -> deltaDefense = str },
                                Triple("Delta IED %", deltaIed) { str: String -> deltaIed = str }
                            ).forEach { (label, value, onUpdate) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(label, fontSize = 12.sp, color = TextPrimary)
                                    OutlinedTextField(
                                        value = value,
                                        onValueChange = onUpdate,
                                        modifier = Modifier.width(90.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = SecondaryTeal,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedContainerColor = DarkSurfaceVariant,
                                            unfocusedContainerColor = DarkSurfaceVariant
                                        ),
                                        singleLine = true
                                    )
                                }
                            }

                            HorizontalDivider(color = DarkBorder)

                            Text("Comparison Output", fontSize = 11.sp, color = TextMuted)
                            CompareRow("Capped Non-Critical", currentResults.nonCritCapped, comparedResults.nonCritCapped)
                            CompareRow("Capped Critical", currentResults.critCapped, comparedResults.critCapped)
                            CompareRow("Potential Non-Critical", currentResults.nonCritPotential, comparedResults.nonCritPotential)
                            CompareRow("Potential Critical", currentResults.critPotential, comparedResults.critPotential)
                        }
                    }
                }
            }
        }
    }
}

data class DamageResults(
    val nonCritPotential: Double,
    val critPotential: Double,
    val nonCritCapped: Double,
    val critCapped: Double
)
