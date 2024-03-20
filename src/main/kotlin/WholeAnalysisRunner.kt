package net.alloyggp.opom

import net.alloyggp.opom.nash2.Strategy
import net.alloyggp.opom.nash2.measureSensitivityToChanges
import net.alloyggp.opom.nash2.solveForNashEquilibriumAmong
import org.apache.commons.math3.stat.inference.AlternativeHypothesis
import org.apache.commons.math3.stat.inference.BinomialTest
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

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
    val curAnalysisRoot = File("newAnalysis2")

    AnalysisState(curAnalysisRoot, 1).run()
}

class AnalysisState(val analysisRoot: File, val gen: Int) {
    val combatantsOfInterest = TreeSet<String>()
    val statisticsDir = File(analysisRoot, "matchupStats")
    val savedIngroupFile = File(analysisRoot, "ingroup")

    fun run() {
        val wholeAnalysisStartTime = System.currentTimeMillis()
        if (!analysisRoot.exists()) {
            analysisRoot.mkdir()
        }

        if (!savedIngroupFile.exists() || loadInGroup().isEmpty()) {
            for (i in 1..10) {
                println("Running single-elimination tournament...")
                val winner = runSingleElimTournamentGetWinner(gen)
                println("Found winner $winner")
                combatantsOfInterest.add(winner)
            }
            saveInGroup()
        } else {
            combatantsOfInterest.addAll(loadInGroup())
        }

//        combatantsOfInterest.add("kabutops_slash")
//        combatantsOfInterest.add("mewtwo_fireblast")
//        combatantsOfInterest.add("mewtwo_psychic")
//        combatantsOfInterest.add("mewtwo_seismictoss")

        println("Initial set: ${combatantsOfInterest}")
        // TODO: Collect a set of info among these, then compute an equilibrium
        // Then collect stats for combatants relative to the ones actually in the equilibrium, then continue...

        for (passNumber in 1..2) {
            println("Starting pass $passNumber")
            while (true) {
                val inGroupBoostTarget = when (passNumber) {
                    1 -> 100 + (10 * combatantsOfInterest.size)
                    2 -> maxOf(100 + (10 * combatantsOfInterest.size), 1000)
                    else -> throw IllegalStateException()
                }
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
                val boostTarget = when (passNumber) {
                    1 -> 5
                    2 -> 20
                    else -> throw IllegalStateException()
                }
                println("Boosting against-strategy stats to $boostTarget...")
                boostAgainstStrategyStatsToN(strategy, matchupResultStore, boostTarget)
                // Take the ones that are close and boost their numbers as if they were in the group, and _then_ pick the best one
                val bestAgainstStratList = findAndPrintBestAgainstStrategy(strategy, loadGen1Results(statisticsDir))
                boostInGroupStatsToN(inGroupBoostTarget, additional = bestAgainstStratList.map { it.combatant })
                val mrs = loadGen1Results(statisticsDir)
                val bestOutsiderAgainstStrat = findAndPrintBestAgainstStrategy(strategy, mrs).first { !combatantsOfInterest.contains(it.combatant) }
                if (bestOutsiderAgainstStrat.winRate > 0.49) {
                    println("Adding ${bestOutsiderAgainstStrat} to in-group")
                    combatantsOfInterest.add(bestOutsiderAgainstStrat.combatant)
                    saveInGroup()
                } else {
                    println("Maybe ran out of new entries? Ending for now")
                    println("Final version of in-group:")
                    for (combatant in combatantsOfInterest) {
                        println("  - $combatant")
                    }

                    if (passNumber == 2) {
                        val sensitivities = measureSensitivityToChanges(strategy, combatantsOfInterest.toList(), mrs)
                        val sensitivitiesSum = sensitivities.values.sum()
                        println("Sum of sensitivies: $sensitivitiesSum")
                        println("Most sensitive matchups:")
                        for ((matchup, sensitivity) in sensitivities.entries.sortedByDescending { it.value }.take(10)) {
                            val (left, right) = matchup
                            println("  - $sensitivity: $left vs. $right")
                        }
                        if (sensitivitiesSum > 1.0) {
                            val matchups = sensitivities.entries.sortedByDescending { it.value }.take(8)
                            // TODO: We need an option in the sample collector to just target specific matchups
                            val boostTargets = ArrayList<MatchupBoostTarget>()
                            for (matchup in matchups) {
                                val (left, right) = matchup.key
                                boostTargets.add(MatchupBoostTarget(left, right, mrs.getMatchupResult(left, right).getSamples() + 1000))
                            }
                            // TODO: Run the thing, then continue
                            println("Boosting counts of those matchups...")
                            boostSpecificMatchups(boostTargets)
                            continue
                        }

                        val sumOfProbsOfOutsidersAbove50Percent = getSumOfProbsOfOutsidersAbove50Percent(strategy, mrs)
                        println("Sum of probabilities of unincluded being above 0.5: " + sumOfProbsOfOutsidersAbove50Percent)
                        if (sumOfProbsOfOutsidersAbove50Percent > 0.05) {
                            val (outsidersToSample, newSampleSize) = identifyOutsidersAndSamplesToAcquire(strategy, mrs)
                            boostInGroupStatsToN(newSampleSize, outsidersToSample)
                            println("New sum of probabilities of unincluded being above 0.5: " + getSumOfProbsOfOutsidersAbove50Percent(strategy, loadGen1Results(statisticsDir)))
                            continue
                        }
                    }

                    val toRemove = HashSet<String>()
                    for (combatant in combatantsOfInterest) {
                        val combatantIndex = matchupResultStore.getContestantIndex(combatant)
                        val winRate = strategy.getWinRateAgainstThisStrategy(matchupResultStore, combatantIndex)
                        if (winRate < 0.45) {
                            println("Will remove $combatant from in-group, win rate is $winRate")
                            toRemove.add(combatant)
                        }
                    }
                    combatantsOfInterest.removeAll(toRemove)
                    saveInGroup()
                    println("Time running whole analysis (if there wasn't a break in the middle): ${(System.currentTimeMillis() - wholeAnalysisStartTime) / 1000.0} seconds")
                    break
                }
            }
        }
    }

