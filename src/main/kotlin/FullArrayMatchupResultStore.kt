package net.alloyggp.opom

class FullArrayMatchupResultStore<T> (override val contestants: List<T>): MatchupResultStore<T>  {
    private val contestantsIndex: Map<T, Int> = contestants.withIndex().map { it.value to it.index }.toMap()
    private val contents: Array<Int>

    init {
        val numMatchups = contestants.size * (contestants.size - 1) / 2
        contents = Array(numMatchups * 3, { 0 })
    }

    override fun getContestantIndex(contestant: T): Int {
        return contestantsIndex.getValue(contestant)
    }

    override fun getMatchupResult(leftContestant: T, rightContestant: T): MatchupResult {
        val i1 = contestantsIndex[leftContestant]!!
        val i2 = contestantsIndex[rightContestant]!!
        return getMatchupResultByIndices(i1, i2)
    }
    override fun getMatchupResultByIndices(leftIndex: Int, rightIndex: Int): MatchupResult {
        val i1 = leftIndex
        val i2 = rightIndex
        if (i1 < i2) {
            val matchupIndex = getMatchupIndex(i1, i2)
            val leftWins = contents[matchupIndex * 3]
            val rightWins = contents[matchupIndex * 3 + 1]
            val draws = contents[matchupIndex * 3 + 2]
            return MatchupResult(leftWins, rightWins, draws)
        } else if (i1 == i2) {
            // TODO: Maybe adjust here
            return MatchupResult(5, 5, 0)
        } else {
            val matchupIndex = getMatchupIndex(i2, i1)
            val leftWins = contents[matchupIndex * 3 + 1]
            val rightWins = contents[matchupIndex * 3]
            val draws = contents[matchupIndex * 3 + 2]
            return MatchupResult(leftWins, rightWins, draws)
        }
    }

    fun storeMatchupResult(leftContestant: T, rightContestant: T, result: MatchupResult) {
        val i1 = contestantsIndex[leftContestant]!!
        val i2 = contestantsIndex[rightContestant]!!

        if (i1 < i2) {
            val matchupIndex = getMatchupIndex(i1, i2)
            contents[matchupIndex * 3] = result.leftWins
            contents[matchupIndex * 3 + 1] = result.rightWins
            contents[matchupIndex * 3 + 2] = result.draws
        } else if (i1 == i2) {
            error("Not allowed to store mirror matchups")
        } else {
            val matchupIndex = getMatchupIndex(i2, i1)
            contents[matchupIndex * 3 + 1] = result.leftWins
            contents[matchupIndex * 3] = result.rightWins
            contents[matchupIndex * 3 + 2] = result.draws
        }

        // TODO: Remove, this is for initial debugging
        val retrievedResult = getMatchupResult(leftContestant, rightContestant)
        if (retrievedResult != result) {
            error("Something went wrong!")
        }
    }

    // Exposed for testing
    // Note: Using capital variable names for clarity, since l and 1 look very similar
    internal fun getMatchupIndex(L: Int, R: Int): Int {
        if (L >= R) {
            error("Indices: $L $R")
        }
        /*
         * Let the number of contestants be N
         * We first have a list of N-1 entries (contestant 0 vs. contestants 1 to N-1)
         * Then a list of N-2, N-3, and so on
         * So for contestant L, we first want to skip all the contributions of 0 through L-1
         *
         * L=0: result 0
         * L=1: result N-1
         * L=2: result N-1 + N-2 = 2N-3
         * L=3: result N-1 + N-2 + N-3 = 3N-6
         *
         * General: Result L*N - (0 + 1 + 2 + 3 + ... L)
         *               = L*N - (L * L+1)/2
         *
         * After that, we add (R-L) - 1 to get the index within
         */
        val N = contestants.size
        return L*N - ((L * (L+1))/2) + (R - L - 1)
    }
}
