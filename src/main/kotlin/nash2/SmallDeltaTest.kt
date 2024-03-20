package net.alloyggp.opom.nash2

import net.alloyggp.opom.MatchupResultStore
import org.apache.commons.math3.stat.interval.ClopperPearsonInterval
import org.apache.commons.math3.stat.interval.WilsonScoreInterval

fun measureSensitivityToChanges(strategy: Strategy, inGroup: List<String>, mrs: MatchupResultStore<String>,
                                confidenceLevel: Double = 0.95): Map<Pair<String, String>, Double> {
    val matchupSensitivities = HashMap<Pair<String, String>, Double>()

    val inGroupIndices = inGroup.map { mrs.getContestantIndex(it) }
    for ((leftIndexInGroup, left) in inGroup.withIndex()) {
        val leftIndex = mrs.getContestantIndex(left)
        for (right in inGroup.drop(leftIndexInGroup + 1)) {
            val rightIndex = mrs.getContestantIndex(right)
            // This gives us one matchup...
            val matchupResult = mrs.getMatchupResultByIndices(leftIndex, rightIndex)
            // TODO: One thing we want is the confidence level of our actual win rate being in a certain interval
            val sampleSize = matchupResult.getSamples()
            // val winRate = matchupResult.getLeftWinningRate()
            val effectiveSuccesses = matchupResult.leftWins + (matchupResult.draws / 2)

//            println("For matchup $left vs $right ($sampleSize samples), confidence level $confidenceLevel:")

            // Given the sample size and the win rate... what is the equivalent of the standard deviation, or upper and
            // lower confidence bounds given the sample size for a given p?
            val wilsonInterval = WilsonScoreInterval().createInterval(sampleSize, effectiveSuccesses, confidenceLevel)
//            println("  $sampleSize $effectiveSuccesses $confidenceLevel")
//            val cpInterval = ClopperPearsonInterval().createInterval(sampleSize, effectiveSuccesses, confidenceLevel)
//            println("  Wilson interval bounds:          $wilsonInterval")
//            println("  Clopper-Pearson interval bounds: $cpInterval")

            // Another is: If we change the matchup result by some epsilon, how much does that change the result?
            val modifiedStrat1 = solveForNashEquilibriumAmong(inGroupIndices, mrs,
                    mapOf(Pair(leftIndex, rightIndex) to wilsonInterval.upperBound))
            val modifiedStrat2 = solveForNashEquilibriumAmong(inGroupIndices, mrs,
                    mapOf(Pair(leftIndex, rightIndex) to wilsonInterval.lowerBound))
            // Should we use the L1 or L2 distance?
            // For the sake of dumb conservatism, let's say L1 for now
            val sensitivity = l1Distance(strategy, modifiedStrat1) + l1Distance(strategy, modifiedStrat2)

//            println("  For matchup $left vs $right ($sampleSize samples), the sensitivity is $sensitivity")
            matchupSensitivities.put(Pair(left, right), sensitivity)
        }
    }
    return matchupSensitivities
}

fun l1Distance(left: Strategy, right: Strategy): Double {
    var sum = 0.0
    for (index in left.getNonDefaultIndices() + right.getNonDefaultIndices()) {
        sum += Math.abs(left.getChoiceNormalizedWeight(index) - right.getChoiceNormalizedWeight(index))
    }
    return sum
}
