package com.example.msmhelper.data

import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val name: String,
    val weapon: Int = 0,
    val armor: Int = 0
)

@Serializable
data class SfOutcomeStats(
    val up: Int = 0,
    val maintain: Int = 0,
    val derank: Int = 0,
    val breakCount: Int = 0,
    val total: Int = 0
) {
    fun increment(outcome: String): SfOutcomeStats {
        return when (outcome) {
            "up" -> copy(up = up + 1, total = total + 1)
            "maintain" -> copy(maintain = maintain + 1, total = total + 1)
            "derank" -> copy(derank = derank + 1, total = total + 1)
            "break" -> copy(breakCount = breakCount + 1, total = total + 1)
            else -> this
        }
    }

    fun decrement(outcome: String): SfOutcomeStats {
        return when (outcome) {
            "up" -> copy(up = maxOf(0, up - 1), total = maxOf(0, total - 1))
            "maintain" -> copy(maintain = maxOf(0, maintain - 1), total = maxOf(0, total - 1))
            "derank" -> copy(derank = maxOf(0, derank - 1), total = maxOf(0, total - 1))
            "break" -> copy(breakCount = maxOf(0, breakCount - 1), total = maxOf(0, total - 1))
            else -> this
        }
    }
}

@Serializable
data class SfLevelStats(
    val normal: SfOutcomeStats = SfOutcomeStats(),
    val catchStats: SfOutcomeStats = SfOutcomeStats()
) {
    fun record(outcome: String, isCatch: Boolean): SfLevelStats {
        return if (isCatch) {
            copy(catchStats = catchStats.increment(outcome))
        } else {
            copy(normal = normal.increment(outcome))
        }
    }

    fun undo(outcome: String, isCatch: Boolean): SfLevelStats {
        return if (isCatch) {
            copy(catchStats = catchStats.decrement(outcome))
        } else {
            copy(normal = normal.decrement(outcome))
        }
    }
}

@Serializable
data class SfHistoryItem(
    val time: String,
    val fromSf: Int,
    val outcome: String, // "up", "maintain", "derank", "break"
    val isCatch: Boolean
)

@Serializable
data class MSMAppState(
    val characters: List<Character> = emptyList(),
    val activeCharIndex: Int = 0,
    val currentSf: Int = 10,
    val sfStats: Map<Int, SfLevelStats> = emptyMap(),
    val sfHistory: List<SfHistoryItem> = emptyList()
)
