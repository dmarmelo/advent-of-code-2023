import kotlin.math.pow

fun main() {

    data class Scratchcard(
        val id: Int,
        val winningNumbers: List<Int>,
        val numbers: List<Int>
    ) {
        val matchingNumbersCount = numbers.count { it in winningNumbers }
    }

    fun String.parseNumbers(): List<Int> = split("\\s+".toRegex())
        .filter { it.isNotBlank() }
        .map { it.trim().toInt() }

    fun List<String>.parseInput() = map { line ->
        val (game, rest) = line.split(": ")
        val id = game.substringAfter(" ")
        val (winningNumbers, numbers) = rest.split(" | ")
        Scratchcard(
            id = id.trim().toInt(),
            winningNumbers = winningNumbers.parseNumbers(),
            numbers = numbers.parseNumbers()
        )
    }

    fun part1(input: List<Scratchcard>): Int {
        return input.map { it.matchingNumbersCount }
            .sumOf { (1 * 2.0.pow(it - 1)).toInt() }
    }

    fun part2(input: List<Scratchcard>): Int {
        val cardCopyCount = mutableMapOf<Int, Int>()
        return input.sumOf { sc ->
            val currentCardInstances = cardCopyCount[sc.id] ?: 1
            repeat(sc.matchingNumbersCount) {
                val newCardCopyId = it + 1 + sc.id
                cardCopyCount[newCardCopyId] =
                    cardCopyCount[newCardCopyId]?.plus(currentCardInstances)
                        ?: (currentCardInstances + 1)
            }
            currentCardInstances
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test").parseInput()
    check(part1(testInput) == 13)
    check(part2(testInput) == 30)

    val input = readInput("Day04").parseInput()
    part1(input).println()
    part2(input).println()
}
