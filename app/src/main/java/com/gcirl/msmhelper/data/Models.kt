package com.gcirl.msmhelper.data

import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val name: String,
    val weapon: Int = 0,
    val armor: Int = 0
)

@Serializable
data class AffectedCharacter(
    val charName: String,
    val statType: String, // "weapon" or "armor"
    val oldPieces: Int,
    val newPieces: Int
)

@Serializable
data class NecroAction(
    val actionType: String, // "add_drop", "craft_stone", "batch_craft"
    val timestamp: String,
    val affected: List<AffectedCharacter> = emptyList(),
    val base: Int = 0,
    val cluster: Int = 0,
    val oldWeaponPool: String = "",
    val oldArmorPool: String = "",
    val oldSharedPool: String = ""
)

@Serializable
data class MastercraftAttempt(
    val timestamp: String,
    val gearType: String, // "necro", "inherit", "chaos", "absolab", "arcane"
    val isRefined: Boolean,
    val luckyScrolls: List<Int> = emptyList(),
    val totalRate: Int,
    val isSuccess: Boolean
)

@Serializable
data class BossAccessoryAttempt(
    val timestamp: String,
    val isSuccess: Boolean // true = Drop, false = No Drop
)

@Serializable
data class CalcPreset(
    val name: String,
    val atk: Double = 0.0,
    val skillPct: Double = 0.0,
    val dmgPct: Double = 0.0,
    val fdPct: Double = 0.0,
    val atkPct: Double = 0.0,
    val bossAtkPct: Double = 0.0,
    val critDmgPct: Double = 0.0,
    val mdc: Double = 40000000.0, // Default 40M
    val bossDefensePct: Double = 0.0,
    val iedPct: Double = 0.0,
    val buffCandy: Boolean = false,
    val buffChestnut: Boolean = false,
    val buffShrimp: Boolean = false,
    val buffYogurt: Boolean = false,
    val buffPork: Boolean = false,
    val buffJellyfish: Boolean = false,
    val buffBossRush: Boolean = false,
    val skillModPct: Double = 0.0,
    val yourAf: Double = 0.0,
    val reqAf: Double = 0.0,
    val useIndividualIed: Boolean = false,
    val iedSources: List<Double> = emptyList()
)

@Serializable
data class MSMAppState(
    val characters: List<Character> = emptyList(),
    val activeCharIndex: Int = 0,
    val necroHistory: List<NecroAction> = emptyList(),
    val mastercraftHistory: List<MastercraftAttempt> = emptyList(),
    val bossAccessoryHistory: List<BossAccessoryAttempt> = emptyList(),
    val calcPresets: List<CalcPreset> = emptyList(),
    val activePresetIndex: Int = 0,
    val selectedTab: Int = 0
)
