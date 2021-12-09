package net.alloyggp.opom.nash

import net.alloyggp.opom.loadGen1Results
import java.io.File

fun main() {
    val mrs = loadGen1Results()
    val strat = Strategy.loadFromFile(File("time-series/1000000.strat.txt"), mrs.contestants)

    val nonDefaultIndices = strat.getNonDefaultIndices().toMutableList()
    nonDefaultIndices.sortByDescending { strat.getChoiceCount(it) }
    val choiceCountSum = strat.getChoicesSum().toDouble()

    for (outerIndex in nonDefaultIndices) {
        val outerName = mrs.contestants[outerIndex]
        val percentage = strat.getChoiceCount(outerIndex) / choiceCountSum
        println("Strat: $outerName ($percentage%)")
        println("Matchups best to worst:")

        val innerIndices = nonDefaultIndices.sortedByDescending { mrs.getMatchupResultByIndices(outerIndex, it).getLeftWinningRate() }
        for (innerIndex in innerIndices) {
            val innerName = mrs.contestants[innerIndex]
            val winPercentage = mrs.getMatchupResultByIndices(outerIndex, innerIndex).getLeftWinningRate() * 100.0
            println("        * $winPercentage% vs. $innerName")
        }
        println()
    }
}
