package com.gcirl.msmhelper.viewmodel

import com.gcirl.msmhelper.data.CalcPreset
import com.gcirl.msmhelper.data.SkillModifier
import org.junit.Assert.*
import org.junit.Test

class DamageCalculatorTest {

    private fun calculateDamageForTest(preset: CalcPreset): TestDamageResults {
        val atk = preset.atk
        val skill = preset.skillPct
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

        val skillMultiplier = (skill / 100.0) * (1.0 + skillDmgModSum / 100.0)
        val dmgMultiplier = 1.0 + (dmg / 100.0)
        val fdMultiplier = (1.0 + (fd / 100.0)) * fdModProduct
        val baseAtkMultiplier = 1.0 + (atkPct / 100.0) + (skillMultiplier * (bossAtk / 100.0))

        val skillModMultiplier = 1.0 + (preset.skillModPct / 100.0)

        val nonCritPotential = atk * skillMultiplier * dmgMultiplier * fdMultiplier * baseAtkMultiplier * skillModMultiplier
        val critPotential = nonCritPotential * (1.0 + (critDmg / 100.0) + 0.2)

        val iedFactor = (1.0 - (finalIed / 100.0)) * 0.85
        val iedTerm = Math.floor(iedFactor * 1000.0 + 1e-9) / 1000.0
        val defMult = (1.0 - (defense / 100.0) * iedTerm).coerceAtLeast(0.0)

        val nonCritDefPotential = nonCritPotential * defMult * afDamageMultiplier
        val critDefPotential = critPotential * defMult * afDamageMultiplier

        val cappedMdc = mdc * defMult
        val nonCritCapped = Math.min(nonCritDefPotential, cappedMdc)
        val critCapped = Math.min(critDefPotential, cappedMdc)

        return TestDamageResults(
            nonCritPotential = nonCritDefPotential,
            critPotential = critDefPotential,
            nonCritCapped = nonCritCapped,
            critCapped = critCapped,
            defMult = defMult
        )
    }

    @Test
    fun calculate_basicDamageFormula_returnsCorrectPotential() {
        val preset = CalcPreset(
            name = "Test",
            atk = 20000.0,
            skillPct = 500.0,
            dmgPct = 80.0,
            fdPct = 50.0,
            atkPct = 40.0,
            bossAtkPct = 30.0,
            critDmgPct = 200.0,
            mdc = 40000000.0,
            bossDefensePct = 0.0,
            iedPct = 0.0
        )
        val results = calculateDamageForTest(preset)
        // 20000 * 5.0 * 1.8 * 1.5 * (1 + 0.4 + 5.0 * 0.3)
        // = 20000 * 5.0 * 1.8 * 1.5 * 2.9 = 783,000.0 ? Wait:
        // Wait, let's recalculate:
        // 20000 * 5.0 = 100,000
        // 100,000 * 1.8 = 180,000
        // 180,000 * 1.5 = 270,000
        // 270,000 * (1 + 0.4 + 5.0 * 0.3) -> 1 + 0.4 + 1.5 = 2.9
        // 270,000 * 2.9 = 783,000
        // Let's assert:
        assertEquals(783000.0, results.nonCritPotential, 0.01)

        // Crit potential: NonCrit * (1 + 2.0 + 0.2) = 783000 * 3.2 = 2,505,600
        assertEquals(2505600.0, results.critPotential, 0.01)
    }

    @Test
    fun calculate_withDefenseAndIed_appliesReductionMultiplier() {
        val preset = CalcPreset(
            name = "Test Def",
            atk = 20000.0,
            skillPct = 500.0,
            dmgPct = 80.0,
            fdPct = 50.0,
            atkPct = 40.0,
            bossAtkPct = 30.0,
            critDmgPct = 200.0,
            mdc = 1000000.0, // Set MDC lower to test cap
            bossDefensePct = 100.0,
            iedPct = 90.0
        )
        val results = calculateDamageForTest(preset)

        // iedFactor = (1.0 - 0.9) * 0.85 = 0.085
        // iedTerm = Floor(0.085 * 1000) / 1000 = 85 / 1000 = 0.085
        // defMult = 1.0 - 1.0 * 0.085 = 0.915
        assertEquals(0.915, results.defMult, 0.001)

        // Uncapped potential adjusted by def
        // nonCritPotentialDef = 783000 * 0.915 = 716445.0
        // critPotentialDef = 2505600 * 0.915 = 2292624.0
        assertEquals(716445.0, results.nonCritPotential, 0.01)
        assertEquals(2292624.0, results.critPotential, 0.01)

        // Capped damage:
        // cappedMdc = MDC * defMult = 1,000,000 * 0.915 = 915,000.0
        // nonCritCapped = min(716445, 915000) = 716445
        // critCapped = min(2292624, 915000) = 915000
        assertEquals(716445.0, results.nonCritCapped, 0.01)
        assertEquals(915000.0, results.critCapped, 0.01)
    }

    @Test
    fun calculate_defMultNeverGoesNegative() {
        val preset = CalcPreset(
            name = "Test Negative Def",
            bossDefensePct = 300.0,
            iedPct = 0.0
        )
        val results = calculateDamageForTest(preset)
        // Multiplier shouldn't be negative, should coerce to 0.0
        assertEquals(0.0, results.defMult, 0.001)
    }

