package net.alloyggp.opom

import java.io.File
import java.util.*
import kotlin.collections.ArrayList

fun loadGen1Results(resultsFolder: File = File("../Pokemon-Showdown/collectedStats")): MatchupResultStore<String> {
    val startTime = System.currentTimeMillis()
    if (!resultsFolder.isDirectory) {
        error("Folder not found")
    }
    val movesets = ArrayList<String>()
    // Deterministic (remove file-system nondeterminism) but unpredictable
    for (file in resultsFolder.listFiles().sortedBy { it.name }.shuffled(Random(0))) {
//        println(file)
        movesets.add(file.name)
    }
//    println(movesets)

    val mrs = MatchupResultStore(movesets)

    for (file in resultsFolder.listFiles()) {
        val left = file.name
        for (line in file.readLines()) {
            val parts = line.split(" ")
            val right = parts[0]
            val leftWins = Integer.parseInt(parts[1])
            val rightWins = Integer.parseInt(parts[2])
            val draws = Integer.parseInt(parts[3])
            // TODO: Store matchup results
            mrs.storeMatchupResult(left, right, MatchupResult(leftWins, rightWins, draws))
        }
    }
    println("Loaded results in ${(System.currentTimeMillis() - startTime) / 1000.0} seconds")
    return mrs
}
