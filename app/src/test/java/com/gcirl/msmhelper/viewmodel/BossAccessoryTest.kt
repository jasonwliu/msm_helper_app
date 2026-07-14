package com.gcirl.msmhelper.viewmodel

import com.gcirl.msmhelper.data.BossAccessoryAttempt
import org.junit.Assert.*
import org.junit.Test

class BossAccessoryTest {

    private fun calculateDropRate(attempts: List<BossAccessoryAttempt>): Double {
        val totalRuns = attempts.size
        val totalDrops = attempts.count { it.isSuccess }
        return if (totalRuns > 0) (totalDrops.toDouble() / totalRuns * 100) else 0.0
    }

    @Test
    fun calculateDropRate_emptyList_returnsZero() {
        val rate = calculateDropRate(emptyList())
        assertEquals(0.0, rate, 0.001)
    }

    @Test
    fun calculateDropRate_allDrops_returnsOneHundred() {
        val attempts = listOf(
            BossAccessoryAttempt("2026-07-14 12:00:00", true),
            BossAccessoryAttempt("2026-07-14 12:05:00", true)
        )
        val rate = calculateDropRate(attempts)
        assertEquals(100.0, rate, 0.001)
    }

    @Test
    fun calculateDropRate_mixedList_returnsCorrectRate() {
        val attempts = listOf(
            BossAccessoryAttempt("2026-07-14 12:00:00", true),
            BossAccessoryAttempt("2026-07-14 12:01:00", false),
            BossAccessoryAttempt("2026-07-14 12:02:00", false),
            BossAccessoryAttempt("2026-07-14 12:03:00", false)
        )
        // 1 drop out of 4 runs = 25.0%
        val rate = calculateDropRate(attempts)
        assertEquals(25.0, rate, 0.001)
    }
}
