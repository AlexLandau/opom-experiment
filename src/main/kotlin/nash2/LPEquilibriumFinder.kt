package net.alloyggp.opom.nash2

import net.alloyggp.opom.MatchupResultStore
import net.alloyggp.opom.loadGen1Results
import org.apache.commons.math3.optim.linear.*
import java.io.File
import java.util.*

fun main() {
    println("Loading results data...")
    val startTime1 = System.currentTimeMillis()
    val mrs = loadGen1Results()
    println("Loaded data in ${System.currentTimeMillis() - startTime1} ms")
    val movesets = mrs.contestants
    println("Calculating...")
    val startTime2 = System.currentTimeMillis()

    val strategy = findBestStrategy(mrs, setOf())
    println("Ran computations in ${System.currentTimeMillis() - startTime2} ms")
    strategy.print()
    strategy.saveToFile(File("output" + System.currentTimeMillis()), mrs)

//    val curMixedStrategyIndices = TreeSet<Int>()
//    curMixedStrategyIndices.add(0)
//
//    while (true) {
//        val strategy = solveForNashEquilibriumAmong(curMixedStrategyIndices.toList(), mrs)
//        strategy.print()
//
////        for (index in ArrayList(curMixedStrategyIndices)) {
////            if (strategy.getChoiceNormalizedWeight(index) == 0.0) {
////                println("Removing ${movesets[index]} from the strategy")
////                curMixedStrategyIndices.remove(index)
////            }
////        }
//
//        var bestPureStratIndex = -1
//        var bestPureStratEffectiveness = -1.0
//        for (pureStratIndex in movesets.indices) {
//            var effectivenessAgainstCurStrat = 0.0
//            for (mixedStratIndex in curMixedStrategyIndices) {
//                effectivenessAgainstCurStrat += strategy.getChoiceNormalizedWeight(mixedStratIndex) *
//                        mrs.getMatchupResultByIndices(pureStratIndex, mixedStratIndex).getLeftWinningRate()
//            }
//            if (effectivenessAgainstCurStrat > bestPureStratEffectiveness) {
//                bestPureStratIndex = pureStratIndex
//                bestPureStratEffectiveness = effectivenessAgainstCurStrat
//            }
//        }
//
//        if (curMixedStrategyIndices.contains(bestPureStratIndex)) {
//            println("Looks like we're done here")
//            println("Best pure-strat effectiveness is ${movesets[bestPureStratIndex]} with effectiveness $bestPureStratEffectiveness")
//            println("Ran computations in ${System.currentTimeMillis() - startTime2} ms")
//            return
//        }
//        curMixedStrategyIndices.add(bestPureStratIndex)
//        println("Adding strategy ${movesets[bestPureStratIndex]}, which has ${bestPureStratEffectiveness} effectiveness vs. the mixed strat")
//    }
}

// Iteratively add to the subset used for NE solving
fun findBestStrategy(mrs: MatchupResultStore<String>, banList: Set<Int>): Strategy {
    val movesets = mrs.contestants

    val curMixedStrategyIndices = TreeSet<Int>()
    curMixedStrategyIndices.add(movesets.indices.filterNot { banList.contains(it) }.first())

    while (true) {
        val strategy = solveForNashEquilibriumAmong(curMixedStrategyIndices.toList(), mrs)

        var bestPureStratIndex = -1
        var bestPureStratEffectiveness = -1.0
        for (pureStratIndex in movesets.indices) {
            if (banList.contains(pureStratIndex)) {
                continue
            }
            var effectivenessAgainstCurStrat = 0.0
            for (mixedStratIndex in curMixedStrategyIndices) {
                effectivenessAgainstCurStrat += strategy.getChoiceNormalizedWeight(mixedStratIndex) *
                        mrs.getMatchupResultByIndices(pureStratIndex, mixedStratIndex).getLeftWinningRate()
            }
            if (effectivenessAgainstCurStrat > bestPureStratEffectiveness) {
                bestPureStratIndex = pureStratIndex
                bestPureStratEffectiveness = effectivenessAgainstCurStrat
            }
        }

        if (curMixedStrategyIndices.contains(bestPureStratIndex)) {
            return strategy
        }
        curMixedStrategyIndices.add(bestPureStratIndex)
    }
}

fun sillyMain() {
    val mrs = loadGen1Results()
    val movesets = mrs.contestants
    println("Calculating...")
    val strategy = solveForNashEquilibriumAmong(movesets.indices.toList(), mrs)
    strategy.print()
}

