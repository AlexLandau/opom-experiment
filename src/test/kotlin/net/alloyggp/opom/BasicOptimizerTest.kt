package net.alloyggp.opom

import org.apache.commons.math3.optim.linear.*
import org.junit.Test
import java.util.*

class BasicOptimizerTest {
    @Test
    fun test() {
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
        // Variables: (probabilities of) Rock, Paper, Scissors; Epsilon
        val objectiveFunction = LinearObjectiveFunction(doubleArrayOf(0.0, 0.0, 0.0, 1.0), 0.0)
        val probabilitiesSumUp = LinearConstraint(doubleArrayOf(1.0, 1.0, 1.0, 0.0), Relationship.EQ, 1.0)
        val rockNonNegative = LinearConstraint(doubleArrayOf(1.0, 0.0, 0.0, 0.0), Relationship.GEQ, 0.0)
        val paperNonNegative = LinearConstraint(doubleArrayOf(0.0, 1.0, 0.0, 0.0), Relationship.GEQ, 0.0)
        val scissorsNonNegative = LinearConstraint(doubleArrayOf(0.0, 0.0, 1.0, 0.0), Relationship.GEQ, 0.0)
        val epsilonNonNegative = LinearConstraint(doubleArrayOf(0.0, 0.0, 0.0, 1.0), Relationship.GEQ, 0.0)
        // TODO: Add restrictions that probabilities are positive?
        val rockDoesntBeatUs = LinearConstraint(doubleArrayOf(0.5, 0.0, 1.0, -1.0), Relationship.LEQ, 0.5)
        val paperDoesntBeatUs = LinearConstraint(doubleArrayOf(1.0, 0.5, 0.0, -1.0), Relationship.LEQ, 0.5)
        val scissorsDoesntBeatUs = LinearConstraint(doubleArrayOf(0.0, 1.0, 0.5, -1.0), Relationship.LEQ, 0.5)
        val allConstraints = LinearConstraintSet(probabilitiesSumUp, rockNonNegative, paperNonNegative, scissorsNonNegative, epsilonNonNegative, rockDoesntBeatUs, paperDoesntBeatUs, scissorsDoesntBeatUs)
//        val data = listOf(objectiveFunction, allConstraints)
        val startTime = System.nanoTime()
        val result = solver.optimize(objectiveFunction, allConstraints)
        println("Elapsed: ${(System.nanoTime() - startTime) / 1_000_000} ms")
        println("Result: $result")
        val resultPoint = result.point
        println("Result: ${Arrays.toString(resultPoint)}")
    }
}