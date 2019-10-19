package net.alloyggp.opom

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.NumberFormat

fun main() {
    val mrs = loadGen1Results()
    val movesets = mrs.contestants
    println("Loaded all results")

    val scores = HashMap<String, Double>()
    for (left in movesets) {
        scores[left] = 0.0
        for (right in movesets) {
            if (left == right) continue

            val matchupResult = mrs.getMatchupResult(left, right)
            scores[left] = scores[left]!! + matchupResult.getLeftWinningRate()
        }
    }

    println(scores)

    val sortedScores = scores.toList().sortedBy { it.second }.reversed()

//    sortedScores.forEach { (name: String, score: Int) ->
//        println("$score - $name")
//    }

    val highestPerSpecies = {
        val alreadyFound = HashSet<String>()
        sortedScores.filter {
            val pokemon = it.first.split("_")[0]
            if (alreadyFound.contains(pokemon)) {
                false
            } else {
                alreadyFound.add(pokemon)
                true
            }
        }
    }()

    writeSortedScoresPage(sortedScores)

    for ((name, score) in highestPerSpecies) {
        println("$score - $name")
    }
}

fun writeSortedScoresPage(sortedScores: List<Pair<String, Double>>) {
    BufferedWriter(FileWriter(File("sortedScores.html"))).use { writer ->
        writer.append("<html><head><title>Gen 1 OPOM - sorted by score</title></head>\n<body>\n")
        writer.append("<h1>Gen 1 OPOM sorted scores</h1>\n\n")

        writer.append("<table>\n")
        val doubleFormatter = NumberFormat.getInstance()
        doubleFormatter.isGroupingUsed = false
        doubleFormatter.maximumFractionDigits = 2
        for ((moveset, score) in sortedScores) {
            val (pokemon, move) = moveset.split("_")
            val formattedScore = doubleFormatter.format(score)
            writer.append("<tr><td>$formattedScore</td><td>${pokemon.capitalize()}</td><td>$move</td></tr>\n")
        }
        writer.append("</table>\n")

        writer.append("</body></html>\n")
    }
}
