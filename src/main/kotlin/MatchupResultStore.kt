package net.alloyggp.opom

interface MatchupResultStore<T> {
    val contestants: List<T>

    fun getContestantIndex(contestant: T): Int

    fun getMatchupResult(leftContestant: T, rightContestant: T): MatchupResult
    fun getMatchupResultByIndices(leftIndex: Int, rightIndex: Int): MatchupResult
}

// TODO: Make it so we use less memory when there isn't a draw
data class MatchupResult(val leftWins: Int, val rightWins: Int, val draws: Int) {
    fun getLeftWinningRate(): Double {
        val total = (leftWins + rightWins + draws).toDouble()
        return (leftWins + (draws / 2.0)) / total
    }

    fun getSamples(): Int {
        return leftWins + rightWins + draws
    }

    fun reverse(): MatchupResult {
        return MatchupResult(rightWins, leftWins, draws)
    }
}
