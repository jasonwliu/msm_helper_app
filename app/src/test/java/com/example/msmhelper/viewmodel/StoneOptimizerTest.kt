package com.example.msmhelper.viewmodel

import com.example.msmhelper.data.Character
import com.example.msmhelper.data.StoneOptimizer
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
}
