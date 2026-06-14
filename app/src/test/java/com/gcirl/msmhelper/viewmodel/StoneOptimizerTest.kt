package com.gcirl.msmhelper.viewmodel

import com.gcirl.msmhelper.data.Character
import com.gcirl.msmhelper.data.StoneOptimizer
import org.junit.Assert.*
import org.junit.Test

class StoneOptimizerTest {

    @Test
    fun calculate_withEmptyCharacters_returnsNull() {
        val result = StoneOptimizer.calculateOptimalDistribution(
            characters = emptyList(),
            totalPool = 100,
            type = "weapon"
        )
        assertNull(result)
    }

    @Test
    fun calculate_withZeroPool_returnsNull() {
        val chars = listOf(Character("Test", 100, 100))
        val result = StoneOptimizer.calculateOptimalDistribution(
            characters = chars,
            totalPool = 0,
            type = "weapon"
        )
        assertNull(result)
    }

    @Test
    fun calculate_completesClosestStonesFirst() {
        val charA = Character("A", weapon = 130) // Needs 20 pieces to reach 150 (1 stone)
        val charB = Character("B", weapon = 50)  // Needs 100 pieces to reach 150 (1 stone)
        val characters = listOf(charA, charB)

        // Scenario 1: Pool size is 20
        // It should give all 20 pieces to A because A needs less to complete a stone.
        val result1 = StoneOptimizer.calculateOptimalDistribution(characters, 20, "weapon")
        assertNotNull(result1)
        assertEquals(1, result1!!.totalStonesCreated)
        
        val rowA1 = result1.distributions.find { it.charName == "A" }
        val rowB1 = result1.distributions.find { it.charName == "B" }
        
        assertNotNull(rowA1)
        assertEquals(20, rowA1!!.givenPieces)
        assertEquals(130, rowA1.previousPieces)
        assertEquals(150, rowA1.currentPieces)
        assertEquals(1, rowA1.stonesAdded)
        
        assertNull(rowB1) // B should not receive any pieces

        // Scenario 2: Pool size is 120
        // A should get 20, B should get 100.
        val result2 = StoneOptimizer.calculateOptimalDistribution(characters, 120, "weapon")
        assertNotNull(result2)
        assertEquals(2, result2!!.totalStonesCreated)
        
        val rowA2 = result2.distributions.find { it.charName == "A" }
        val rowB2 = result2.distributions.find { it.charName == "B" }
        
        assertNotNull(rowA2)
        assertEquals(20, rowA2!!.givenPieces)
        assertEquals(150, rowA2.currentPieces)
        
        assertNotNull(rowB2)
        assertEquals(100, rowB2!!.givenPieces)
        assertEquals(150, rowB2.currentPieces)
    }

    @Test
    fun calculate_doesNotAllocateLeftoversOverStone() {
        val charA = Character("A", weapon = 130) // Needs 20 (new total 150)
        val charB = Character("B", weapon = 10)  // Needs 140 (new total 150)
        val characters = listOf(charA, charB)

        // Pool is 170.
        // A gets 20 (reaches 150).
        // B gets 140 (reaches 150).
        // Leftover 10 should NOT be allocated because it would go over a stone.
        val result = StoneOptimizer.calculateOptimalDistribution(characters, 170, "weapon")
        assertNotNull(result)
        assertEquals(2, result!!.totalStonesCreated)
        
        val rowA = result.distributions.find { it.charName == "A" }
        val rowB = result.distributions.find { it.charName == "B" }
        
        assertNotNull(rowA)
        assertNotNull(rowB)
        
        assertEquals(20, rowA!!.givenPieces)
        assertEquals(140, rowB!!.givenPieces)
        
        // Total pieces allocated must equal 160 (10 leftover ignored)
        val totalAllocated = rowA.givenPieces + rowB.givenPieces
        assertEquals(160, totalAllocated)
    }

    @Test
    fun calculateJoint_withEmptyCharacters_returnsNull() {
        val result = StoneOptimizer.calculateJointOptimalDistribution(
            characters = emptyList(),
            weaponPool = 100,
            armorPool = 100,
            sharedPool = 100
        )
        assertNull(result)
    }

