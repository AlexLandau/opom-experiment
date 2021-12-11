package net.alloyggp.opom.nash2

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

    fun saveToFile(file: File) {
        if (!file.absoluteFile.parentFile.exists()) {
            file.absoluteFile.parentFile.mkdirs()
        }
        file.bufferedWriter().use { writer ->
            for (index in getNonDefaultIndices()) {
                val count = choices[index]
                val name = choiceNames[index]
                writer.appendLine("$index $name $count")
            }
        }
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