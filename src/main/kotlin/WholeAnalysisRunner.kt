package net.alloyggp.opom

import net.alloyggp.opom.nash2.Strategy
import net.alloyggp.opom.nash2.solveForNashEquilibriumAmong
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

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

    AnalysisState(curAnalysisRoot, 1).run()
}

class AnalysisState(val analysisRoot: File, val gen: Int) {
    val combatantsOfInterest = TreeSet<String>()
    val statisticsDir = File(analysisRoot, "matchupStats")

    fun run() {
        if (!analysisRoot.exists()) {
            analysisRoot.mkdir()
        }

        for (i in 1..10) {
            println("Running single-elimination tournament...")
            val winner = runSingleElimTournamentGetWinner(gen)
            println("Found winner $winner")
            combatantsOfInterest.add(winner)
        }

//        combatantsOfInterest.add("kabutops_slash")
//        combatantsOfInterest.add("mewtwo_fireblast")
//        combatantsOfInterest.add("mewtwo_psychic")
//        combatantsOfInterest.add("mewtwo_seismictoss")

        println("Initial set: ${combatantsOfInterest}")
        // TODO: Collect a set of info among these, then compute an equilibrium
        // Then collect stats for combatants relative to the ones actually in the equilibrium, then continue...

        while (true) {
            val inGroupBoostTarget = 100 + (10 * combatantsOfInterest.size)
            println("Boosting among-in-group stats to $inGroupBoostTarget...")
            boostInGroupStatsToN(inGroupBoostTarget)

            val matchupResultStore = loadGen1Results(statisticsDir)
            val strategy = solveForNashEquilibriumAmong(getCombatantsOfInterestIndices(matchupResultStore), matchupResultStore)
            strategy.print()
            println("Strategy members / in-group members: ${strategy.getNonDefaultIndices().size}/${combatantsOfInterest.size}")

//            println("Boosting against-in-group stats to 1...")
//            boostAgainstStrategyStatsToN(strategy, matchupResultStore, 1)
//            findAndPrintBestAgainstStrategy(strategy, loadGen1Results(statisticsDir))

            // val boostTarget = 2 + (combatantsOfInterest.size / 2)
            val boostTarget = 5
            println("Boosting against-strategy stats to $boostTarget...")
            boostAgainstStrategyStatsToN(strategy, matchupResultStore, boostTarget)
            // TODO: Take the ones that are close and boost their numbers as if they were in the group, and _then_
            // pick the best one
            val bestAgainstStratList = findAndPrintBestAgainstStrategy(strategy, loadGen1Results(statisticsDir))
            boostInGroupStatsToN(inGroupBoostTarget, additional = bestAgainstStratList.map { it.combatant })
            val bestAgainstStrat = findAndPrintBestAgainstStrategy(strategy, loadGen1Results(statisticsDir))[0]
            if (bestAgainstStrat.winRate > 0.5) {
                println("Adding ${bestAgainstStrat} to in-group")
                combatantsOfInterest.add(bestAgainstStrat.combatant)
            } else {
                println("Maybe ran out of new entries? Ending for now")
                println("Final version of in-group:")
                for (combatant in combatantsOfInterest) {
                    println("  - $combatant")
                }
                break
            }
        }
    }

    data class CombatantWithWinRate(val combatant: String, val winRate: Double)

    private fun findAndPrintBestAgainstStrategy(strategy: Strategy, mrs: MatchupResultStore<String>): List<CombatantWithWinRate> {
        val contestants = ArrayList<CombatantWithWinRate>()
        for (i in 0 until mrs.contestants.size) {
            if (strategy.containsIndex(i)) {
                continue
            }
            val winRate = strategy.getWinRateAgainstThisStrategy(mrs, i)
            contestants.add(CombatantWithWinRate(mrs.contestants[i], winRate))
        }
        val bestFirst = contestants.sortedBy { it.combatant }.sortedByDescending { it.winRate }
        println("Best against this strategy:")
        for (contestant in bestFirst.take(10)) {
            println("  - ${contestant.winRate}: ${contestant.combatant}")
        }
        return bestFirst.shuffled().sortedByDescending { it.winRate }.take(5)
    }

