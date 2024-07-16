package net.alloyggp.opom

class MapMatchupResultStore<T> (override val contestants: List<T>): MatchupResultStore<T> {
    private val contestantsIndex: Map<T, Int> = contestants.withIndex().map { it.value to it.index }.toMap()
    private val contents: MutableMap<ULong, MatchupResult> = HashMap()

    val EMPTY_MATCHUP_RESULT = MatchupResult(0, 0, 0)

    override fun getContestantIndex(contestant: T): Int {
        return contestantsIndex.getValue(contestant)
    }

    override fun getMatchupResult(leftContestant: T, rightContestant: T): MatchupResult {
        return getMatchupResultByIndices(
                contestantsIndex.getValue(leftContestant),
                contestantsIndex.getValue(rightContestant))
    }

    override fun getMatchupResultByIndices(leftIndex: Int, rightIndex: Int): MatchupResult {
        if (leftIndex == rightIndex) {
            throw RuntimeException()
        }
        if (leftIndex < rightIndex) {
            val index = getIndex(leftIndex, rightIndex)
            return contents.get(index) ?: EMPTY_MATCHUP_RESULT
        } else {
            val index = getIndex(rightIndex, leftIndex)
            return contents.get(index)?.reverse() ?: EMPTY_MATCHUP_RESULT
        }
    }

    fun storeMatchupResult(leftContestant: T, rightContestant: T, result: MatchupResult) {
        val leftIndex = contestantsIndex.getValue(leftContestant)
        val rightIndex = contestantsIndex.getValue(rightContestant)
        if (leftIndex < rightIndex) {
            val index = getIndex(leftIndex, rightIndex)
            contents.put(index, result)
        } else if (leftIndex > rightIndex) {
            val index = getIndex(rightIndex, leftIndex)
            contents.put(index, result.reverse())
        } else {
            throw RuntimeException()
        }
    }

    private fun getIndex(leftIndex: Int, rightIndex: Int): ULong {
        if (leftIndex >= rightIndex) {
            throw RuntimeException()
        }
        return leftIndex.toULong() * (UInt.MAX_VALUE + 1UL) + rightIndex.toULong()
    }
}
