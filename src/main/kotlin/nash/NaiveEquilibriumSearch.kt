package net.alloyggp.opom.nash

import net.alloyggp.opom.loadGen1Results
import java.io.File
import java.text.NumberFormat

// TODO: First, add the performance change
// TODO: Try adding decrements as well as increments to the strategy hillclimbing
// TODO: Start figuring out a condition for stopping the search
fun main() {
    // Do some slow (~1 iteration per second) hillclimbing towards something equilibrium-ish
    val mrs = loadGen1Results()
    val movesets = mrs.contestants
    println("Calculating...")

    // version 1
//    val curStrategy = Strategy(Array(movesets.size, { 1 }))
//    // Add some tweaks from previous runs
//    val strategyTweaks = mapOf(
//            "mewtwo_psychic" to 350,
//            "mewtwo_icebeam" to 152
//    )
    val filename = File("strat1.txt")
    val curStrategy = Strategy.loadFromFileOr(filename, movesets) {
        Strategy(movesets, 1)
    }
//    val curStrategy = Strategy(movesets, 1)
    // version 2
//    val filename = File("strat2.txt")
//    val curStrategy = Strategy.loadFromFile(filename, movesets)
//    val strategyTweaks = mapOf(
//            "chansey_bide" to 313,
//            "chansey_rest" to 52,
//            "chansey_seismictoss" to 29,
//            "cloyster_clamp" to 408,
//            "cloyster_rest" to 89,
//            "cloyster_toxic" to 59,
//            "dewgong_icebeam" to 1,
//            "dewgong_toxic" to 270,
//            "gengar_thunderbolt" to 108,
//            "gengar_toxic" to 21,
//            "haunter_bide" to 1,
//            "kabutops_slash" to 75,
//            "lapras_bide" to 313,
//            "lapras_blizzard" to 397,
//            "lapras_bodyslam" to 1,
//            "lapras_rest" to 4,
//            "mewtwo_bide" to 113,
//            "mewtwo_bodyslam" to 747,
//            "mewtwo_fireblast" to 182,
//            "mewtwo_icebeam" to 1139,
//            "mewtwo_psychic" to 406,
//            "mewtwo_recover" to 356,
//            "mewtwo_seismictoss" to 2,
//            "mewtwo_thunderbolt" to 704,
//            "omastar_rest" to 33,
//            "omastar_toxic" to 31,
//            "rhydon_dig" to 294,
//            "rhydon_earthquake" to 90,
//            "rhydon_seismictoss" to 89,
//            "snorlax_bide" to 5,
//            "snorlax_bodyslam" to 319
//    )
    curStrategy.incrementChoice(movesets.indexOf("mewtwo_psychic"))


//    for ((moveset, count) in strategyTweaks) {
//        val movesetIndex = movesets.indexOf(moveset)
//        curStrategy.choices[movesetIndex] = count
//    }

    var i = 0
    while (true) {
        // TODO: Cache this value for each moveset, then update each value once when the strategy changes
        fun getScoreAgainstCurStrategy(moveset: String): Double {
            var sum = 0.0
            for ((movesetIndex, curStratMoveset) in movesets.withIndex()) {
                val result = mrs.getMatchupResult(moveset, curStratMoveset)
                val contribution = result.getLeftWinningRate() * curStrategy.getChoiceCount(movesetIndex)
                sum += contribution
            }
            return sum
        }

        val (chosen, score) = movesets.map { it to getScoreAgainstCurStrategy(it) }.maxByOrNull { it.second }!!
        println("Picked $chosen with score $score")
        val chosenMovesetIndex = movesets.indexOf(chosen)
//        curStrategy.choices[chosenMovesetIndex]++
        curStrategy.incrementChoice(chosenMovesetIndex)
        i++
        if (i >= 50) {
            i = 0
            curStrategy.print()
            curStrategy.saveToFile(filename)
        }
    }
}



