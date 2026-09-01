package com.gcirl.msmhelper.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gcirl.msmhelper.theme.*
import com.gcirl.msmhelper.viewmodel.MSMHelperViewModel

@Composable
fun SettingsScreen(
    viewModel: MSMHelperViewModel,
    onNavigateToBackup: () -> Unit
) {
    val nextCharStoneMode by viewModel.nextCharStoneMode.collectAsState()
    val reorderModeEnabled by viewModel.reorderModeEnabled.collectAsState()
    val autoSyncToCloud by viewModel.autoSyncToCloud.collectAsState()
    val googleUserEmail by viewModel.googleUserEmail.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECTION 1: NECRO TRACKER ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💎", fontSize = 18.sp)
                        Text(
                            text = "NECRO TRACKER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple,
                            letterSpacing = 1.sp
                        )
                    }

                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                    // Setting 1: Default stone when moving to next character
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Next Character Stone Selection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Select which stone type is active when advancing to the next character in Daily Tracker.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 4 Options: Armor, Weapon, Keep (Global), Per-Character Keep
                        val row1Options = listOf(
                            Triple("armor", "🛡️ Armor", ArmorBlue),
                            Triple("weapon", "⚔️ Weapon", WeaponPink)
                        )
                        val row2Options = listOf(
                            Triple("keep", "🔄 Keep (Global)", PrimaryPurple),
                            Triple("per_character", "👤 Per-Character", Color(0xFFE5A93C))
                        )

                        listOf(row1Options, row2Options).forEachIndexed { rowIndex, rowList ->
                            if (rowIndex > 0) Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowList.forEach { (mode, label, accentColor) ->
                                    val isSelected = nextCharStoneMode == mode
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.setNextCharStoneMode(mode) },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) accentColor.copy(alpha = 0.18f) else DarkSurfaceVariant
                                        ),
                                        border = BorderStroke(
                                            if (isSelected) 1.5.dp else 1.dp,
                                            if (isSelected) accentColor else DarkBorder
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = label,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = if (isSelected) accentColor else TextMuted
                                            )
                                            if (isSelected) {
                                                Text(
                                                    text = "Active",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentColor,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Explanation of current mode
                        val modeDescription = when (nextCharStoneMode) {
                            "weapon" -> "Automatically resets to Weapon stone when advancing characters."
                            "keep" -> "Keeps whatever stone type (Armor or Weapon) you last selected on the previous character."
                            "per_character" -> "Remembers and keeps each character's own individual stone selection (Armor or Weapon)."
                            else -> "Automatically resets to Armor stone when advancing characters (Default)."
                        }
                        Text(
                            text = modeDescription,
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                    // Setting 2: Character Rearrange Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Character Rearrange Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Allow dragging and dropping character cards to reorder them in the Overview tab.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = reorderModeEnabled,
                            onCheckedChange = { viewModel.setReorderModeEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryPurple,
                                checkedTrackColor = PrimaryPurple.copy(alpha = 0.5f)
                            )
                        )
                    }

                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                    // Setting 3: Daily Reset Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Server Reset",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Automatically restarts queue to the top character at 00:00 Midnight Server Time (GMT-8).",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Surface(
                            color = Color(0x22C59BFF),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "00:00 GMT-8",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 2: CLOUD & GOOGLE DRIVE SYNC ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("☁️", fontSize = 18.sp)
                        Text(
                            text = "GOOGLE DRIVE CLOUD SYNC",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple,
                            letterSpacing = 1.sp
                        )
                    }

                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                    // Cloud Account Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Account",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = googleUserEmail ?: "Not signed in",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (googleUserEmail != null) UpGreen else TextMuted
                            )
                        }
                        if (googleUserEmail != null) {
                            Surface(
                                color = UpGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, UpGreen.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Connected",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UpGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                    // Auto-Sync Changes Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Sync Changes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Upload backup silently to Google Drive whenever changes are made.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = autoSyncToCloud,
                            onCheckedChange = { viewModel.setAutoSyncToCloud(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryPurple,
                                checkedTrackColor = PrimaryPurple.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }

        // --- SECTION 3: DATA & BACKUP ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💾", fontSize = 18.sp)
                        Text(
                            text = "DATA & STORAGE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple,
                            letterSpacing = 1.sp
                        )
                    }

                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Backup & Restore",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Export or import local JSON backups and manage Google Drive files.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Button(
                            onClick = onNavigateToBackup,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Open",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 4: ABOUT ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("ℹ️", fontSize = 18.sp)
                        Text(
                            text = "ABOUT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "MapleStory M Helper App",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Necro tracker, Mastercrafting simulator & tracker, Boss accessory drop tracker, and Damage calculator.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
