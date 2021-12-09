package net.alloyggp.opom

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.util.regex.Pattern

val filenamePattern = Pattern.compile("([0-9]+)\\.([^.]+)\\.txt")

data class TimeSeriesData(
        val bestOverCurrentStrat: HashMap<Int, String>,
        val effectivenessVsPrev: HashMap<Int, Double>,
        val normalizedScoreOverEquilibrium: HashMap<Int, Double>,
        val rawScoreOverEquilibrium: HashMap<Int, Double>,
        val strats: HashMap<String, HashMap<Int, Double>>,
)

// Takes 300 seconds to run =/
fun main() {
    val timeSeriesDir = File("time-series")
    val bestOverCurrentStrat = HashMap<Int, String>()
    val effectivenessVsPrev = HashMap<Int, Double>()
    val normalizedScoreOverEquilibrium = HashMap<Int, Double>()
    val rawScoreOverEquilibrium = HashMap<Int, Double>()
    val strats = HashMap<String, HashMap<Int, Double>>()
    println("Starting to load time series data...")
    val startTime = System.currentTimeMillis()
    for (file in timeSeriesDir.listFiles()) {
        val matcher = filenamePattern.matcher(file.name)
        if (!matcher.matches()) {
            error("Didn't match: $file")
        }
        val iteration = matcher.group(1).toInt()
        val type = matcher.group(2)
        when (type) {
            "bestOverCurrentStrat" -> bestOverCurrentStrat.put(iteration, file.readText())
            "effectivenessVsPrev" -> effectivenessVsPrev.put(iteration, file.readText().toDouble())
            "normalizedScoreOverEquilibrium" -> normalizedScoreOverEquilibrium.put(iteration, file.readText().toDouble())
            "rawScoreOverEquilibrium" -> rawScoreOverEquilibrium.put(iteration, file.readText().toDouble())
            "strat" -> {
                var totalMoveCount = 0.0
                for (line in file.readLines()) {
                    if (line == "0" || line.isEmpty()) {
                        continue
                    }
                    val (moveId, moveName, moveCount) = line.split(" ")
                    totalMoveCount += moveCount.toInt()
                }
                for (line in file.readLines()) {
                    if (line == "0" || line.isEmpty()) {
                        continue
                    }
                    val (moveId, moveName, moveCount) = line.split(" ")
                    strats.computeIfAbsent(moveName, { HashMap() })
                    val movePercentage = moveCount.toInt() / totalMoveCount
                    strats.getValue(moveName).put(iteration, movePercentage)
                }
            }
            else -> error("unexpected type, file is $file")
        }
    }
    println("Done loading time series data")
    println("Took ${(System.currentTimeMillis() - startTime) / 1000} seconds")

    val timeSeriesDataObj = TimeSeriesData(bestOverCurrentStrat, effectivenessVsPrev, normalizedScoreOverEquilibrium, rawScoreOverEquilibrium, strats)

    val mapper = ObjectMapper()
    mapper.writeValue(File("time-series-data.json"), timeSeriesDataObj)

}
