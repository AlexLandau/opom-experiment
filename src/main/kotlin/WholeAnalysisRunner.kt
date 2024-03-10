package net.alloyggp.opom

import java.io.File

// This is intended to automate the process of both running simulations to get additional samples
// (and thus more accurate matchup numbers) and computing Nash equilibria (using the fast linear
// programming method) to better target the search.

// Idea: for a given combatant, try to find the vector of matchup deltas of a given length that maximizes
// the change in that combatant's percentage of the overall strategy (or, simpler, the single change
// to a matchup that maximizes changes). Use the ratio between the percentage change and the given delta
// vector length (fixed across the different combatants we're testing) to see which combatants are more
// or less secure in their positioning.

// Test case for the above: RPS but with two scissors cases, and the matchup for the scissors somewhere close to 0.5

fun main() {
    val curAnalysisRoot = File("newAnalysis1")

    AnalysisState(curAnalysisRoot).run()
}

class AnalysisState(val analysisRoot: File) {
    val combatantsOfInterest = ArrayList<String>()

    fun run() {
        if (!analysisRoot.exists()) {
            analysisRoot.mkdir()
        }


    }
}

enum class ActionType(val argName: String) {
    SINGLE_ELIM_TOURNAMENT("singleelim")
}

val executableLocation = "/home/"
val runPokemonShowdown(gen: Int, action: ActionType): String {

    ProcessBuilder()

}
