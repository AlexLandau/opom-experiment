package net.alloyggp.opom.nash

import java.io.File
import java.text.NumberFormat

class Strategy(private val choiceNames: List<String>, private val default: Int) {
    private val choices: Array<Int> = Array(choiceNames.size, { 0 })

    fun getChoicesSum(): Int {
        return choices.map { if (it == 0) default else it }.sum()
    }
    fun getChoiceCount(index: Int): Int {
        val count = choices[index]
        return if (count == 0) default else count
    }
    fun getNonDefaultValuesCount(): Int {
        return choices.count { it != 0 }
    }
    fun getNumOptions(): Int {
        return choices.size
    }
    fun getDefaultValuesCount(): Int {
        return getNumOptions() - getNonDefaultValuesCount()
    }
    fun incrementChoice(index: Int) {
        if (choices[index] == 0) {
            choices[index] = default + 1
        } else {
            choices[index]++
        }
    }
    fun incrementChoiceByN(index: Int, n: Int) {
        if (choices[index] == 0) {
            choices[index] = default + n
        } else {
            choices[index] += n
        }
        if (choices[index] == default) {
            choices[index] = 0
        }
    }
    fun setChoice(index: Int, count: Int) {
        if (count == default) {
            choices[index] = 0
        } else {
            choices[index] = count
        }
    }

    fun print() {
        if (default == 0) {
            println("Cur strategy:")
            val stratMap = HashMap<String, Int>()
            val sum = getChoicesSum().toDouble()
            for ((index, moveset) in choiceNames.withIndex()) {
                val count = getChoiceCount(index)
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
        } else {
            println("Cur strategy:")
            val sum = getChoicesSum().toDouble()
            for ((index, moveset) in choiceNames.withIndex()) {
                val count = getChoiceCount(index)
                if (count > 1) {
                    println("  $moveset: ${count / sum} (count: $count)")
                }
            }
            val theField = getDefaultValuesCount()
            println("  None of the above: ${theField / sum}")
        }
    }

    fun saveToFile(file: File) {
        if (!file.absoluteFile.parentFile.exists()) {
            file.absoluteFile.parentFile.mkdirs()
        }
        file.bufferedWriter().use { writer ->
            writer.appendln("$default")
            for ((index, name) in choiceNames.withIndex()) {
                val count = choices[index]
                if (count > 0) {
                    writer.appendln("$index $name $count")
                }
            }
        }
    }

    fun copy(): Strategy {
        val copy = Strategy(choiceNames, default)
        choices.copyInto(copy.choices)
        return copy
    }

    fun getTopChoiceIndex(): Int {
        return choices.indexOf(choices.max()!!)
    }

    companion object {
        fun loadFromFile(file: File, choiceNames: List<String>): Strategy {
            val lines = file.readLines()
            val default = Integer.parseInt(lines[0])
            val strategy = Strategy(choiceNames, default)
            for (line in lines.drop(1)) {
                val (indexStr, name, countStr) = line.split(" ")
                val index = Integer.parseInt(indexStr)
                val count = Integer.parseInt(countStr)
                if (choiceNames[index] != name) {
                    error("Error parsing, likely incompatible strategy for the set of choices; index was $index, saved name was $name but current name is ${choiceNames[index]}")
                }
                strategy.setChoice(index, count)
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