    data class OutsidersAndSample(val outsiders: List<String>, val newSampleSize: Int)
    private fun identifyOutsidersAndSamplesToAcquire(strategy: Strategy, mrs: MatchupResultStore<String>): OutsidersAndSample {
        data class SortableOutsiderData(val outsider: String, val winRate: Double, val pValue: Double, val sampleSize: Int)
        val outsiders = ArrayList<SortableOutsiderData>()
        for (outsider in mrs.contestants - combatantsOfInterest) {
            val outsiderIndex = mrs.getContestantIndex(outsider)
            val sampleSize = getMinSampleSizeAgainstStrategy(strategy, outsiderIndex, mrs)
            val winRate = strategy.getWinRateAgainstThisStrategy(mrs, outsiderIndex)
            val pValue = getPValueOfTrueValueAbove50Percent(outsider, strategy, mrs)
            outsiders.add(SortableOutsiderData(outsider, winRate, pValue, sampleSize))
        }
        // Very rough heuristic
        outsiders.sortByDescending { it.pValue / (sqrt(it.sampleSize.toDouble()) + 1) }
        val chosenOutsiders = outsiders.take(5)
        val firstChosen = chosenOutsiders[0]
        val newSampleSize = firstChosen.sampleSize + 50
        // TODO: Pass in a sensible p-value limit
        val filteredOutsiders = outsiders.filter { it.sampleSize < newSampleSize && it.sampleSize >= newSampleSize - 100 && it.pValue > (firstChosen.pValue / 10) }.take(5)
        println("Increasing samples to ${newSampleSize} for the following chosen outsiders:")
        for (o in filteredOutsiders) {
            println("  - ${o.outsider} (w=${o.winRate.toThreeDecimals()}, p=${o.pValue.toThreeDecimals()}, sample size=${o.sampleSize})")
        }
        return OutsidersAndSample(filteredOutsiders.map { it.outsider }, newSampleSize)
    }

    private fun getSumOfProbsOfOutsidersAbove50Percent(strategy: Strategy, mrs: MatchupResultStore<String>): Double {
        var sum = 0.0
        for (outsider in mrs.contestants - combatantsOfInterest) {
            sum += getPValueOfTrueValueAbove50Percent(outsider, strategy, mrs)
        }
        return sum
    }

