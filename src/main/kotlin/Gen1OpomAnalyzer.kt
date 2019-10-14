package net.alloyggp.opom

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

fun main() {
    val resultsFolder = File("../Pokemon-Showdown/collectedStats")
    if (!resultsFolder.isDirectory) {
        error("Folder not found")
    }
    val movesets = ArrayList<String>()
    for (file in resultsFolder.listFiles()) {
//        println(file)
        movesets.add(file.name)
    }
    println(movesets)

    val mrs = MatchupResultStore(movesets)

    for (file in resultsFolder.listFiles()) {
        val left = file.name
        for (line in file.readLines()) {
            val parts = line.split(" ")
            val right = "${parts[0]}_${parts[1]}"
            val leftWins = Integer.parseInt(parts[2])
            val rightWins = Integer.parseInt(parts[3])
            val draws = Integer.parseInt(parts[4])
            if (draws > 0) {
                println("Found draws in matchup: $left $right")
            }
            // TODO: Store matchup results
            mrs.storeMatchupResult(left, right, MatchupResult(leftWins, rightWins, draws))
        }
    }
    println("Loaded all results")

    val scores = HashMap<String, Int>()
    for (left in movesets) {
        scores[left] = 0
        for (right in movesets) {
            if (left == right) continue

            scores[left] = scores[left]!! + mrs.getMatchupResult(left, right).leftWins
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

fun writeSortedScoresPage(sortedScores: List<Pair<String, Int>>) {
    BufferedWriter(FileWriter(File("sortedScores.html"))).use { writer ->
        writer.append("<html><head><title>Gen 1 OPOM - sorted by score</title></head>\n<body>\n")
        writer.append("<h1>Gen 1 OPOM sorted scores</h1>\n\n")

        writer.append("<table>\n")
        for ((moveset, score) in sortedScores) {
            val (pokemon, move) = moveset.split("_")
            writer.append("<tr><td>$score</td><td>${pokemon.capitalize()}</td><td>$move</td></tr>\n")
        }
        writer.append("</table>\n")

        writer.append("</body></html>\n")
    }
}