    @Test
    fun calculateJoint_withZeroPools_returnsNull() {
        val chars = listOf(Character("Test", 100, 100))
        val result = StoneOptimizer.calculateJointOptimalDistribution(
            characters = chars,
            weaponPool = 0,
            armorPool = 0,
            sharedPool = 0
        )
        assertNull(result)
    }

    @Test
    fun calculateJoint_distributesSharedPoolCorrectly() {
        val charA = Character("A", weapon = 130, armor = 140) // Weapon needs 20, Armor needs 10
        val characters = listOf(charA)

        // Weapon Pool = 10, Armor Pool = 0, Shared Pool = 20
        // We can get both stones:
        // - Weapon: uses 10 from weapon pool, 10 from shared
        // - Armor: uses 0 from armor pool, 10 from shared
        // Total shared used = 20 <= 20 (valid)
        val result = StoneOptimizer.calculateJointOptimalDistribution(
            characters = characters,
            weaponPool = 10,
            armorPool = 0,
            sharedPool = 20
        )
        assertNotNull(result)
        assertEquals(2, result!!.totalStonesCreated)
        assertEquals(1, result.weaponStonesCreated)
        assertEquals(1, result.armorStonesCreated)
        assertEquals(10, result.sharedUsedForWeapon)
        assertEquals(10, result.sharedUsedForArmor)

        val weaponRow = result.weaponDistributions.find { it.charName == "A" }
        val armorRow = result.armorDistributions.find { it.charName == "A" }
        assertNotNull(weaponRow)
        assertNotNull(armorRow)
        assertEquals(20, weaponRow!!.givenPieces)
        assertEquals(10, armorRow!!.givenPieces)
    }

    @Test
    fun calculateJoint_minimizesLeftoversAsTieBreaker() {
        val charA = Character("A", weapon = 130, armor = 0) // Weapon needs 20
        val charB = Character("B", weapon = 140, armor = 0) // Weapon needs 10
        val characters = listOf(charA, charB)

        // Weapon Pool = 20, Armor Pool = 0, Shared Pool = 0
        // We can make at most 1 stone.
        // Option 1: Give 20 to A (uses 20 pieces)
        // Option 2: Give 10 to B (uses 10 pieces)
        // It should choose Option 2 to minimize pieces used.
        val result = StoneOptimizer.calculateJointOptimalDistribution(
            characters = characters,
            weaponPool = 20,
            armorPool = 0,
            sharedPool = 0
        )
        assertNotNull(result)
        assertEquals(1, result!!.totalStonesCreated)
        assertEquals(1, result.weaponStonesCreated)
        assertEquals(0, result.armorStonesCreated)

        val rowA = result.weaponDistributions.find { it.charName == "A" }
        val rowB = result.weaponDistributions.find { it.charName == "B" }
        assertNull(rowA)
        assertNotNull(rowB)
        assertEquals(10, rowB!!.givenPieces)
    }

    @Test
    fun calculateJoint_noSharedPool_behavesIndependently() {
        val charA = Character("A", weapon = 130, armor = 140) // Weapon needs 20, Armor needs 10
        val characters = listOf(charA)

        // Weapon Pool = 20, Armor Pool = 5, Shared Pool = 0
        // Weapon should get 20 (1 stone). Armor gets 0 (no stone possible with only 5 pieces).
        val result = StoneOptimizer.calculateJointOptimalDistribution(
            characters = characters,
            weaponPool = 20,
            armorPool = 5,
            sharedPool = 0
        )
        assertNotNull(result)
        assertEquals(1, result!!.totalStonesCreated)
        assertEquals(1, result.weaponStonesCreated)
        assertEquals(0, result.armorStonesCreated)
        assertEquals(0, result.sharedUsedForWeapon)
        assertEquals(0, result.sharedUsedForArmor)

        val weaponRow = result.weaponDistributions.find { it.charName == "A" }
        val armorRow = result.armorDistributions.find { it.charName == "A" }
        assertNotNull(weaponRow)
        assertNull(armorRow)
        assertEquals(20, weaponRow!!.givenPieces)
    }
}
