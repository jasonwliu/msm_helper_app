package com.gcirl.msmhelper.ui.screens

import android.widget.Toast
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import com.gcirl.msmhelper.theme.*
import com.gcirl.msmhelper.viewmodel.MSMHelperViewModel


@Composable
fun DailyTrackerTab(
    viewModel: MSMHelperViewModel,
    onNavigateToOverview: () -> Unit
) {
    val characters by viewModel.characters.collectAsState()
    val activeIndex by viewModel.activeCharIndex.collectAsState()
    val currentBase by viewModel.currentBase.collectAsState()
    val currentCluster by viewModel.currentCluster.collectAsState()
    val trackedTypeOverride by viewModel.trackedTypeOverride.collectAsState()

    val trackedType = trackedTypeOverride ?: viewModel.getTrackedType()
    val isWeekend = viewModel.isWeekend()

    if (characters.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Characters Added Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your character characters in the Overview tab to start daily crystal piece tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNavigateToOverview,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.Black)
            ) {
                Text("Go to Overview")
            }
        }
        return
    }

    val size = characters.size
    val activeChar = characters.getOrNull(activeIndex) ?: characters.first()
    val prevChar = if (size > 1) characters.getOrNull((activeIndex - 1 + size) % size) else null
    val nextChar = if (size > 1) characters.getOrNull((activeIndex + 1) % size) else null

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Half: Character details and queue
            LazyColumn(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Day Indicator Banner
                item {
                    val indicatorBg = if (trackedType == "weapon") WeaponPinkAlpha else ArmorBlueAlpha
                    val indicatorBorder = if (trackedType == "weapon") WeaponPink else ArmorBlue

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, indicatorBorder),
                        colors = CardDefaults.cardColors(containerColor = indicatorBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = viewModel.getDayLabel(),
                                fontWeight = FontWeight.Bold,
                                color = indicatorBorder,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (trackedTypeOverride != null) {
                                Text(
                                    text = "Reset to Auto",
                                    color = indicatorBorder,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.resetTrackedTypeOverride() }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }

                // Previous Character Card (Muted queue above)
                if (prevChar != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(0.4f),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "↑ Previous: ${prevChar.name}",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("W: ${prevChar.weapon}", fontSize = 11.sp, color = WeaponPink)
                                    Text("A: ${prevChar.armor}", fontSize = 11.sp, color = ArmorBlue)
                                }
                            }
                        }
                    }
                }

                // Active Character Card (Prominent)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.5.dp, PrimaryPurple.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = activeChar.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Weapon count card
                                val isWpnActive = trackedType == "weapon"
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = isWeekend) { viewModel.handleTypeClick("weapon") },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isWpnActive) WeaponPinkAlpha else DarkSurfaceVariant
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isWpnActive) WeaponPink else if (isWeekend) DarkBorder.copy(alpha = 0.5f) else Color.Transparent
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Weapon",
                                            color = if (isWpnActive) WeaponPink else TextMuted,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = activeChar.weapon.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWpnActive) WeaponPink else TextPrimary
                                        )
                                    }
                                }

                                // Armor count card
                                val isArmActive = trackedType == "armor"
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = isWeekend) { viewModel.handleTypeClick("armor") },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isArmActive) ArmorBlueAlpha else DarkSurfaceVariant
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isArmActive) ArmorBlue else if (isWeekend) DarkBorder.copy(alpha = 0.5f) else Color.Transparent
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Armor",
                                            color = if (isArmActive) ArmorBlue else TextMuted,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = activeChar.armor.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isArmActive) ArmorBlue else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Next Character Card (Muted queue below)
                if (nextChar != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(0.4f),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "↓ Up Next: ${nextChar.name}",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("W: ${nextChar.weapon}", fontSize = 11.sp, color = WeaponPink)
                                    Text("A: ${nextChar.armor}", fontSize = 11.sp, color = ArmorBlue)
                                }
                            }
                        }
                    }
                }
            }

            // Column 2: Piece selections
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
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Base Drop Row
                            Column {
                                Text(
                                    text = "BASE DROP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(3, 5, 7).forEach { value ->
                                        val active = currentBase == value
                                        Button(
                                            onClick = { viewModel.setBase(value) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (active) PrimaryPurple else DarkSurfaceVariant,
                                                contentColor = if (active) Color.Black else TextPrimary
                                            ),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, if (active) PrimaryPurple else DarkBorder),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(value.toString(), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Cluster Selection
                            Column {
                                Text(
                                    text = "CLUSTER (OPTIONAL)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                val clusters = listOf(20, 30, 40, 50, 60, 70, 80)
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        clusters.take(4).forEach { value ->
                                            val active = currentCluster == value
                                            Button(
                                                onClick = { viewModel.setCluster(value) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (active) SecondaryTeal else DarkSurfaceVariant,
                                                    contentColor = if (active) Color.Black else TextPrimary
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.dp, if (active) SecondaryTeal else DarkBorder),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        clusters.drop(4).forEach { value ->
                                            val active = currentCluster == value
                                            Button(
                                                onClick = { viewModel.setCluster(value) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (active) SecondaryTeal else DarkSurfaceVariant,
                                                    contentColor = if (active) Color.Black else TextPrimary
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.dp, if (active) SecondaryTeal else DarkBorder),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Column 3: The add or skip buttons
            LazyColumn(
                modifier = Modifier
                    .weight(0.9f)
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
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "COMMIT DROP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val total = currentBase + currentCluster
                            val hasPiecesSelected = total > 0

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total to Add", color = TextPrimary, fontSize = 13.sp)
                                Text("+$total Pieces", color = if (hasPiecesSelected) PrimaryPurple else TextMuted, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = { viewModel.commitDrop() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasPiecesSelected) PrimaryPurple else Color(0xFF2E2E3E),
                                    contentColor = if (hasPiecesSelected) Color.Black else TextMuted
                                ),
                                enabled = hasPiecesSelected,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (hasPiecesSelected) "Add +$total Pieces" else "Select Pieces First",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.nextCharacter() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF444454),
                                    contentColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Skip Without Adding",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // Portrait Mode Layout (Traditional lazy column)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Day Indicator Banner
        item {
            val indicatorBg = if (trackedType == "weapon") WeaponPinkAlpha else ArmorBlueAlpha
            val indicatorBorder = if (trackedType == "weapon") WeaponPink else ArmorBlue

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, indicatorBorder),
                colors = CardDefaults.cardColors(containerColor = indicatorBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.getDayLabel(),
                        fontWeight = FontWeight.Bold,
                        color = indicatorBorder,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (trackedTypeOverride != null) {
                        Text(
                            text = "Reset to Auto",
                            color = indicatorBorder,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.resetTrackedTypeOverride() }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // 2. Previous Character Element (Muted queue above)
        if (prevChar != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.4f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "↑ Previous: ${prevChar.name}",
                            fontSize = 13.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("W: ${prevChar.weapon}", fontSize = 12.sp, color = WeaponPink)
                            Text("A: ${prevChar.armor}", fontSize = 12.sp, color = ArmorBlue)
                        }
                    }
                }
            }
        }

        // 3. Active Character Panel (Fully Prominent)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.5.dp, PrimaryPurple.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeChar.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Stat Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Weapon count card
                        val isWpnActive = trackedType == "weapon"
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = isWeekend) { viewModel.handleTypeClick("weapon") },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isWpnActive) WeaponPinkAlpha else DarkSurfaceVariant
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isWpnActive) WeaponPink else if (isWeekend) DarkBorder.copy(alpha = 0.5f) else Color.Transparent
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Weapon",
                                    color = if (isWpnActive) WeaponPink else TextMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = activeChar.weapon.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWpnActive) WeaponPink else TextPrimary
                                )
                            }
                        }

                        // Armor count card
                        val isArmActive = trackedType == "armor"
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = isWeekend) { viewModel.handleTypeClick("armor") },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isArmActive) ArmorBlueAlpha else DarkSurfaceVariant
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isArmActive) ArmorBlue else if (isWeekend) DarkBorder.copy(alpha = 0.5f) else Color.Transparent
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Armor",
                                    color = if (isArmActive) ArmorBlue else TextMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = activeChar.armor.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isArmActive) ArmorBlue else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Next Character Element (Muted queue below)
        if (nextChar != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.4f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "↓ Up Next: ${nextChar.name}",
                            fontSize = 13.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("W: ${nextChar.weapon}", fontSize = 12.sp, color = WeaponPink)
                            Text("A: ${nextChar.armor}", fontSize = 12.sp, color = ArmorBlue)
                        }
                    }
                }
            }
        }

        // 5. Selection inputs: Base Drop & Clusters
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Base Drop Row
                    Column {
                        Text(
                            text = "BASE DROP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(3, 5, 7).forEach { value ->
                                val active = currentBase == value
                                Button(
                                    onClick = { viewModel.setBase(value) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (active) PrimaryPurple else DarkSurfaceVariant,
                                        contentColor = if (active) Color.Black else TextPrimary
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, if (active) PrimaryPurple else DarkBorder)
                                ) {
                                    Text(value.toString(), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Cluster Selection (20 to 80)
                    Column {
                        Text(
                            text = "CLUSTER (OPTIONAL)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val clusters = listOf(20, 30, 40, 50, 60, 70, 80)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: 20, 30, 40, 50
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                clusters.take(4).forEach { value ->
                                    val active = currentCluster == value
                                    Button(
                                        onClick = { viewModel.setCluster(value) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (active) SecondaryTeal else DarkSurfaceVariant,
                                            contentColor = if (active) Color.Black else TextPrimary
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, if (active) SecondaryTeal else DarkBorder),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(value.toString(), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Row 2: 60, 70, 80
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                clusters.drop(4).forEach { value ->
                                    val active = currentCluster == value
                                    Button(
                                        onClick = { viewModel.setCluster(value) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (active) SecondaryTeal else DarkSurfaceVariant,
                                            contentColor = if (active) Color.Black else TextPrimary
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, if (active) SecondaryTeal else DarkBorder),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(value.toString(), fontWeight = FontWeight.Bold)
                                    }
                                }
                                // Fill space
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    val total = currentBase + currentCluster
                    val hasPiecesSelected = total > 0
                    Button(
                        onClick = { viewModel.commitDrop() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasPiecesSelected) PrimaryPurple else DarkSurfaceVariant,
                            contentColor = if (hasPiecesSelected) Color.Black else TextMuted
                        ),
                        enabled = hasPiecesSelected,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (hasPiecesSelected) "Add $total Pieces" else "Select drop amounts to add",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.nextCharacter() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF444454),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Skip Without Adding",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun OverviewTab(viewModel: MSMHelperViewModel) {
    val characters by viewModel.characters.collectAsState()
    val context = LocalContext.current

    // Dialog state
    var charToDeleteIndex by remember { mutableIntStateOf(-1) }

    // Add Character state variables
    var newName by remember { mutableStateOf("") }
    var newWpn by remember { mutableStateOf("") }
    var newArm by remember { mutableStateOf("") }

    // Deletion confirmation
    if (charToDeleteIndex != -1) {
        val charName = characters.getOrNull(charToDeleteIndex)?.name ?: ""
        AlertDialog(
            onDismissRequest = { charToDeleteIndex = -1 },
            title = { Text("Delete Character?") },
            text = { Text("Are you sure you want to delete '$charName'? All piece tracking data will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCharacter(charToDeleteIndex)
                        charToDeleteIndex = -1
                    }
                ) {
                    Text("Delete", color = BreakRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { charToDeleteIndex = -1 }) {
                    Text("Cancel", color = TextPrimary)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Character list
        itemsIndexed(characters) { index, char ->
            val wStones = char.weapon / 150
            val wRem = char.weapon % 150
            val aStones = char.armor / 150
            val aRem = char.armor % 150

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = char.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Weapon pieces row
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Weapon:",
                                    color = WeaponPink,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(65.dp)
                                )
                                var textValue by remember(char.weapon) { mutableStateOf(char.weapon.toString()) }
                                OutlinedTextField(
                                    value = textValue,
                                    onValueChange = {
                                        textValue = it
                                        val parsed = it.toIntOrNull() ?: 0
                                        viewModel.updateCharacterStat(index, "weapon", parsed)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(80.dp),
                                    textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary)
                                )
                                Text(
                                    text = "($wStones St, $wRem/150)",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Armor pieces row
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Armor:",
                                    color = ArmorBlue,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(65.dp)
                                )
                                var textValue by remember(char.armor) { mutableStateOf(char.armor.toString()) }
                                OutlinedTextField(
                                    value = textValue,
                                    onValueChange = {
                                        textValue = it
                                        val parsed = it.toIntOrNull() ?: 0
                                        viewModel.updateCharacterStat(index, "armor", parsed)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(80.dp),
                                    textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary)
                                )
                                Text(
                                    text = "($aStones St, $aRem/150)",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Actions Column (Up, Down, Delete)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.moveCharacter(index, "up") },
                            enabled = index > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move character up",
                                tint = if (index > 0) PrimaryPurple else TextMuted.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.moveCharacter(index, "down") },
                            enabled = index < characters.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move character down",
                                tint = if (index < characters.size - 1) PrimaryPurple else TextMuted.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = { charToDeleteIndex = index }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete character",
                                tint = BreakRed
                            )
                        }
                    }
                }
            }
        }

        // Add character form
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
                        text = "Add New Character",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Character Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newWpn,
                            onValueChange = { newWpn = it },
                            label = { Text("Initial Weapon") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = newArm,
                            onValueChange = { newArm = it },
                            label = { Text("Initial Armor") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                val wVal = newWpn.toIntOrNull() ?: 0
                                val aVal = newArm.toIntOrNull() ?: 0
                                viewModel.addCharacter(newName, wVal, aVal)
                                newName = ""
                                newWpn = ""
                                newArm = ""
                                Toast.makeText(context, "Added character!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Character", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun StoneMaximizerTab(viewModel: MSMHelperViewModel) {
    val characters by viewModel.characters.collectAsState()
    val context = LocalContext.current

    val weaponPiecesInput by viewModel.weaponPoolInput.collectAsState()
    val armorPiecesInput by viewModel.armorPoolInput.collectAsState()
    val sharedPiecesInput by viewModel.sharedPoolInput.collectAsState()
    val jointCalcResult by viewModel.jointCalcResult.collectAsState()

    if (characters.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Please add characters in the Overview tab first.",
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = "Stone Maximizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Text(
                        text = "Enter unassigned pieces for Weapon, Armor, and/or Shared (Choice) pools to calculate the mathematically optimal distribution to get the maximum number of completed stones (150 pieces = 1 stone).",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = weaponPiecesInput,
                            onValueChange = { viewModel.setWeaponPoolInput(it) },
                            label = { Text("Weapon Pool", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WeaponPink,
                                focusedLabelColor = WeaponPink,
                                cursorColor = WeaponPink
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = armorPiecesInput,
                            onValueChange = { viewModel.setArmorPoolInput(it) },
                            label = { Text("Armor Pool", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArmorBlue,
                                focusedLabelColor = ArmorBlue,
                                cursorColor = ArmorBlue
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = sharedPiecesInput,
                            onValueChange = { viewModel.setSharedPoolInput(it) },
                            label = { Text("Shared Pool", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple,
                                cursorColor = PrimaryPurple
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val wPool = weaponPiecesInput.toIntOrNull() ?: 0
                            val aPool = armorPiecesInput.toIntOrNull() ?: 0
                            val sPool = sharedPiecesInput.toIntOrNull() ?: 0
                            viewModel.setJointCalcResult(viewModel.calculateJointOptimalDistribution(wPool, aPool, sPool))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Calculate Optimal Distributions", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Show Results
        jointCalcResult?.let { result ->
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
                            text = "Calculation Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Stones Created:", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${result.totalStonesCreated} Stones",
                                color = StarGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Weapon Stones: ${result.weaponStonesCreated}", fontSize = 13.sp, color = WeaponPink, fontWeight = FontWeight.SemiBold)
                                Text("Armor Stones: ${result.armorStonesCreated}", fontSize = 13.sp, color = ArmorBlue, fontWeight = FontWeight.SemiBold)
                            }
                            val sharedPoolVal = sharedPiecesInput.toIntOrNull() ?: 0
                            if (sharedPoolVal > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Shared Used (W): +${result.sharedUsedForWeapon}", fontSize = 12.sp, color = TextMuted)
                                    Text("Shared Used (A): +${result.sharedUsedForArmor}", fontSize = 12.sp, color = TextMuted)
                                    val leftover = sharedPoolVal - result.sharedUsedForWeapon - result.sharedUsedForArmor
                                    Text("Leftover Shared: $leftover", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (result.totalStonesCreated > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val wPool = weaponPiecesInput.toIntOrNull() ?: 0
                                    val aPool = armorPiecesInput.toIntOrNull() ?: 0
                                    val sPool = sharedPiecesInput.toIntOrNull() ?: 0
                                    viewModel.craftStonesFromJointResult(
                                        result = result,
                                        weaponPool = wPool,
                                        armorPool = aPool,
                                        sharedPool = sPool
                                    ) { leftoverW, leftoverA, leftoverS ->
                                        viewModel.setWeaponPoolInput(if (leftoverW > 0) leftoverW.toString() else "")
                                        viewModel.setArmorPoolInput(if (leftoverA > 0) leftoverA.toString() else "")
                                        viewModel.setSharedPoolInput(if (leftoverS > 0) leftoverS.toString() else "")
                                        viewModel.setJointCalcResult(null)
                                        Toast.makeText(
                                            context,
                                            "Successfully crafted ${result.totalStonesCreated} stone(s)!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StarGold,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Craft ${result.totalStonesCreated} Stones", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Weapon Results
                    if (result.weaponDistributions.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            border = BorderStroke(1.dp, WeaponPink.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Optimal Weapon Allocations",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WeaponPink
                                )
                                HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                                result.weaponDistributions.forEach { row ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(row.charName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Text("+${row.givenPieces} pieces", color = WeaponPink, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("Result: ", color = TextMuted, fontSize = 12.sp)
                                                        Text("${row.previousPieces} ➔ ${row.currentPieces}", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                                    }
                                                    if (row.stonesAdded > 0) {
                                                        Text(
                                                            text = " (+${row.stonesAdded} Stone${if (row.stonesAdded > 1) "s" else ""}!)",
                                                            color = StarGold,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                }
                                                if (row.stonesAdded > 0) {
                                                    Button(
                                                        onClick = {
                                                            val wPool = weaponPiecesInput.toIntOrNull() ?: 0
                                                            val aPool = armorPiecesInput.toIntOrNull() ?: 0
                                                            val sPool = sharedPiecesInput.toIntOrNull() ?: 0
                                                            viewModel.craftIndividualStone(
                                                                charName = row.charName,
                                                                givenPieces = row.givenPieces,
                                                                currentPieces = row.currentPieces,
                                                                stonesAdded = row.stonesAdded,
                                                                type = "weapon",
                                                                weaponPool = wPool,
                                                                armorPool = aPool,
                                                                sharedPool = sPool
                                                            ) { leftoverW, leftoverA, leftoverS ->
                                                                viewModel.setWeaponPoolInput(if (leftoverW > 0) leftoverW.toString() else "")
                                                                viewModel.setArmorPoolInput(if (leftoverA > 0) leftoverA.toString() else "")
                                                                viewModel.setSharedPoolInput(if (leftoverS > 0) leftoverS.toString() else "")
                                                                viewModel.setJointCalcResult(null)
                                                                Toast.makeText(
                                                                    context,
                                                                    "Successfully crafted ${row.stonesAdded} stone(s) for ${row.charName}!",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = StarGold, contentColor = Color.Black),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Text("Craft", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Armor Results
                    if (result.armorDistributions.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            border = BorderStroke(1.dp, ArmorBlue.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Optimal Armor Allocations",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ArmorBlue
                                )
                                HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                                result.armorDistributions.forEach { row ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(row.charName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Text("+${row.givenPieces} pieces", color = ArmorBlue, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("Result: ", color = TextMuted, fontSize = 12.sp)
                                                        Text("${row.previousPieces} ➔ ${row.currentPieces}", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                                    }
                                                    if (row.stonesAdded > 0) {
                                                        Text(
                                                            text = " (+${row.stonesAdded} Stone${if (row.stonesAdded > 1) "s" else ""}!)",
                                                            color = StarGold,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                }
                                                if (row.stonesAdded > 0) {
                                                    Button(
                                                        onClick = {
                                                            val wPool = weaponPiecesInput.toIntOrNull() ?: 0
                                                            val aPool = armorPiecesInput.toIntOrNull() ?: 0
                                                            val sPool = sharedPiecesInput.toIntOrNull() ?: 0
                                                            viewModel.craftIndividualStone(
                                                                charName = row.charName,
                                                                givenPieces = row.givenPieces,
                                                                currentPieces = row.currentPieces,
                                                                stonesAdded = row.stonesAdded,
                                                                type = "armor",
                                                                weaponPool = wPool,
                                                                armorPool = aPool,
                                                                sharedPool = sPool
                                                            ) { leftoverW, leftoverA, leftoverS ->
                                                                viewModel.setWeaponPoolInput(if (leftoverW > 0) leftoverW.toString() else "")
                                                                viewModel.setArmorPoolInput(if (leftoverA > 0) leftoverA.toString() else "")
                                                                viewModel.setSharedPoolInput(if (leftoverS > 0) leftoverS.toString() else "")
                                                                viewModel.setJointCalcResult(null)
                                                                Toast.makeText(
                                                                    context,
                                                                    "Successfully crafted ${row.stonesAdded} stone(s) for ${row.charName}!",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = StarGold, contentColor = Color.Black),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Text("Craft", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
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
fun ReadyToCraftTab(viewModel: MSMHelperViewModel) {
    val characters by viewModel.characters.collectAsState()
    val context = LocalContext.current

    val readyCharacters = remember(characters) {
        characters.filter { it.weapon >= 150 || it.armor >= 150 }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ready to Craft Stones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StarGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Characters with 150 or more weapon or armor pieces can craft a Necro Stone here. Crafting will subtract 150 pieces.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (readyCharacters.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No characters ready to craft a stone yet.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            itemsIndexed(readyCharacters) { _, char ->
                val charIndexInMainList = characters.indexOf(char)
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
                            text = char.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Weapon Column
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Weapon Pieces",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WeaponPink,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${char.weapon} pieces",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                val weaponStones = char.weapon / 150
                                Text(
                                    text = if (weaponStones > 0) "$weaponStones stone${if (weaponStones > 1) "s" else ""} ready!" else "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarGold,
                                    modifier = Modifier.height(18.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (charIndexInMainList != -1) {
                                            viewModel.useStone(charIndexInMainList, "weapon")
                                            Toast.makeText(context, "Crafted 1 Weapon Stone for ${char.name}!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = char.weapon >= 150,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WeaponPink,
                                        contentColor = Color.Black,
                                        disabledContainerColor = DarkSurfaceVariant,
                                        disabledContentColor = TextMuted
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Craft Weapon", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            // Armor Column
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Armor Pieces",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArmorBlue,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${char.armor} pieces",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                val armorStones = char.armor / 150
                                Text(
                                    text = if (armorStones > 0) "$armorStones stone${if (armorStones > 1) "s" else ""} ready!" else "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarGold,
                                    modifier = Modifier.height(18.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (charIndexInMainList != -1) {
                                            viewModel.useStone(charIndexInMainList, "armor")
                                            Toast.makeText(context, "Crafted 1 Armor Stone for ${char.name}!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = char.armor >= 150,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ArmorBlue,
                                        contentColor = Color.Black,
                                        disabledContainerColor = DarkSurfaceVariant,
                                        disabledContentColor = TextMuted
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Craft Armor", fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
fun BackupRestoreTab(viewModel: MSMHelperViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var importJsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonText = inputStream?.bufferedReader()?.use { it.readText() }
                if (jsonText != null) {
                    val success = viewModel.importBackupJson(jsonText.trim())
                    if (success) {
                        Toast.makeText(context, "Data imported successfully!", Toast.LENGTH_SHORT).show()
                        showImportDialog = false
                    } else {
                        Toast.makeText(context, "Failed to parse JSON file.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to read file.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val recoverableAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.cloudBackupNow()
        }
    }

    val recoverableIntent by viewModel.recoverableAuthIntent.collectAsState()
    LaunchedEffect(recoverableIntent) {
        recoverableIntent?.let {
            recoverableAuthLauncher.launch(it)
            viewModel.clearRecoverableAuthIntent()
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Backup JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Upload a backup JSON file or paste the exported JSON string below:", fontSize = 12.sp, color = TextMuted)
                    
                    Button(
                        onClick = {
                            filePickerLauncher.launch("application/json")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Choose Backup JSON File", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = DarkBorder, thickness = 0.5.dp)
                        Text(" OR ", fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 8.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = DarkBorder, thickness = 0.5.dp)
                    }

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("Paste JSON string here...", color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            val success = viewModel.importBackupJson(importJsonText.trim())
                            if (success) {
                                Toast.makeText(context, "Data imported successfully!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                                importJsonText = ""
                            } else {
                                Toast.makeText(context, "Failed to parse JSON. Please verify.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Import", color = PrimaryPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = TextPrimary)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Backup & Restore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage your MSM Helper app data. Backups contain all characters, piece counts, and Necro tracker history.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Google Cloud Sync Card
        item {
            val googleUserEmail by viewModel.googleUserEmail.collectAsState()
            val lastCloudSyncTime by viewModel.lastCloudSyncTime.collectAsState()
            val autoSyncToCloud by viewModel.autoSyncToCloud.collectAsState()
            val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()

            val signInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                    viewModel.handleGoogleSignInResult(account)
                    Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    val details = if (e is com.google.android.gms.common.api.ApiException) {
                        "ApiException code ${e.statusCode}: ${e.status.statusMessage ?: e.message}"
                    } else {
                        "${e.javaClass.simpleName}: ${e.message}"
                    }
                    Toast.makeText(context, "Sign-in failed: $details", Toast.LENGTH_LONG).show()
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, if (googleUserEmail != null) PrimaryPurple.copy(alpha = 0.5f) else DarkBorder)
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
                            text = "Google Drive Cloud Backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        if (isCloudSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = PrimaryPurple,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    if (googleUserEmail == null) {
                        Text(
                            text = "Link your Google Account to back up characters and Necro tracker history automatically to your personal Google Drive (AppData). This is private, completely free, and secure.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                signInLauncher.launch(viewModel.googleDriveSyncManager.getSignInIntent())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Sign in with Google", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Logged in: $googleUserEmail",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Last Synced: ${lastCloudSyncTime ?: "Never"}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            
                            HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Auto-Sync Changes", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Upload backup instantly to Google Drive on changes", fontSize = 11.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = autoSyncToCloud,
                                    onCheckedChange = { viewModel.setAutoSyncToCloud(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple)
                                )
                            }

                            HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.cloudBackupNow { success ->
                                            if (success) {
                                                Toast.makeText(context, "Cloud backup completed!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val errMsg = viewModel.syncErrorMessage.value ?: "Unknown backup error"
                                                Toast.makeText(context, "Cloud backup failed: $errMsg", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal, contentColor = Color.Black),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Backup Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.cloudRestoreNow { success ->
                                            if (success) {
                                                Toast.makeText(context, "Cloud restore completed!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val errMsg = viewModel.syncErrorMessage.value ?: "Unknown restore error"
                                                Toast.makeText(context, "Cloud restore failed: $errMsg", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B4A66), contentColor = TextPrimary),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Restore Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = { viewModel.performGoogleSignOut() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF), contentColor = BreakRed),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Local Backups Card
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
                        text = "Local Backups",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal
                    )
                    Text(
                        text = "Export your app backup data as JSON text to your clipboard or import saved JSON backup text.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val jsonStr = viewModel.exportBackupJson()
                                if (jsonStr.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(jsonStr))
                                    Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No data found to export.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export JSON", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                importJsonText = ""
                                showImportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C3D52), contentColor = TextPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Import JSON", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

