package com.gcirl.msmhelper.data

object StoneOptimizer {

    data class DistributionRow(
        val charName: String,
        val givenPieces: Int,
        val previousPieces: Int,
        val currentPieces: Int,
        val stonesAdded: Int
    )

    data class CalculatorResult(
        val totalStonesCreated: Int,
        val distributions: List<DistributionRow>
    )

    data class JointCalculatorResult(
        val totalStonesCreated: Int,
        val weaponStonesCreated: Int,
        val armorStonesCreated: Int,
        val sharedUsedForWeapon: Int,
        val sharedUsedForArmor: Int,
        val weaponDistributions: List<DistributionRow>,
        val armorDistributions: List<DistributionRow>
    )

    fun calculateOptimalDistribution(
        characters: List<Character>,
        totalPool: Int,
        type: String
    ): CalculatorResult? {
        val result = solve1D(characters, totalPool, type) ?: return null
        val dp = result.first
        val parent = result.second

        var bestJ = 0
        var bestStones = -1
        var bestPieces = -1
        for (j in 0..totalPool) {
            val state = dp[j]
            if (state.first != -1 && isBetterState(state.first, state.second, bestStones, bestPieces)) {
                bestStones = state.first
                bestPieces = state.second
                bestJ = j
            }
        }

        if (bestStones <= 0) return null

        val distributions = reconstructDistribution(characters, parent, bestJ, type)
        return CalculatorResult(
            totalStonesCreated = bestStones,
            distributions = distributions
        )
    }

    fun calculateJointOptimalDistribution(
        characters: List<Character>,
        weaponPool: Int,
        armorPool: Int,
        sharedPool: Int
    ): JointCalculatorResult? {
        if (characters.isEmpty()) return null
        if (weaponPool <= 0 && armorPool <= 0 && sharedPool <= 0) return null

        // 1. Solve Weapon 1D DP up to weaponPool + sharedPool
        val maxW = weaponPool + sharedPool
        val wResult = solve1D(characters, maxW, "weapon") ?: return null
        val dpW = wResult.first
        val parentW = wResult.second

        // 2. Solve Armor 1D DP up to armorPool + sharedPool
        val maxA = armorPool + sharedPool
        val aResult = solve1D(characters, maxA, "armor") ?: return null
        val dpA = aResult.first
        val parentA = aResult.second

        // 3. Find the best combination of (w, a)
        var bestW = 0
        var bestA = 0
        var bestStones = -1
        var bestPieces = -1
        var bestSharedW = 0
        var bestSharedA = 0

        for (w in 0..maxW) {
            val stateW = dpW[w]
            if (stateW.first == -1) continue
            val wUsed = stateW.second
            val sUsedW = maxOf(0, wUsed - weaponPool)

            for (a in 0..maxA) {
                val stateA = dpA[a]
                if (stateA.first == -1) continue
                val aUsed = stateA.second
                val sUsedA = maxOf(0, aUsed - armorPool)

                if (sUsedW + sUsedA <= sharedPool) {
                    val stones = stateW.first + stateA.first
                    val pieces = wUsed + aUsed

                    // Check if this state is better than our best (maximize stones primarily, minimize pieces used secondarily)
                    if (stones > bestStones || (stones == bestStones && pieces < bestPieces)) {
                        bestStones = stones
                        bestPieces = pieces
                        bestW = w
                        bestA = a
                        bestSharedW = sUsedW
                        bestSharedA = sUsedA
                    }
                }
            }
        }

        if (bestStones <= 0) return null

        // 4. Reconstruct distributions for Weapon and Armor
        val weaponDist = reconstructDistribution(characters, parentW, bestW, "weapon")
        val armorDist = reconstructDistribution(characters, parentA, bestA, "armor")

        return JointCalculatorResult(
            totalStonesCreated = bestStones,
            weaponStonesCreated = dpW[bestW].first,
            armorStonesCreated = dpA[bestA].first,
            sharedUsedForWeapon = bestSharedW,
            sharedUsedForArmor = bestSharedA,
            weaponDistributions = weaponDist,
            armorDistributions = armorDist
        )
    }

    private fun solve1D(
        characters: List<Character>,
        totalPool: Int,
        type: String
    ): Pair<Array<Pair<Int, Int>>, Array<IntArray>>? {
        if (characters.isEmpty() || totalPool < 0) return null

        val items = characters.map { c ->
            val cur = if (type == "weapon") c.weapon else c.armor
            val rem = cur % 150
            val need = if (rem == 0) 150 else 150 - rem
            c.name to need
        }

        val dp = Array(totalPool + 1) { Pair(-1, -1) } // Pair(stonesCreated, piecesUsed)
        val parent = Array(characters.size) { IntArray(totalPool + 1) { -1 } }

        dp[0] = Pair(0, 0)

        for (i in characters.indices) {
            val need = items[i].second
            val nextDp = dp.clone()

            for (j in 0..totalPool) {
                if (dp[j].first != -1) {
                    parent[i][j] = 0
                }
            }

            for (j in 0..totalPool) {
                val prev = dp[j]
                if (prev.first == -1) continue

                if (nextDp[j].first == -1 || isBetterState(prev.first, prev.second, nextDp[j].first, nextDp[j].second)) {
                    nextDp[j] = prev
                    parent[i][j] = 0
                }

                var k = 0
                while (true) {
                    val cost = need + 150 * k
                    if (j + cost > totalPool) break

                    val nextStones = prev.first + 1 + k
                    val nextPieces = prev.second + cost
                    val targetIndex = j + cost

                    if (nextDp[targetIndex].first == -1 || isBetterState(nextStones, nextPieces, nextDp[targetIndex].first, nextDp[targetIndex].second)) {
                        nextDp[targetIndex] = Pair(nextStones, nextPieces)
                        parent[i][targetIndex] = cost
                    }
                    k++
                }
            }
            for (j in 0..totalPool) {
                dp[j] = nextDp[j]
            }
        }
        return Pair(dp, parent)
    }

    private fun reconstructDistribution(
        characters: List<Character>,
        parent: Array<IntArray>,
        targetBudget: Int,
        type: String
    ): List<DistributionRow> {
        val distributions = mutableListOf<DistributionRow>()
        var currJ = targetJ(parent, targetBudget)
        for (i in characters.indices.reversed()) {
            val allocated = parent[i][currJ]
            if (allocated > 0) {
                val char = characters[i]
                val prevVal = if (type == "weapon") char.weapon else char.armor
                val prevStones = prevVal / 150
                val curVal = prevVal + allocated
                val curStones = curVal / 150
                distributions.add(
                    DistributionRow(
                        charName = char.name,
                        givenPieces = allocated,
                        previousPieces = prevVal,
                        currentPieces = curVal,
                        stonesAdded = curStones - prevStones
                    )
                )
                currJ -= allocated
            }
        }
        distributions.reverse()
        return distributions
    }

    private fun targetJ(parent: Array<IntArray>, budget: Int): Int {
        return budget
    }

    private fun isBetterState(stonesA: Int, piecesA: Int, stonesB: Int, piecesB: Int): Boolean {
        if (stonesA > stonesB) return true
        if (stonesA < stonesB) return false
        return piecesA < piecesB
    }
}
