package net.alloyggp.opom.nash

import net.alloyggp.opom.loadGen1Results
import java.io.File
import java.util.*

fun main() {
    val mrs = loadGen1Results()
    val movesets = mrs.contestants
    println("Calculating...")

    // TODO: Come up with a ranking of movesets based on which is the most common move in a given NE, then
    // iteratively ban the highest and recalculate for what remains...

    val bannedList = BitSet(movesets.size)
    for (banIndex in 0 until movesets.size) {
        val banFile = File("banlists/$banIndex.ban")
        val stratFile = File("banlists/$banIndex.strategy")
        if (banFile.exists()) {
            val ban = banFile.readText()
            bannedList.set(movesets.indexOf(ban))
            println("$banIndex  $ban")
            continue
        }

        val resultStrat = runEquilibriumSearch(mrs, null, bannedList, 100_000)
        println("Saving results...")
        resultStrat.print()
        resultStrat.saveToFile(stratFile)
        val toBan = movesets[resultStrat.getTopChoiceIndex()]
        banFile.writeText(toBan)
        bannedList.set(movesets.indexOf(toBan))
        println("$banIndex  $toBan")
    }
//    runEquilibriumSearch(mrs, filename, bannedList, numIterations)
}