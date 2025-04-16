package net.alloyggp.opom.numbers

import org.apache.commons.math3.optim.linear.*
import java.util.*

fun main() {
    val foundMaxValues = HashSet<Int>()
    var lastMaxValue = 1000001
    val lowestMaxWhereNumberIsUsed = HashMap<Int, Int>()
    while (lastMaxValue > 3) {
        val maxValue = lastMaxValue - 1
        println("Running with max value $maxValue")
        val bestStrat = findBestStrategy(maxValue)
        val actualMax = bestStrat.getHighestNonzeroNumber()
        println("Best strat with max value $maxValue (actual max $actualMax):")
        bestStrat.print()
        foundMaxValues.add(actualMax)
        for (number in bestStrat.getNonZeroNumbers()) {
            lowestMaxWhereNumberIsUsed.put(number, actualMax)
        }
        lastMaxValue = actualMax
    }
    println("Max values found: ${foundMaxValues.sorted()}")
    println("Where numbers are first used:")
    for (key in lowestMaxWhereNumberIsUsed.keys.sorted()) {
        println("$key: ${lowestMaxWhereNumberIsUsed[key]}")
    }
}

fun findBestStrategy(maxValue: Int): Strategy {
    val curMixedStrategyValues = TreeSet<Int>()
    curMixedStrategyValues.add(2)

    while (true) {
        val strategy = solveForNashEquilibriumAmong(curMixedStrategyValues.toList())

        var bestPureStratValue = -1
        var bestPureStratEffectiveness = -1.0
        for (pureStratValue in 1..maxValue) {
            var effectivenessAgainstCurStrat = 0.0
            for (mixedStratIndex in curMixedStrategyValues) {
                effectivenessAgainstCurStrat += strategy.getChoiceNormalizedWeight(mixedStratIndex) *
                        getLeftWinningRate(pureStratValue, mixedStratIndex)
            }
            if (effectivenessAgainstCurStrat > bestPureStratEffectiveness) {
                bestPureStratValue = pureStratValue
                bestPureStratEffectiveness = effectivenessAgainstCurStrat
            }
        }

        if (curMixedStrategyValues.contains(bestPureStratValue)) {
            return strategy
        }
        curMixedStrategyValues.add(bestPureStratValue)
    }
}

fun getLeftWinningRate(leftValue: Int, rightValue: Int): Double {
    if (leftValue == rightValue) {
        return 0.5
    }
    if (leftValue.mod(rightValue) == 0) {
        // left is a multiple of right
        return 1.0
    }
    if (rightValue.mod(leftValue) == 0) {
        return 0.0
    }
    if (leftValue < rightValue) {
        return 1.0
    } else {
        return 0.0
    }
}

fun solveForNashEquilibriumAmong(
    mixedStrategyNumbers: List<Int>): Strategy {
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
    val numStrats = mixedStrategyNumbers.size

    val objectiveFunction = LinearObjectiveFunction(Collections.nCopies(numStrats, 0.0).plus(1.0).toDoubleArray(), 0.0)
    val probabilitiesSumUp = LinearConstraint(Collections.nCopies(numStrats, 1.0).plus(0.0).toDoubleArray(), Relationship.EQ, 1.0)
//    val probabilitiesSumUp = LinearConstraint(doubleArrayOf(1.0, 1.0, 1.0, 0.0), Relationship.EQ, 1.0)
//    val rockNonNegative = LinearConstraint(doubleArrayOf(1.0, 0.0, 0.0, 0.0), Relationship.GEQ, 0.0)
//    val paperNonNegative = LinearConstraint(doubleArrayOf(0.0, 1.0, 0.0, 0.0), Relationship.GEQ, 0.0)
//    val scissorsNonNegative = LinearConstraint(doubleArrayOf(0.0, 0.0, 1.0, 0.0), Relationship.GEQ, 0.0)
//    val epsilonNonNegative = LinearConstraint(doubleArrayOf(0.0, 0.0, 0.0, 1.0), Relationship.GEQ, 0.0)
    val constraints = ArrayList<LinearConstraint>()
    constraints.add(probabilitiesSumUp)
    for (outerNumber in mixedStrategyNumbers) {
        // The value we're computing represents how well the outerNumber strategy does against the mixed strategy
        // represented by our variables.
        val coefficients = ArrayList<Double>()
        for (innerNumber in mixedStrategyNumbers) {
            val leftWinRate = getLeftWinningRate(outerNumber, innerNumber)
            coefficients.add(leftWinRate)
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
    val resultStrat = Strategy()
    for ((arrayIndex, probability) in result.point.dropLast(1).withIndex()) {
        val stratNumber = mixedStrategyNumbers[arrayIndex]
        resultStrat.setChoiceUnnormalizedWeight(stratNumber, probability)
    }
    return resultStrat
}
