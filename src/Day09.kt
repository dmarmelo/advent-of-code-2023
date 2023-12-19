fun main() {

    fun List<String>.parseInput() = map { line ->
        line.split(" ").map { it.toInt() }
    }

    fun calculateNextValue(sequence: List<Int>): Int {
        val diffs = mutableListOf(sequence)
        do {
            diffs.add(diffs.last().windowed(2).map { (v1, v2) -> v2 - v1 })
        } while (!diffs.last().all { it == 0 })
        return diffs.map { it.last() }.reduce { acc, value -> acc + value }
    }

    fun part1(input: List<List<Int>>): Int {
        return input.sumOf(::calculateNextValue)
    }

    fun part2(input: List<List<Int>>): Int {
        return input.sumOf { calculateNextValue(it.reversed()) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day09_test").parseInput()
    check(part1(testInput) == 114)
    check(part2(testInput) == 2)

    val input = readInput("Day09").parseInput()
    part1(input).println()
    part2(input).println()
}
