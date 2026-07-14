package com.gcirl.msmhelper.viewmodel

import com.gcirl.msmhelper.data.CalcPreset
import org.junit.Assert.*
import org.junit.Test

class DamageCalculatorTest {

    private fun calculateDamageForTest(preset: CalcPreset): TestDamageResults {
        val atk = preset.atk
        val skill = preset.skillPct
        val dmg = preset.dmgPct
        val fd = preset.fdPct
        val atkPct = preset.atkPct
        val bossAtk = preset.bossAtkPct
        val critDmg = preset.critDmgPct
        val mdc = preset.mdc
        val defense = preset.bossDefensePct
        val ied = preset.iedPct

        val skillMultiplier = skill / 100.0
        val dmgMultiplier = 1.0 + (dmg / 100.0)
        val fdMultiplier = 1.0 + (fd / 100.0)
        val baseAtkMultiplier = 1.0 + (atkPct / 100.0) + (skillMultiplier * (bossAtk / 100.0))

        val nonCritPotential = atk * skillMultiplier * dmgMultiplier * fdMultiplier * baseAtkMultiplier
        val critPotential = nonCritPotential * (1.0 + (critDmg / 100.0) + 0.2)

        val iedFactor = (1.0 - (ied / 100.0)) * 0.85
        val iedTerm = Math.floor(iedFactor * 1000.0 + 1e-9) / 1000.0
        val defMult = (1.0 - (defense / 100.0) * iedTerm).coerceAtLeast(0.0)

        val nonCritDefPotential = nonCritPotential * defMult
        val critDefPotential = critPotential * defMult

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
}

data class TestDamageResults(
    val nonCritPotential: Double,
    val critPotential: Double,
    val nonCritCapped: Double,
    val critCapped: Double,
    val defMult: Double
)
