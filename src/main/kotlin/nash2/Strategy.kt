package net.alloyggp.opom.nash2

import net.alloyggp.opom.MatchupResultStore
import java.io.File
import java.text.NumberFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * Unlike the Strategy in the nash package, this one is real-valued (using Doubles).
 *
 * This also doesn't have "default" values.
 */
class Strategy(private val choiceNames: List<String>) {
//    private val choices: Array<Int> = Array(choiceNames.size, { 0 })
    private val choices: MutableMap<Int, Double> = HashMap()

    fun getChoicesSum(): Double {
        return choices.values.sum()
    }
    fun getChoiceUnnormalizedWeight(index: Int): Double {
        return choices[index] ?: 0.0
    }
    fun getChoiceNormalizedWeight(index: Int): Double {
        val unnormalized = choices[index] ?: return 0.0
        return unnormalized / getChoicesSum()
    }
    fun getNonDefaultValuesCount(): Int {
        return choices.size
    }
    fun getNonDefaultIndices(): SortedSet<Int> {
        return choices.keys.toSortedSet()
    }
    fun getNumOptions(): Int {
        return choiceNames.size
    }
    fun setChoiceUnnormalizedWeight(index: Int, newValue: Double) {
        if (!newValue.isFinite()) {
            error("Trying to set a non-finite weight: $newValue for index $index")
        }
        if (newValue <= 0.0) {
            choices.remove(index)
        } else {
            choices.put(index, newValue)
        }
    }

    fun print() {
        println("Cur strategy:")
        val stratMap = HashMap<String, Double>()
        val sum = getChoicesSum()
        for ((index, moveset) in choiceNames.withIndex()) {
            val count = getChoiceUnnormalizedWeight(index)
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
//            println("val strategyTweaks = mapOf(")
//            for ((index, moveset) in choiceNames.withIndex()) {
//                val count = getChoiceCount(index)
//                if (count > 0) {
//                    println("  \"$moveset\" to $count,")
//                }
//            }
//            println(")")
    }

    fun saveToFile(file: File, mrs: MatchupResultStore<String>) {
        if (!file.absoluteFile.parentFile.exists()) {
            file.absoluteFile.parentFile.mkdirs()
        }
        file.bufferedWriter().use { writer ->
            val minSampleSize = getSampleSizePropertyForChosen(this, mrs, Integer.MAX_VALUE, ::minOf)
            val maxSampleSize = getSampleSizePropertyForChosen(this, mrs, 0, ::maxOf)
            writer.appendLine("Min sample size (among chosen): $minSampleSize")
            writer.appendLine("Max sample size (among chosen): $maxSampleSize")
            writer.appendLine()
            for (index in getNonDefaultIndices()) {
                val count = choices[index]
                val name = choiceNames[index]
                writer.appendLine("$index $name $count")
            }
            writer.appendLine()
            data class ChoiceWinProb(val choice: String, val winProb: Double) {}
            val choiceWinProbs = ArrayList<ChoiceWinProb>()
            for (choiceIndex in mrs.contestants.indices) {
                val winRate = getWinRateAgainstThisStrategy(mrs, choiceIndex)
                choiceWinProbs.add(ChoiceWinProb(mrs.contestants[choiceIndex], winRate))
            }
            choiceWinProbs.sortByDescending { it.winProb }
            writer.appendLine("Estimated effectiveness of pure strategies against this strategy (may use smaller sample sizes):")
            for (cwb in choiceWinProbs) {
                writer.appendLine("${cwb.winProb} ~ ${cwb.choice}")
            }
        }
    }

    public fun getWinRateAgainstThisStrategy(mrs: MatchupResultStore<String>, choiceIndex: Int): Double {
        var effectivenessAgainstCurStrat = 0.0
        for (mixedStratIndex in this.choices.keys) {
            effectivenessAgainstCurStrat += this.getChoiceNormalizedWeight(mixedStratIndex) *
                                            mrs.getMatchupResultByIndices(choiceIndex, mixedStratIndex).getLeftWinningRate()
        }
        return effectivenessAgainstCurStrat
    }

    private fun getSampleSizePropertyForChosen(strategy: Strategy, mrs: MatchupResultStore<String>, startingValue: Int, reducer: (Int, Int) -> Int): Int {
        var valueSoFar = startingValue
        for (key1 in strategy.choices.keys) {
            for (key2 in strategy.choices.keys) {
                if (key1 == key2) {
                    continue
                }
//                mrs.getMatchupIndex(key1, key2)
                val name1 = strategy.choiceNames[key1]
                val name2 = strategy.choiceNames[key2]
                val samples = mrs.getMatchupResult(name1, name2).getSamples()
                valueSoFar = reducer(valueSoFar, samples)
            }
        }
        return valueSoFar
    }

    fun copy(): Strategy {
        val copy = Strategy(choiceNames)
        copy.choices.putAll(choices)
        return copy
    }

    fun getTopChoiceIndex(): Int {
        return choices.entries.maxByOrNull { it.value }!!.key
    }

    companion object {
        fun loadFromFile(file: File, choiceNames: List<String>): Strategy {
            val lines = file.readLines()
            val strategy = Strategy(choiceNames)
            for (line in lines) {
                val (indexStr, name, countStr) = line.split(" ")
                val index = Integer.parseInt(indexStr)
                val count = java.lang.Double.parseDouble(countStr)
                if (choiceNames[index] != name) {
                    error("Error parsing, likely incompatible strategy for the set of choices; index was $index, saved name was $name but current name is ${choiceNames[index]}")
                }
                strategy.setChoiceUnnormalizedWeight(index, count)
            }
            return strategy
        }

        fun loadFromFileOr(file: File, choiceName: List<String>, orElse: () -> Strategy): Strategy {
            if (!file.exists()) {
                return orElse()
            }
            return loadFromFile(file, choiceName)
        }
    }

}