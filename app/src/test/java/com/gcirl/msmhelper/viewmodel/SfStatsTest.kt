package com.gcirl.msmhelper.viewmodel

import com.gcirl.msmhelper.data.Character
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class SfStatsTest {

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