    private fun getCombatantsOfInterestIndices(mrs: MatchupResultStore<String>): List<Int> {
        return combatantsOfInterest.map { mrs.getContestantIndex(it) }
    }

    // usually fast
    private fun boostInGroupStatsToN(minSampleSize: Int, additional: List<String> = listOf()) {
        val startTime = System.currentTimeMillis()
        runPokemonShowdown(gen, ActionType.COLLECT_STATS, listOf(
                statisticsDir.absolutePath,
                toJsonArray(combatantsOfInterest + additional),
                "among",
                minSampleSize.toString()
        ))
        val timeSeconds = (System.currentTimeMillis() - startTime) / 1000.0
        println("Collected statistics in ${timeSeconds} seconds")
    }
    // around 5-10 seconds per sample per in-strategy combatant
    // TODO: This is pretty slow at times; I think it would be better to find some more equitable way to divide stuff among
    // workers when they're running in parallel
    private fun boostAgainstStrategyStatsToN(strategy: Strategy, mrs: MatchupResultStore<String>, minSampleSize: Int) {
        val strategyCombatants = strategy.getNonDefaultIndices().map { mrs.contestants[it] }
        val startTime = System.currentTimeMillis()
        runPokemonShowdown(gen, ActionType.COLLECT_STATS, listOf(
                statisticsDir.absolutePath,
                toJsonArray(strategyCombatants),
                "outside",
                minSampleSize.toString()
        ))
        val timeSeconds = (System.currentTimeMillis() - startTime) / 1000.0
        println("Collected statistics in ${timeSeconds} seconds")
    }
}

// Not general-use, doesn't escape the strings
fun toJsonArray(strings: Iterable<String>): String {
    return strings.joinToString(
            separator = "\",\"",
            prefix = "[\"",
            postfix = "\"]")
}

// Takes around 40-50 seconds
fun runSingleElimTournamentGetWinner(gen: Int): String {
    val startTime = System.currentTimeMillis()
    val output = runPokemonShowdown(gen, ActionType.SINGLE_ELIM_TOURNAMENT, listOf())
    val timeSeconds = (System.currentTimeMillis() - startTime) / 1000.0
    println("Ran a single-elimination tournament in ${timeSeconds} seconds")
    return output.lines().findLast { it.startsWith("winner: ") }
            ?.removePrefix("winner: ") ?: throw RuntimeException("Output was:\n${output}")
}

enum class ActionType(val argName: String) {
    SINGLE_ELIM_TOURNAMENT("single_elim"),
    COLLECT_STATS("collect_stats"),
}

val scriptLocation = "/home/alandau/code/Pokemon-Showdown/run.sh"
fun runPokemonShowdown(gen: Int, action: ActionType, args: List<String>): String {
    val command = mutableListOf("bash", scriptLocation,
            "gen${gen}",
            action.argName)
    command.addAll(args)
    // TODO: Use better process execution
    val process = ProcessBuilder()
            .command(command)
            .directory(File(scriptLocation).parentFile)
//            .inheritIO()
            .start()
    val exitCode = process.waitFor()
    val baos = ByteArrayOutputStream()
    process.inputStream.transferTo(baos)
    val stdout = String(baos.toByteArray(), StandardCharsets.UTF_8)
    if (exitCode != 0) {
        val baos2 = ByteArrayOutputStream()
        process.errorStream.transferTo(baos2)
        val stderr = String(baos2.toByteArray(), StandardCharsets.UTF_8)
        throw RuntimeException("Process failed; stdout:\n${stdout}\n\nstderr:\n${stderr}")
    }
    return stdout
}