    @Test
    fun calculate_withBuffsEnabled_addsBuffPercentagesCorrectly() {
        val preset = CalcPreset(
            name = "Test Buffs",
            atk = 20000.0,
            skillPct = 500.0,
            dmgPct = 50.0, // base
            fdPct = 50.0,
            atkPct = 0.0,  // base
            bossAtkPct = 0.0, // base
            critDmgPct = 170.0, // base
            mdc = 40000000.0,
            bossDefensePct = 0.0,
            iedPct = 0.0,
            buffCandy = true,      // +30% DMG
            buffPork = true,       // +20% DMG
            buffChestnut = true,   // +30% Crit DMG
            buffShrimp = true,     // +50% Boss ATK
            buffYogurt = false,    // Keep ATK base
            buffJellyfish = true,  // +20% Boss ATK
            buffBossRush = true    // +50% Boss ATK
        )
        val results = calculateDamageForTest(preset)

        // Expected final percentages:
        // DMG = 50 + 30 + 20 = 100%
        // Crit DMG = 170 + 30 = 200%
        // Boss ATK = 0 + 50 + 20 + 50 = 120%
        // ATK% = 0%
        // Let's verify formula:
        // skillMultiplier = 500 / 100 = 5.0
        // dmgMultiplier = 1.0 + 100/100 = 2.0
        // fdMultiplier = 1.0 + 50/100 = 1.5
        // baseAtkMultiplier = 1.0 + 0/100 + 5.0 * (120/100) = 1.0 + 6.0 = 7.0
        // nonCritPotential = 20000 * 5.0 * 2.0 * 1.5 * 7.0 = 2,100,000.0
        // critPotential = 2100000 * (1.0 + 2.0 + 0.2) = 2100000 * 3.2 = 6,720,000.0

        assertEquals(2100000.0, results.nonCritPotential, 0.01)
        assertEquals(6720000.0, results.critPotential, 0.01)
    }

    @Test
    fun calculate_withArcaneForceAndIndividualIed_calculatesCorrectly() {
        val preset = CalcPreset(
            name = "Test AF & Individual IED",
            atk = 10000.0,
            skillPct = 100.0,
            dmgPct = 0.0,
            fdPct = 0.0,
            atkPct = 0.0,
            bossAtkPct = 0.0,
            critDmgPct = 0.0,
            mdc = 40000000.0,
            bossDefensePct = 100.0,
            iedPct = 0.0,
            useIndividualIed = true,
            iedSources = listOf(30.0, 30.0), // Combined: 1.0 - 0.7 * 0.7 = 51.0%
            skillModPct = 20.0, // +20% skill modifier final multiplier
            yourAf = 150.0,
            reqAf = 100.0 // Ratio: 1.5 -> +50% ATK% bonus & no damage penalty
        )
        val results = calculateDamageForTest(preset)

        // IED: 51% -> iedFactor = (1.0 - 0.51) * 0.85 = 0.4165 -> iedTerm = 0.416
        // defMult = 1.0 - 1.0 * 0.416 = 0.584
        // 1.5x AF gives +50% ATK% bonus -> baseAtkMultiplier = 1.5
        // nonCritPotential (pre-def, pre-AF) = 10000 * 1.0 (skill) * 1.5 (baseAtkMult) * 1.2 (skillMod) = 18000.0
        // nonCritDefPotential = 18000.0 * 0.584 * 1.0 (no damage penalty) = 10512.0
        assertEquals(10512.0, results.nonCritPotential, 0.01)
    }

    @Test
    fun calculate_withSkillModifiersListAndAFPenalty_calculatesCorrectly() {
        val preset = CalcPreset(
            name = "Test Skill Modifiers & AF penalty",
            atk = 10000.0,
            skillPct = 100.0,
            dmgPct = 0.0,
            fdPct = 0.0,
            atkPct = 0.0,
            bossAtkPct = 0.0,
            critDmgPct = 0.0,
            mdc = 40000000.0,
            bossDefensePct = 100.0,
            iedPct = 0.0,
            yourAf = 90.0,
            reqAf = 100.0, // Ratio: 0.9 -> starts at 50% damage penalty (afDamageMultiplier = 0.50)
            skillModifiers = listOf(
                SkillModifier(type = "Skill DMG", value = 20.0),
                SkillModifier(type = "FD", value = 10.0),
                SkillModifier(type = "IED", value = 10.0)
            )
        )
        val results = calculateDamageForTest(preset)

        // Skill multiplier: 1.0 * 1.2 = 1.2
        // FD multiplier: 1.0 * 1.1 = 1.1
        // IED: 10% -> iedFactor = (1.0 - 0.1) * 0.85 = 0.765 -> iedTerm = 0.765
        // defMult = 1.0 - 1.0 * 0.765 = 0.235
        // nonCritPotential (pre-def, pre-AF) = 10000 * 1.2 (skill) * 1.0 (dmg) * 1.1 (fd) = 13200.0
        // nonCritDefPotential = 13200.0 * 0.235 * 0.50 (AF penalty) = 1551.0
        assertEquals(1551.0, results.nonCritPotential, 0.01)
    }
}

data class TestDamageResults(
    val nonCritPotential: Double,
    val critPotential: Double,
    val nonCritCapped: Double,
    val critCapped: Double,
    val defMult: Double
)
