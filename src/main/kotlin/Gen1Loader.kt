package net.alloyggp.opom

import java.io.File

fun loadGen1Results(): MatchupResultStore<String> {
    val resultsFolder = File("../Pokemon-Showdown/collectedStats")
    if (!resultsFolder.isDirectory) {
        error("Folder not found")
    }
    val movesets = ArrayList<String>()
    for (file in resultsFolder.listFiles()) {
//        println(file)
        movesets.add(file.name)
    }
//    println(movesets)

    val mrs = MatchupResultStore(movesets)

    for (file in resultsFolder.listFiles()) {
        val left = file.name
        for (line in file.readLines()) {
            val parts = line.split(" ")
            val right = "${parts[0]}_${parts[1]}"
            val leftWins = Integer.parseInt(parts[2])
            val rightWins = Integer.parseInt(parts[3])
            val draws = Integer.parseInt(parts[4])
            // TODO: Store matchup results
            mrs.storeMatchupResult(left, right, MatchupResult(leftWins, rightWins, draws))
        }
    }
    return mrs
}