    private fun getPValueOfTrueValueAbove50Percent(combatant: String, strategy: Strategy, mrs: MatchupResultStore<String>): Double {
        val index = mrs.getContestantIndex(combatant)
        // Neither the minimum sample size nor the sum of sample sizes is exactly right. What we actually want is an
        // adjusted version of the sample size with the combination of the highest impact on the overall score and the
        // lowest sample size relative to that. If the largest share of the strategy is 30%, for example, we want to
        // know: if you kept going at the same sample rate to expand that 30% to 100%, what would that sample rate be?
        // These could perhaps be combined more intelligently, but taking the minimum of these adjusted sample sizes
        // is conservative.
        val sampleSize = getMinAdjustedSampleSizeAgainstStrategy(strategy, index, mrs)
        val winRate = strategy.getWinRateAgainstThisStrategy(mrs, index)
        // To be conservative, we round up
        val numberOfWins = ceil(winRate * sampleSize).roundToInt()
        val result = BinomialTest().binomialTest(
                /*numberOfTrials =*/ sampleSize,
                /*numberOfSuccesses =*/ numberOfWins,
                /*probability =*/ 0.5,
                AlternativeHypothesis.LESS_THAN // i.e., the null hypothesis we want to reject is that it's high
        )
        //println("sample size $sampleSize, numWins $numberOfWins, result $result")
        return result
    }

    private fun getMinSampleSizeAgainstStrategy(strategy: Strategy, index: Int, mrs: MatchupResultStore<String>) =
            strategy.getNonDefaultIndices()
                    .filter { it != index }
                    .map { oppIndex -> mrs.getMatchupResultByIndices(index, oppIndex).getSamples() }
                    .minOrNull() ?: 0

    private fun getMinAdjustedSampleSizeAgainstStrategy(strategy: Strategy, index: Int, mrs: MatchupResultStore<String>) =
            strategy.getNonDefaultIndices()
                    .filter { it != index }
                    .map { oppIndex ->
                        val rawSampleSize = mrs.getMatchupResultByIndices(index, oppIndex).getSamples()
                        val weightInStrat = strategy.getChoiceNormalizedWeight(oppIndex)
                        // If we sliced 100% of the strategy into pieces as thin as these samples, how many slices?
                        val adjustedSampleSize = rawSampleSize / weightInStrat
                        // Round down to be conservative
                        Math.floor(adjustedSampleSize).roundToInt()
                    }
                    .minOrNull() ?: 0

    private fun saveInGroup() {
        savedIngroupFile.writeText(combatantsOfInterest.joinToString("\n"))
    }

    private fun loadInGroup(): List<String> {
        return savedIngroupFile.readLines().filter { it.isNotBlank() }
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
            println("  - ${contestant.winRate}: ${contestant.combatant}${if (combatantsOfInterest.contains(contestant.combatant)) "*" else ""} (p=${getPValueOfTrueValueAbove50Percent(contestant.combatant, strategy, mrs).toThreeDecimals()})")
        }
        val bestSubset = HashSet<CombatantWithWinRate>()
        // Make sure to return at least one not already in the in-group
        bestSubset.addAll(bestFirst.sortedByDescending { it.winRate }.filterNot { combatantsOfInterest.contains(it.combatant) }.take(1))
        bestSubset.addAll(bestFirst.sortedByDescending { it.winRate }.take(5))
        return bestSubset.shuffled().sortedByDescending { it.winRate }
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
    data class MatchupBoostTarget(val left: String, val right: String, val count: Int) {
        fun toJsonArrayString(): String {
            return "[\"$left\",\"$right\",$count]"
        }
    }

    private fun boostSpecificMatchups(boostTargets: ArrayList<MatchupBoostTarget>) {
        val startTime = System.currentTimeMillis()
        runPokemonShowdown(gen, ActionType.SPECIFIC_MATCHUPS, listOf(
                statisticsDir.absolutePath,
                boostTargets.map { it.toJsonArrayString() }.joinToString(",", "[", "]")
        ))
        val timeSeconds = (System.currentTimeMillis() - startTime) / 1000.0
        println("Boosted specific-matchup statistics in ${timeSeconds} seconds")
    }
}

private fun Double.toThreeDecimals(): String {
    val format = DecimalFormat()
    format.maximumFractionDigits = 3
    return format.format(this)
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
    SPECIFIC_MATCHUPS("specific_matchups"),
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
