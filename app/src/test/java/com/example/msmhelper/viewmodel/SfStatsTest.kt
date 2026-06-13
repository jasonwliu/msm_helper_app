package com.example.msmhelper.viewmodel

import com.example.msmhelper.data.Character
import com.example.msmhelper.data.SfLevelStats
import com.example.msmhelper.data.SfOutcomeStats
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class SfStatsTest {

    @Test
    fun outcomeStats_increment_correctlyTracksCounts() {
        var stats = SfOutcomeStats()
        assertEquals(0, stats.total)

        stats = stats.increment("up")
        assertEquals(1, stats.up)
        assertEquals(1, stats.total)

        stats = stats.increment("maintain")
        stats = stats.increment("derank")
        stats = stats.increment("break")

        assertEquals(1, stats.up)
        assertEquals(1, stats.maintain)
        assertEquals(1, stats.derank)
        assertEquals(1, stats.breakCount)
        assertEquals(4, stats.total)
    }

    @Test
    fun outcomeStats_decrement_clapsAtZero() {
        var stats = SfOutcomeStats()
        // Try to decrement from empty state
        stats = stats.decrement("up")
        assertEquals(0, stats.up)
        assertEquals(0, stats.total)

        // Increment then decrement
        stats = stats.increment("derank")
        stats = stats.increment("derank")
        assertEquals(2, stats.derank)
        assertEquals(2, stats.total)

        stats = stats.decrement("derank")
        assertEquals(1, stats.derank)
        assertEquals(1, stats.total)

        stats = stats.decrement("derank")
        assertEquals(0, stats.derank)
        assertEquals(0, stats.total)

        stats = stats.decrement("derank")
        assertEquals(0, stats.derank)
        assertEquals(0, stats.total)
    }

    @Test
    fun levelStats_recordAndUndo_tracksNormalAndCatchSeparately() {
        var level = SfLevelStats()

        // Record normal successes
        level = level.record("up", isCatch = false)
        level = level.record("maintain", isCatch = false)

        // Record star catch successes
        level = level.record("up", isCatch = true)
        level = level.record("derank", isCatch = true)

        // Verify normal stats
        assertEquals(1, level.normal.up)
        assertEquals(1, level.normal.maintain)
        assertEquals(0, level.normal.derank)
        assertEquals(2, level.normal.total)

        // Verify catch stats
        assertEquals(1, level.catchStats.up)
        assertEquals(0, level.catchStats.maintain)
        assertEquals(1, level.catchStats.derank)
        assertEquals(2, level.catchStats.total)

        // Undo a normal maintain
        level = level.undo("maintain", isCatch = false)
        assertEquals(0, level.normal.maintain)
        assertEquals(1, level.normal.total)

        // Undo a catch derank
        level = level.undo("derank", isCatch = true)
        assertEquals(0, level.catchStats.derank)
        assertEquals(1, level.catchStats.total)
    }

    @Test
    fun legacyWebFormat_isDeserializedCorrectly() {
        val legacyJson = """
            [
              {
                "name": "garbodb",
                "weapon": 50,
                "armor": 19
              },
              {
                "name": "garbophantom",
                "weapon": 0,
                "armor": 13
              }
            ]
        """.trimIndent()
        val list = Json.decodeFromString<List<Character>>(legacyJson)
        assertEquals(2, list.size)
        assertEquals("garbodb", list[0].name)
        assertEquals(50, list[0].weapon)
        assertEquals(19, list[0].armor)
        assertEquals("garbophantom", list[1].name)
        assertEquals(0, list[1].weapon)
        assertEquals(13, list[1].armor)
    }
}
