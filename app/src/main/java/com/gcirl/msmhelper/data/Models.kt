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
data class MSMAppState(
    val characters: List<Character> = emptyList(),
    val activeCharIndex: Int = 0,
    val necroHistory: List<NecroAction> = emptyList(),
    val mastercraftHistory: List<MastercraftAttempt> = emptyList()
)
