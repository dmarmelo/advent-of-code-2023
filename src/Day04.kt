import kotlin.math.pow

fun main() {

    data class Scratchcard(
        val id: Int,
        val winningNumbers: Set<Int>,
        val numbers: Set<Int>
    ) {
        val matchingNumbersCount = numbers.intersect(winningNumbers).size
    }

    fun String.parseNumbers(): Set<Int> = split(" ")
        .filter { it.isNotBlank() }
        .map { it.trim().toInt() }
        .toHashSet()

    fun List<String>.parseInput() = map { line ->
        val (game, winningNumbers, numbers) = line.split(": ", " | ")
        val id = game.substringAfter("Card ")
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
        val cardCopyCount = IntArray(input.size) { 1 }
        for (sc in input) {
            repeat(sc.matchingNumbersCount) {
                cardCopyCount[it + sc.id] += cardCopyCount[sc.id - 1]
            }
        }
        return cardCopyCount.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test").parseInput()
    check(part1(testInput) == 13)
    check(part2(testInput) == 30)

    val input = readInput("Day04").parseInput()
    part1(input).println()
    part2(input).println()
}
