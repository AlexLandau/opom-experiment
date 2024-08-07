package net.alloyggp.opom.nash

import net.alloyggp.opom.MatchupResultStore
import net.alloyggp.opom.loadGen1Results
import java.io.File
import java.util.*

// TODO: Graph of OPOMs changing prevalence over time
// TODO: Compare outputs of several runs (maybe add some non-determinism?)
// TODO: Output graph of which OPOMs beat which
// Note: This takes 500 seconds in its current state, but that's probably because I'm
// mixing in a ton of I/O that could be put on separate threads or structured differently
// (e.g. writing to a smaller number of files)
fun main() {
    val mrs = loadGen1Results()
    val movesets = mrs.contestants
    println("Calculating...")

    val bannedList = BitSet(movesets.size)
//    bannedList.set(movesets.indexOf("mewtwo_icebeam"))
//    bannedList.set(movesets.indexOf("mewtwo_bide"))

//    val filename = File("cstrat4.txt")
    val startTime = System.nanoTime()
    val strat = runEquilibriumSearch(mrs, null, bannedList, 1_000_000)
    println("Time to run: ${(System.nanoTime() - startTime) / 1_000_000_000} s")
    strat.print()
}

fun runEquilibriumSearch(mrs: MatchupResultStore<String>, startingStrategy: Strategy?, bannedList: BitSet, numIterations: Int): Strategy {
    if (numIterations < 1) {
        error("numIterations: $numIterations")
    }
    val movesets = mrs.contestants
    val curStrategy = startingStrategy?.copy() ?: Strategy(movesets, 0)

    val scoresAgainstCurStrategy = initializeScoresAgainstStrategy(mrs, curStrategy)
    fun updateStrategyByN(chosenIndex: Int, chosenMove: String, n: Int) {
        curStrategy.incrementChoiceByN(chosenIndex, n)
        for ((scoreIndex, scoreMove) in movesets.withIndex()) {
            val improvement = mrs.getMatchupResult(scoreMove, chosenMove).getLeftWinningRate()
            scoresAgainstCurStrategy[scoreIndex] += improvement * n
        }
    }


    var prevStrategy = curStrategy.copy()

    var i = 0
    for (iteration in 1..numIterations) {
        val (chosen, score) = movesets.withIndex().filter {
            (moveIndex, moveName) -> !bannedList.get(moveIndex)
        }.map { (moveIndex, moveName) -> moveName to scoresAgainstCurStrategy[moveIndex] }.maxByOrNull { it.second }!!
//        println("Picked $chosen with score $score")

        val chosenIndex = movesets.indexOf(chosen)
        updateStrategyByN(chosenIndex, chosen, 1)

        i++
        if (i >= 100) {
            i = 0
//            curStrategy.print()
            curStrategy.saveToFile(File("time-series/${iteration}.strat.txt"))
            val effVsPrev = getScore(curStrategy, prevStrategy, mrs)
            File("time-series/${iteration}.effectivenessVsPrev.txt").writeText("$effVsPrev")
            val (chosen2, score2) = movesets.withIndex().filter {
                (moveIndex, moveName) -> !bannedList.get(moveIndex)
            }.map { (moveIndex, moveName) -> moveName to scoresAgainstCurStrategy[moveIndex] }.maxByOrNull { it.second }!!
            // TODO: Normalize this (score2) relative to
            val score2AtEquilibrium = curStrategy.getChoicesSum().toDouble() / 2.0
            val scoreOverEquilibrium = score2 - score2AtEquilibrium
            val normalizedScoreOverEquilibrium = (scoreOverEquilibrium / curStrategy.getChoicesSum())
            File("time-series/${iteration}.rawScoreOverEquilibrium.txt").writeText("$scoreOverEquilibrium")
            File("time-series/${iteration}.normalizedScoreOverEquilibrium.txt").writeText("$normalizedScoreOverEquilibrium")
            File("time-series/${iteration}.bestOverCurrentStrat.txt").writeText(chosen2)
//            println("Current best pure OPOM selection vs. this: $chosen2 with score $score2, vs $score2AtEquilibrium ($scoreOverEquilibrium)")
//            println ("          Normalized score over equilibrium: ($normalizedScoreOverEquilibrium)")
            // TODO: Battle prev vs. cur
//            println("Latest relativized score: ${score / (curStrategy.getChoicesSum() - 1)}")
            println("Effectiveness against previous: ${effVsPrev}")
            prevStrategy = curStrategy.copy()
            // See if this helps...
            for ((moveIndex, move) in movesets.withIndex()) {
                if (curStrategy.getChoiceCount(moveIndex) > 0) {
                    updateStrategyByN(moveIndex, move, -1)
                }
            }
        }
    }
    return curStrategy
}

fun getScore(leftStrat: Strategy, rightStrat: Strategy, mrs: MatchupResultStore<String>): Double {
    val movesets = mrs.contestants
    var sum = 0.0
    for ((leftIndex, leftChoice) in movesets.withIndex()) {
        val leftCount = leftStrat.getChoiceCount(leftIndex)
        if (leftCount == 0) {
            continue
        }
        for ((rightIndex, rightChoice) in movesets.withIndex()) {
            val rightCount = rightStrat.getChoiceCount(rightIndex)
            if (rightCount == 0) {
                continue
            }
            val result = mrs.getMatchupResult(leftChoice, rightChoice).getLeftWinningRate()
            sum += result * leftCount * rightCount
        }
    }
    return sum / leftStrat.getChoicesSum() / rightStrat.getChoicesSum()
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
