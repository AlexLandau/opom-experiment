package net.alloyggp.opom.nash

import net.alloyggp.opom.MatchupResultStore
import net.alloyggp.opom.loadGen1Results
import java.io.File

fun main() {
    val mrs = loadGen1Results()
    val movesets = mrs.contestants
    println("Calculating...")

    val filename = File("cstrat1.txt")
    val curStrategy = Strategy.loadFromFileOr(filename, movesets) {
        val strat = Strategy(movesets, 0)
        strat.incrementChoice(movesets.indexOf("mewtwo_psychic"))
        strat
    }

    val scoresAgainstCurStrategy = initializeScoresAgainstStrategy(mrs, curStrategy)

    var i = 0
    while (true) {
        val (chosen, score) = movesets.withIndex().map { (moveIndex, moveName) -> moveName to scoresAgainstCurStrategy[moveIndex] }.maxBy { it.second }!!
        println("Picked $chosen with score $score")

        val chosenIndex = movesets.indexOf(chosen)
        // Update the _ and the scores against the current strategy
        curStrategy.incrementChoice(chosenIndex)
        for ((scoreIndex, scoreMove) in movesets.withIndex()) {
            val improvement = mrs.getMatchupResult(scoreMove, chosen).getLeftWinningRate()
            scoresAgainstCurStrategy[scoreIndex] += improvement
        }

        i++
        if (i >= 50) {
            i = 0
            curStrategy.print()
        }
    }
}

fun initializeScoresAgainstStrategy(mrs: MatchupResultStore<String>, strat: Strategy): Array<Double> {
    val movesets = mrs.contestants
    val scores = Array(movesets.size, { 0.0 })
    for ((scoreIndex, scoreMove) in movesets.withIndex()) {
        var sum = 0.0
        for ((stratIndex, stratMove) in movesets.withIndex()) {
            val count = strat.getChoiceCount(stratIndex)
            if (count > 0) {
                sum += count * mrs.getMatchupResult(scoreMove, stratMove).getLeftWinningRate()
            }
        }
        scores[scoreIndex] = sum
    }
    return scores
}