fun oldMain() {
    val mrs = loadGen1Results()
    val movesets = mrs.contestants
    println("Calculating...")

    // Take a reasonable-looking subset and throw in one extra...
    val mixedStrategyIndices = listOf(
        0, // this is extra -- ekans_rage, I believe
        178,
        326,
        359,
        479,
        552,
        620,
        638,
        1181,
        1252,
        1597,
        2143,
        2377,
        2798,
        2924,
        2962,
        3047,
        3136,
        3422,
        3599,
        3678,
        3802,
        3835,
        3870,
    )

    // Now see what the NE is if we only use these strategies
    val strategy = solveForNashEquilibriumAmong(mixedStrategyIndices, mrs)
    println("Chosen strategy:")
    strategy.print()
}

fun solveForNashEquilibriumAmong(
        mixedStrategyIndices: List<Int>,
        mrs: MatchupResultStore<String>,
        winRateOverrides: Map<Pair<Int, Int>, Double> = mapOf()): Strategy {
    val solver = SimplexSolver()
    /*

objective function: LinearObjectiveFunction - mandatory
linear constraints LinearConstraintSet - mandatory
type of optimization: GoalType - optional, default: MINIMIZE
whether to allow negative values as solution: NonNegativeConstraint - optional, default: true
pivot selection rule: PivotSelectionRule - optional, default PivotSelectionRule.DANTZIG
callback for the best solution: SolutionCallback - optional
maximum number of iterations: MaxIter - optional, default: Integer.MAX_VALUE

     */
    // So, we want one variable per each pure strategy, then one representing opponent's payoff (which
    // will generalize better for future stuff than assuming 0.5)

    // Variables: (probabilities of) Rock, Paper, Scissors; Epsilon
    val numStrats = mixedStrategyIndices.size

    val objectiveFunction = LinearObjectiveFunction(Collections.nCopies(numStrats, 0.0).plus(1.0).toDoubleArray(), 0.0)
    val probabilitiesSumUp = LinearConstraint(Collections.nCopies(numStrats, 1.0).plus(0.0).toDoubleArray(), Relationship.EQ, 1.0)
//    val probabilitiesSumUp = LinearConstraint(doubleArrayOf(1.0, 1.0, 1.0, 0.0), Relationship.EQ, 1.0)
//    val rockNonNegative = LinearConstraint(doubleArrayOf(1.0, 0.0, 0.0, 0.0), Relationship.GEQ, 0.0)
//    val paperNonNegative = LinearConstraint(doubleArrayOf(0.0, 1.0, 0.0, 0.0), Relationship.GEQ, 0.0)
//    val scissorsNonNegative = LinearConstraint(doubleArrayOf(0.0, 0.0, 1.0, 0.0), Relationship.GEQ, 0.0)
//    val epsilonNonNegative = LinearConstraint(doubleArrayOf(0.0, 0.0, 0.0, 1.0), Relationship.GEQ, 0.0)
    val constraints = ArrayList<LinearConstraint>()
    constraints.add(probabilitiesSumUp)
    for (outerIndex in mixedStrategyIndices) {
        // The value we're computing represents how well the outerIndex strategy does against the mixed strategy
        // represented by our variables.
        val coefficients = ArrayList<Double>()
        for (innerIndex in mixedStrategyIndices) {
            if (innerIndex == outerIndex) {
                coefficients.add(0.5)
            } else {
                val leftWinRate = winRateOverrides[Pair(outerIndex, innerIndex)]
                        ?: mrs.getMatchupResultByIndices(outerIndex, innerIndex).getLeftWinningRate()
                coefficients.add(leftWinRate)
            }
        }
        coefficients.add(-1.0)
        constraints.add(LinearConstraint(coefficients.toDoubleArray(), Relationship.LEQ, 0.0))
    }
    // TODO: Add restrictions that probabilities are positive?
//    val rockDoesntBeatUs = LinearConstraint(doubleArrayOf(0.5, 0.0, 1.0, -1.0), Relationship.LEQ, 0.5)
//    val paperDoesntBeatUs = LinearConstraint(doubleArrayOf(1.0, 0.5, 0.0, -1.0), Relationship.LEQ, 0.5)
//    val scissorsDoesntBeatUs = LinearConstraint(doubleArrayOf(0.0, 1.0, 0.5, -1.0), Relationship.LEQ, 0.5)
    val allConstraints = LinearConstraintSet(constraints)
//        val data = listOf(objectiveFunction, allConstraints)
//    println("Starting the optimizer...")
    val startTime = System.nanoTime()
    val result = solver.optimize(objectiveFunction, allConstraints, NonNegativeConstraint(true))
//    println("Elapsed: ${(System.nanoTime() - startTime) / 1_000_000} ms")
//    println("Result: $result")
    //    println("Result: ${Arrays.toString(resultPoint)}")
    val resultStrat = Strategy(mrs.contestants)
    for ((arrayIndex, probability) in result.point.dropLast(1).withIndex()) {
        val stratIndex = mixedStrategyIndices[arrayIndex]
        resultStrat.setChoiceUnnormalizedWeight(stratIndex, probability)
    }
    return resultStrat
}
