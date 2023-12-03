fun main() {
    fun part1(input: List<String>): Int {
        val height = input.size - 1
        val width = input[0].length - 1
        return input.flatMapIndexed { index, line ->
            "(\\d+)".toRegex().findAll(line).mapNotNull { number ->
                val range =
                    ((number.range.first - 1).coerceAtLeast(0)..(number.range.last + 1).coerceAtMost(width))
                val surrounding = buildString {
                    if (index > 0) {
                        append(input[index - 1].substring(range))
                    }
                    append(line.substring(range))
                    if (index < height) {
                        append(input[index + 1].substring(range))
                    }
                }.filter { it.isDigit().not() && it != '.' }

                if (surrounding.isNotEmpty()) number.value.toInt() else null
            }
        }.sum()
    }

    infix fun IntRange.intersects(other: IntRange) = first in other || last in other

    fun findPartNumbersInRange(input: String, range: IntRange) =
        "(\\d+)".toRegex().findAll(input)
            .filter { number -> number.range intersects range }
            .map { it.value.toInt() }
            .toList()

    fun part2(input: List<String>): Int {
        val height = input.size - 1
        val width = input[0].length - 1
        return input.flatMapIndexed { index, line ->
            "(\\*)".toRegex().findAll(line).mapNotNull { gear ->
                val range =
                    ((gear.range.first - 1).coerceAtLeast(0)..(gear.range.last + 1).coerceAtMost(width))
                val surrounding = buildList {
                    if (index > 0) {
                        addAll(findPartNumbersInRange(input[index - 1], range))
                    }
                    addAll(findPartNumbersInRange(line, range))
                    if (index < height) {
                        addAll(findPartNumbersInRange(input[index + 1], range))
                    }
                }
                if (surrounding.size == 2) surrounding.product() else null
            }
        }.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 4361)
    check(part2(testInput) == 467835)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}
