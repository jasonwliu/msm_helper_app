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

    fun calculateOptimalDistribution(
        characters: List<Character>,
        totalPool: Int,
        type: String
    ): CalculatorResult? {
        if (characters.isEmpty() || totalPool <= 0) return null

        // Extract pieces needed for each character
        val items = characters.map { c ->
            val cur = if (type == "weapon") c.weapon else c.armor
            val rem = cur % 150
            val need = if (rem == 0) 150 else 150 - rem
            c.name to need
        }

        // We want to find for each character an allocation choice of: 0, need, need + 150, need + 300, etc.
        // We use dynamic programming to maximize:
        // 1. Total stones created (primary)
        // 2. Total character pieces used (equivalent to minimizing pool pieces used, as a secondary tie-breaker)
        // while ensuring sum(allocation) <= totalPool.
        
        val dp = Array(totalPool + 1) { Pair(-1, -1) } // Pair(stonesCreated, piecesUsed)
        val parent = Array(characters.size) { IntArray(totalPool + 1) { -1 } } // Track chosen allocation for reconstruction

        dp[0] = Pair(0, 0)

        for (i in characters.indices) {
            val need = items[i].second
            val nextDp = dp.clone()
            
            // Initialize parent for reachable states as 0 (default choice)
            for (j in 0..totalPool) {
                if (dp[j].first != -1) {
                    parent[i][j] = 0
                }
            }

            for (j in 0..totalPool) {
                val prev = dp[j]
                if (prev.first == -1) continue // unreachable budget state
                
                // Option 0: Allocate 0 pieces to character i
                if (nextDp[j].first == -1 || isBetterState(prev.first, prev.second, nextDp[j].first, nextDp[j].second)) {
                    nextDp[j] = prev
                    parent[i][j] = 0
                }

                // Option 1+: Allocate need + 150 * k pieces
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
            // Copy nextDp back to dp for the next character iteration
            for (j in 0..totalPool) {
                dp[j] = nextDp[j]
            }
        }

        // Find best final state
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

        // Reconstruct allocations
        val distributions = mutableListOf<DistributionRow>()
        var currJ = bestJ
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

        // Reverse to match original character ordering
        distributions.reverse()

        return CalculatorResult(
            totalStonesCreated = bestStones,
            distributions = distributions
        )
    }

    private fun isBetterState(stonesA: Int, piecesA: Int, stonesB: Int, piecesB: Int): Boolean {
        if (stonesA > stonesB) return true
        if (stonesA < stonesB) return false
        return piecesA < piecesB
    }
}
