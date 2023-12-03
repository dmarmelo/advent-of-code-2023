fun main() {

    fun IntRange.expandBy(n: Int, lowerBound: Int, upperBound: Int) =
        ((this.first - n).coerceAtLeast(lowerBound)..(this.last + n).coerceAtMost(upperBound))

    fun List<String>.expandSurrounding(row: Int, innerRange: IntRange, expandBy: Int) = buildList {
        val expandedRange = innerRange.expandBy(expandBy, 0, this@expandSurrounding[row].lastIndex)
        if (row > 0) {
            add(this@expandSurrounding[row - 1].substring(expandedRange))
        }
        add(this@expandSurrounding[row].substring(expandedRange))
        if (row < this@expandSurrounding.lastIndex) {
            add(this@expandSurrounding[row + 1].substring(expandedRange))
        }
    }

    fun String.findPartNumbers() =
        "(\\d+)".toRegex().findAll(this)
            .map { it.value.toInt() to it.range }

    fun part1(input: List<String>): Int {
        return input.flatMapIndexed { row, line ->
            line.findPartNumbers().mapNotNull { partNumber ->
                input.expandSurrounding(row, partNumber.second, 1)
                    .joinToString("")
                    .filterNot { it.isDigit() || it == '.' }
                    .let {
                        if (it.isNotEmpty()) partNumber.first
                        else null
                    }
            }
        }.sum()
    }

    infix fun IntRange.overlaps(other: IntRange) = first in other || last in other

    fun String.findPartNumbersOverlapRange(range: IntRange) =
        findPartNumbers()
            .filter { partNumber -> partNumber.second overlaps range }
            .map { it.first }
            .toList()

    fun List<String>.findPartNumbersOverlapRange(range: IntRange) = flatMap {
        it.findPartNumbersOverlapRange(range)
    }

    fun String.findGears() =
        "(\\*)".toRegex().findAll(this)
            .map { it.range }

    fun part2(input: List<String>): Int {
        return input.flatMapIndexed { row, line ->
            line.findGears().mapNotNull { gear ->
                input.expandSurrounding(row, gear, line.length)
                    .findPartNumbersOverlapRange(gear.expandBy(1, 0, line.lastIndex))
                    .let {
                        if (it.size == 2) it.product()
                        else null
                    }
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
