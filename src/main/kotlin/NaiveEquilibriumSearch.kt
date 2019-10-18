package net.alloyggp.opom

import javafx.util.converter.DoubleStringConverter
import java.text.NumberFormat

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
    // version 2
    val curStrategy = Strategy(Array(movesets.size, { 0 }))
    val strategyTweaks = mapOf(
            "chansey_bide" to 293,
            "chansey_rest" to 52,
            "chansey_seismictoss" to 29,
            "cloyster_clamp" to 346,
            "cloyster_rest" to 50,
            "cloyster_toxic" to 21,
            "dewgong_icebeam" to 1,
            "dewgong_toxic" to 117,
            "gengar_thunderbolt" to 108,
            "gengar_toxic" to 21,
            "haunter_bide" to 1,
            "kabutops_slash" to 75,
            "lapras_bide" to 313,
            "lapras_blizzard" to 299,
            "lapras_bodyslam" to 1,
            "lapras_rest" to 4,
            "mewtwo_bide" to 81,
            "mewtwo_bodyslam" to 633,
            "mewtwo_fireblast" to 182,
            "mewtwo_icebeam" to 819,
            "mewtwo_psychic" to 322,
            "mewtwo_recover" to 292,
            "mewtwo_seismictoss" to 2,
            "mewtwo_thunderbolt" to 423,
            "omastar_rest" to 33,
            "omastar_toxic" to 31,
            "rhydon_dig" to 100,
            "rhydon_earthquake" to 39,
            "rhydon_seismictoss" to 89,
            "snorlax_bide" to 5,
            "snorlax_bodyslam" to 319
    )


    for ((moveset, count) in strategyTweaks) {
        val movesetIndex = movesets.indexOf(moveset)
        curStrategy.choices[movesetIndex] = count
    }

    var i = 0
    while (true) {
        fun getScoreAgainstCurStrategy(moveset: String): Double {
            var sum = 0.0
            for ((movesetIndex, curStratMoveset) in movesets.withIndex()) {
                val result = mrs.getMatchupResult(moveset, curStratMoveset)
                val contribution = (result.leftWins + (result.draws / 2.0)) * curStrategy.choices[movesetIndex]
                sum += contribution
            }
            return sum
        }

        val (chosen, score) = movesets.map { it to getScoreAgainstCurStrategy(it) }.maxBy { it.second }!!
        println("Picked $chosen with score $score")
        val chosenMovesetIndex = movesets.indexOf(chosen)
        curStrategy.choices[chosenMovesetIndex]++
        i++
        if (i >= 50) {
            i = 0
            printStrategy(curStrategy, movesets)
        }
    }
}


fun printStrategy(curStrategy: Strategy, movesets: List<String>) {
    if (curStrategy.choices.contains(0)) {
        println("Cur strategy:")
        val stratMap = HashMap<String, Int>()
        val sum = curStrategy.choices.sum().toDouble()
        for ((index, moveset) in movesets.withIndex()) {
            val count = curStrategy.choices[index]
            if (count > 0) {
                stratMap[moveset] = count
//                println("  $moveset: ${count / sum} (count: $count)")
            }
        }
        val percentFormatter = NumberFormat.getPercentInstance()
        percentFormatter.maximumFractionDigits = 3
        for ((moveset, count) in stratMap.toList().sortedByDescending { it.second }) {
            println("  $moveset: ${percentFormatter.format(count / sum)}")
        }
        // Print an "importable" version
        println("val strategyTweaks = mapOf(")
        for ((index, moveset) in movesets.withIndex()) {
            val count = curStrategy.choices[index]
            if (count > 0) {
                println("  \"$moveset\" to $count,")
            }
        }
        println(")")
    } else {
        println("Cur strategy:")
        val sum = curStrategy.choices.sum().toDouble()
        for ((index, moveset) in movesets.withIndex()) {
            val count = curStrategy.choices[index]
            if (count > 1) {
                println("  $moveset: ${count / sum} (count: $count)")
            }
        }
        val theField = curStrategy.choices.count { it == 1 }
        println("  None of the above: ${theField / sum}")
    }
}

class Strategy(val choices: Array<Int>)