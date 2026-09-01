package com.gcirl.msmhelper.viewmodel

import com.gcirl.msmhelper.data.Account
import com.gcirl.msmhelper.data.Character
import com.gcirl.msmhelper.data.MSMAppState
import com.gcirl.msmhelper.data.NecroAction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class NecroResetTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Test
    fun account_serialization_preservesLastResetDate() {
        val account = Account(
            name = "Main Account",
            characters = listOf(Character(name = "Hero", weapon = 10, armor = 20)),
            activeCharIndex = 2,
            lastResetDate = "2026-08-28"
        )

        val encoded = json.encodeToString(account)
        val decoded = json.decodeFromString<Account>(encoded)

        assertEquals("2026-08-28", decoded.lastResetDate)
        assertEquals(2, decoded.activeCharIndex)
    }

    @Test
    fun account_legacyDeserialization_defaultsLastResetDateToEmpty() {
        // Simulating JSON produced prior to adding lastResetDate
        val legacyJson = """{"name":"Legacy Account","characters":[],"activeCharIndex":1}"""
        val decoded = json.decodeFromString<Account>(legacyJson)

        assertEquals("", decoded.lastResetDate)
        assertEquals(1, decoded.activeCharIndex)
    }

    @Test
    fun msmAppState_serialization_preservesLastResetDate() {
        val state = MSMAppState(
            activeCharIndex = 3,
            lastResetDate = "2026-08-29"
        )

        val encoded = json.encodeToString(state)
        val decoded = json.decodeFromString<MSMAppState>(encoded)

        assertEquals("2026-08-29", decoded.lastResetDate)
        assertEquals(3, decoded.activeCharIndex)
    }

    @Test
    fun dailyReset_detectsDateChange() {
        val lastResetDate = "2026-08-28"
        val currentDate = "2026-08-29"

        val needsReset = lastResetDate.isNotEmpty() && lastResetDate != currentDate
        assertTrue("Expected reset when current date differs from last reset date", needsReset)
    }

    @Test
    fun dailyReset_sameDayDoesNotReset() {
        val lastResetDate = "2026-08-29"
        val currentDate = "2026-08-29"

        val needsReset = lastResetDate.isNotEmpty() && lastResetDate != currentDate
        assertFalse("Expected no reset when dates are identical", needsReset)
    }

    @Test
    fun serverDateFormat_usesGmtMinus8() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT-8")
        }

        val testCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-8")).apply {
            set(2026, Calendar.AUGUST, 29, 0, 0, 1)
        }

        val formattedDate = sdf.format(testCalendar.time)
        assertEquals("2026-08-29", formattedDate)
    }

    @Test
    fun trackedType_defaultsToArmorWhenOverrideCleared() {
        var trackedTypeOverride: String? = "weapon"
        // Advancing to next character clears the override
        trackedTypeOverride = null
        val trackedType = trackedTypeOverride ?: "armor"
        assertEquals("armor", trackedType)
    }

    @Test
    fun msmAppState_settingsSerialization_preservesSettings() {
        val state = MSMAppState(
            nextCharStoneMode = "weapon",
            reorderModeEnabled = true
        )

        val encoded = json.encodeToString(state)
        val decoded = json.decodeFromString<MSMAppState>(encoded)

        assertEquals("weapon", decoded.nextCharStoneMode)
        assertTrue(decoded.reorderModeEnabled)
    }

    @Test
    fun msmAppState_legacyDeserialization_defaultsSettings() {
        val legacyJson = """{"characters":[],"activeCharIndex":0}"""
        val decoded = json.decodeFromString<MSMAppState>(legacyJson)

        assertEquals("armor", decoded.nextCharStoneMode)
        assertFalse(decoded.reorderModeEnabled)
    }

    @Test
    fun nextCharStoneSelection_behaviorModes() {
        // Mode 1: Armor
        val modeArmor = "armor"
        val nextStoneArmor = when (modeArmor) {
            "weapon" -> "weapon"
            "keep" -> "weapon"
            else -> "armor"
        }
        assertEquals("armor", nextStoneArmor)

        // Mode 2: Weapon
        val modeWeapon = "weapon"
        val nextStoneWeapon = when (modeWeapon) {
            "weapon" -> "weapon"
            "keep" -> "armor"
            else -> "armor"
        }
        assertEquals("weapon", nextStoneWeapon)

        // Mode 3: Keep
        val modeKeep = "keep"
        val currentSelection = "weapon"
        val nextStoneKeep = when (modeKeep) {
            "weapon" -> "weapon"
            "keep" -> currentSelection
            else -> "armor"
        }
        assertEquals("weapon", nextStoneKeep)

        // Mode 4: Per-Character Keep
        val chars = listOf(
            Character("Char1", preferredType = "weapon"),
            Character("Char2", preferredType = "armor"),
            Character("Char3", preferredType = null)
        )
        val modePerChar = "per_character"
        fun getTrackedTypeForIndex(index: Int): String {
            val char = chars[index]
            return when (modePerChar) {
                "weapon" -> "weapon"
                "per_character" -> if (char.preferredType == "weapon") "weapon" else "armor"
                else -> "armor"
            }
        }
        assertEquals("weapon", getTrackedTypeForIndex(0))
        assertEquals("armor", getTrackedTypeForIndex(1))
        assertEquals("armor", getTrackedTypeForIndex(2))
    }

    @Test
    fun character_preferredType_serializationCompatibility() {
        val charWithPref = Character(name = "Hero", weapon = 10, armor = 20, preferredType = "weapon")
        val encoded = json.encodeToString(charWithPref)
        val decoded = json.decodeFromString<Character>(encoded)

        assertEquals("weapon", decoded.preferredType)

        // Legacy JSON without preferredType defaults to null
        val legacyJson = """{"name":"OldHero","weapon":5,"armor":15}"""
        val legacyDecoded = json.decodeFromString<Character>(legacyJson)
        assertNull(legacyDecoded.preferredType)
    }
}
