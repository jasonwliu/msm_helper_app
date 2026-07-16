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

@Composable
fun DmgCalcTextField(
    value: Double,
    onUpdate: (Double) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    borderColor: Color = PrimaryPurple
) {
    var textState by remember(value) {
        mutableStateOf(if (value == 0.0) "" else {
            if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
        })
    }

    OutlinedTextField(
        value = textState,
        onValueChange = { newValue ->
            textState = newValue
            val parsed = newValue.toDoubleOrNull() ?: 0.0
            onUpdate(parsed)
        },
        label = { Text(label, fontSize = 11.sp) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = DarkBorder,
            focusedContainerColor = DarkSurfaceVariant,
            unfocusedContainerColor = DarkSurfaceVariant
        ),
        singleLine = true
    )
}

@Composable
fun CharacterStatsContent(
    activePreset: CalcPreset,
    viewModel: MSMHelperViewModel
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CHARACTER STATS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("?", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Standard fields
        listOf(
            Triple("ATK", activePreset.atk) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(atk = valVal)) },
            Triple("Skill %", activePreset.skillPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(skillPct = valVal)) },
            Triple("DMG %", activePreset.dmgPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(dmgPct = valVal)) },
            Triple("FD %", activePreset.fdPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(fdPct = valVal)) },
            Triple("ATK %", activePreset.atkPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(atkPct = valVal)) },
            Triple("Boss ATK %", activePreset.bossAtkPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(bossAtkPct = valVal)) },
            Triple("Crit Dmg %", activePreset.critDmgPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(critDmgPct = valVal)) },
            Triple("MDC", activePreset.mdc) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(mdc = valVal)) },
            Triple("Boss Def %", activePreset.bossDefensePct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(bossDefensePct = valVal)) },
            Triple("Skill Mod %", activePreset.skillModPct) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(skillModPct = valVal)) },
            Triple("Your AF", activePreset.yourAf) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(yourAf = valVal)) },
            Triple("Req AF", activePreset.reqAf) { valVal: Double -> viewModel.updateActivePreset(activePreset.copy(reqAf = valVal)) }
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
                DmgCalcTextField(
                    value = value,
                    onUpdate = onUpdate,
                    label = label,
                    modifier = Modifier.width(120.dp)
                )
            }
        }

        HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

        // IED Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Use Individual IED Sources",
                fontSize = 13.sp,
                color = TextPrimary
            )
            Switch(
                checked = activePreset.useIndividualIed,
                onCheckedChange = { valVal ->
                    viewModel.updateActivePreset(activePreset.copy(useIndividualIed = valVal))
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PrimaryPurple,
                    checkedTrackColor = PrimaryPurple.copy(alpha = 0.3f)
                )
            )
        }

        if (activePreset.useIndividualIed) {
            val computedIed = if (activePreset.iedSources.isNotEmpty()) {
                val product = activePreset.iedSources.fold(1.0) { acc, source -> acc * (1.0 - (source / 100.0)) }
                (1.0 - product) * 100.0
            } else {
                0.0
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Combined Total IED %", fontSize = 13.sp, color = TextMuted)
                Text(String.format(Locale.US, "%.2f%%", computedIed), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activePreset.iedSources.forEachIndexed { idx, sourceValue ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Source ${idx + 1}", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        DmgCalcTextField(
                            value = sourceValue,
                            onUpdate = { newVal ->
                                val newList = activePreset.iedSources.toMutableList()
                                newList[idx] = newVal
                                viewModel.updateActivePreset(activePreset.copy(iedSources = newList))
                            },
                            label = "IED %",
                            modifier = Modifier.width(100.dp)
                        )
                        IconButton(
                            onClick = {
                                val newList = activePreset.iedSources.toMutableList()
                                newList.removeAt(idx)
                                viewModel.updateActivePreset(activePreset.copy(iedSources = newList))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("❌", color = BreakRed, fontSize = 14.sp)
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.updateActivePreset(
                            activePreset.copy(iedSources = activePreset.iedSources + 0.0)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Add IED Source", color = PrimaryPurple, fontSize = 12.sp)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "IED %",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1.5f)
                )
                DmgCalcTextField(
                    value = activePreset.iedPct,
                    onUpdate = { valVal ->
                        viewModel.updateActivePreset(activePreset.copy(iedPct = valVal))
                    },
                    label = "IED %",
                    modifier = Modifier.width(120.dp)
                )
            }
        }

        HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

        var skillModifiersExpanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { skillModifiersExpanded = !skillModifiersExpanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SKILL MODIFIERS LIST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Text(
                text = if (skillModifiersExpanded) "Collapse ▲" else "Expand ▼",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
        }

        if (skillModifiersExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activePreset.skillModifiers.forEachIndexed { idx, modifier ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = BorderStroke(1.dp, DarkBorder)
                            ) {
                                Text(modifier.type, fontSize = 12.sp, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                listOf("Skill DMG", "FD", "IED").forEach { typeOption ->
                                    DropdownMenuItem(
                                        text = { Text(typeOption, color = TextPrimary) },
                                        onClick = {
                                            val newList = activePreset.skillModifiers.toMutableList()
                                            newList[idx] = modifier.copy(type = typeOption)
                                            viewModel.updateActivePreset(activePreset.copy(skillModifiers = newList))
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        DmgCalcTextField(
                            value = modifier.value,
                            onUpdate = { newVal ->
                                val newList = activePreset.skillModifiers.toMutableList()
                                newList[idx] = modifier.copy(value = newVal)
                                viewModel.updateActivePreset(activePreset.copy(skillModifiers = newList))
                            },
                            label = "Value %",
                            modifier = Modifier.width(90.dp)
                        )

                        IconButton(
                            onClick = {
                                val newList = activePreset.skillModifiers.toMutableList()
                                newList.removeAt(idx)
                                viewModel.updateActivePreset(activePreset.copy(skillModifiers = newList))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("❌", color = BreakRed, fontSize = 14.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Skill DMG", "FD", "IED").forEach { typeName ->
                        Button(
                            onClick = {
                                viewModel.updateActivePreset(
                                    activePreset.copy(
                                        skillModifiers = activePreset.skillModifiers + SkillModifier(type = typeName, value = 0.0)
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple.copy(alpha = 0.1f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("+ $typeName", color = PrimaryPurple, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Damage Calc Legend", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• ATK: Base Physical or Magic Attack power.", color = TextPrimary, fontSize = 13.sp)
                    Text("• Skill %: Base damage scaling percentage from skill description.", color = TextPrimary, fontSize = 13.sp)
                    Text("• DMG %: Total damage increase stats.", color = TextPrimary, fontSize = 13.sp)
                    Text("• FD %: Final Damage multiplier (separate multiplicative factor).", color = TextPrimary, fontSize = 13.sp)
                    Text("• ATK %: Attack percentage modifier.", color = TextPrimary, fontSize = 13.sp)
                    Text("• Boss ATK %: Boss Attack percentage modifier.", color = TextPrimary, fontSize = 13.sp)
                    Text("• Crit Dmg %: Critical Damage percentage.", color = TextPrimary, fontSize = 13.sp)
                    Text("• MDC: Max Damage Cap. Default is 40,000,000.", color = TextPrimary, fontSize = 13.sp)
                    Text("• Boss Def %: Boss defense rate (usually 100% or more for high bosses).", color = TextPrimary, fontSize = 13.sp)
                    Text("• IED %: Ignore Enemy Defense percentage (reduces boss defense penalty). Supports adding multiple individual multiplicative sources.", color = TextPrimary, fontSize = 13.sp)
                    Text("• Skill Mod %: Final multiplier for specific skill levels/reinforcements (e.g. Hyper Skill Reinforce passive +20%, node boosts).", color = TextPrimary, fontSize = 13.sp)
                    Text("• Your AF / Req AF: Arcane Force scaling. Having >= 1.5x gives +50% ATK% & +5.4M MDC; >= 1.4x gives +30% ATK% & +2.7M MDC; >= 1.3x gives +30% ATK% & +2.16M MDC; >= 1.2x gives +15% ATK% & +1.08M MDC; >= 1.1x gives +15% ATK% & +540K MDC. Under 1.0x AF starts at a 50% damage penalty, increasing by 10% penalty for each 0.1x ratio drop down to a cap of 99% penalty.", color = TextPrimary, fontSize = 13.sp)
                    Text("• Skill Modifiers List: Add custom multipliers for Node boosts (+Skill DMG), Hyper skill passive boosters (+FD), or specific skill ignore defense stats (+IED).", color = TextPrimary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Close", color = PrimaryPurple)
                }
            },
            containerColor = DarkSurface
        )
    }
}

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
    var buffsExpanded by remember { mutableStateOf(false) }

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
        
        // Arcane Force (AF) parameters
        val ratio = if (preset.reqAf > 0.0) preset.yourAf / preset.reqAf else 1.0
        var afAtkPctBonus = 0.0
        var afMdcBonus = 0.0
        var afDamageMultiplier = 1.0

        if (preset.reqAf > 0.0) {
            when {
                ratio >= 1.5 -> {
                    afAtkPctBonus = 50.0
                    afMdcBonus = 5400000.0
                }
                ratio >= 1.4 -> {
                    afAtkPctBonus = 30.0
                    afMdcBonus = 2700000.0
                }
                ratio >= 1.3 -> {
                    afAtkPctBonus = 30.0
                    afMdcBonus = 2160000.0
                }
                ratio >= 1.2 -> {
                    afAtkPctBonus = 15.0
                    afMdcBonus = 1080000.0
                }
                ratio >= 1.1 -> {
                    afAtkPctBonus = 15.0
                    afMdcBonus = 540000.0
                }
                ratio >= 1.0 -> {
                    // No bonus, no penalty
                }
                ratio >= 0.9 -> {
                    afDamageMultiplier = 0.50
                }
                ratio >= 0.8 -> {
                    afDamageMultiplier = 0.40
                }
                ratio >= 0.7 -> {
                    afDamageMultiplier = 0.30
                }
                ratio >= 0.6 -> {
                    afDamageMultiplier = 0.20
                }
                ratio >= 0.5 -> {
                    afDamageMultiplier = 0.10
                }
                else -> {
                    afDamageMultiplier = 0.01 // 99% penalty
                }
            }
        }

        val atkPct = preset.atkPct + (if (preset.buffYogurt) 50.0 else 0.0) + afAtkPctBonus
        val bossAtk = preset.bossAtkPct + (if (preset.buffShrimp) 50.0 else 0.0) + (if (preset.buffJellyfish) 20.0 else 0.0) + (if (preset.buffBossRush) 50.0 else 0.0)
        val critDmg = preset.critDmgPct + (if (preset.buffChestnut) 30.0 else 0.0)
        val mdc = preset.mdc + afMdcBonus
        val defense = preset.bossDefensePct
        
        // IED calculation (individual vs total)
        val baseIed = if (preset.useIndividualIed && preset.iedSources.isNotEmpty()) {
            val product = preset.iedSources.fold(1.0) { acc, source -> acc * (1.0 - (source / 100.0)) }
            (1.0 - product) * 100.0
        } else {
            preset.iedPct
        }

        // Apply Skill Modifiers
        val skillDmgModSum = preset.skillModifiers.filter { it.type == "Skill DMG" }.sumOf { it.value }
        val fdModProduct = preset.skillModifiers.filter { it.type == "FD" }.fold(1.0) { acc, mod -> acc * (1.0 + mod.value / 100.0) }
        val iedModProduct = preset.skillModifiers.filter { it.type == "IED" }.fold(1.0) { acc, mod -> acc * (1.0 - mod.value / 100.0) }

        val finalIed = (1.0 - (1.0 - baseIed / 100.0) * iedModProduct) * 100.0

        // Formulas:
        // ATK * skillMultiplier * (1 + DMG%) * fdMultiplier * baseAtkMultiplier
        val skillMultiplier = (skill / 100.0) * (1.0 + skillDmgModSum / 100.0)
        val dmgMultiplier = 1.0 + (dmg / 100.0)
        val fdMultiplier = (1.0 + (fd / 100.0)) * fdModProduct
        val baseAtkMultiplier = 1.0 + (atkPct / 100.0) + (skillMultiplier * (bossAtk / 100.0))

        val skillModMultiplier = 1.0 + (preset.skillModPct / 100.0)

        val nonCritPotential = atk * skillMultiplier * dmgMultiplier * fdMultiplier * baseAtkMultiplier * skillModMultiplier
        val critPotential = nonCritPotential * (1.0 + (critDmg / 100.0) + 0.2)

        // Boss defense & IED term:
        // DefenseMultiplier = 1 - Boss Defense * (Floor((1 - IED) * (1 - 0.15) * 1000)) / 1000
        val iedFactor = (1.0 - (finalIed / 100.0)) * 0.85
        val iedTerm = Math.floor(iedFactor * 1000.0 + 1e-9) / 1000.0
        val defMult = (1.0 - (defense / 100.0) * iedTerm).coerceAtLeast(0.0)

        // Final potential values scaled by AF multiplier and defense multiplier
        val nonCritDefPotential = nonCritPotential * defMult * afDamageMultiplier
        val critDefPotential = critPotential * defMult * afDamageMultiplier

        // Capped damage using boosted MDC
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { buffsExpanded = !buffsExpanded }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "BUFF TOGGLES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (buffsExpanded) "Collapse ▲" else "Expand ▼",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple
                                )
                            }

                            if (buffsExpanded) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                    }
                }

                // Stats inputs
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        CharacterStatsContent(activePreset = activePreset, viewModel = viewModel)
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { buffsExpanded = !buffsExpanded }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BUFF TOGGLES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (buffsExpanded) "Collapse ▲" else "Expand ▼",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                        }

                        if (buffsExpanded) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                    CharacterStatsContent(activePreset = activePreset, viewModel = viewModel)
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
