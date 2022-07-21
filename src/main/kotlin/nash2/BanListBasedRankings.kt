package net.alloyggp.opom.nash2

import net.alloyggp.opom.loadGen1Results

fun main() {
    val mrs = loadGen1Results()
    val movesets = mrs.contestants

    val banList = HashSet<Int>()

    val allPokemonInOrder = ArrayList<String>()

    while (banList.size < movesets.size) {
        val theStrat = findBestStrategy(mrs, banList)
        val pokemonPcts = HashMap<String, Double>()
        for (movesetIndex in theStrat.getNonDefaultIndices()) {
            val moveset = movesets[movesetIndex]
            val pokemon = moveset.split("_")[0]
            val curPct = theStrat.getChoiceNormalizedWeight(movesetIndex)
            pokemonPcts[pokemon] = (pokemonPcts[pokemon] ?: 0.0) + curPct
        }
        println(pokemonPcts)
        val sortedEntries = pokemonPcts.entries.sortedByDescending { it.value }
        val bestPokemon = sortedEntries[0].key
        val bestPokemonScore = sortedEntries[0].value
        val nextBestPokemon = if (sortedEntries.size == 1) "(no runner-up)" else sortedEntries[1].key
        val nextBestScore = if (sortedEntries.size == 1) 0.0 else sortedEntries[1].value
        println("Best Pokemon: $bestPokemon ($bestPokemonScore)")
        println("                    Runner-up: $nextBestPokemon (${nextBestScore})")
        allPokemonInOrder.add(bestPokemon)

        for ((index, moveset) in movesets.withIndex()) {
            if (moveset.startsWith("${bestPokemon}_")) {
                banList.add(index)
            }
        }
    }

    for ((index, pokemon) in allPokemonInOrder.withIndex()) {
        println("${index + 1}. ${pokemon.capitalize()}")
    }
}