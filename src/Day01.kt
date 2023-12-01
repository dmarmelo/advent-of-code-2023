fun main() {
    fun part1(input: List<String>): Int {
        return input.sumOf { line ->
            "${line.first { it.isDigit() }}${line.last { it.isDigit() }}".toInt()
        }
    }

    val digits = List(10) { "$it" }
    val words = mapOf(
        "one" to 1,
        "two" to 2,
        "three" to 3,
        "four" to 4,
        "five" to 5,
        "six" to 6,
        "seven" to 7,
        "eight" to 8,
        "nine" to 9
    )
    val extendedDigits = digits + words.keys

    fun String.toInt(): Int {
        return toIntOrNull()
            ?: words[this]
            ?: error("$this is not a number")
    }

    fun part2(input: List<String>): Int {
        return input.map { line ->
            val firstDigit = line.findAnyOf(extendedDigits)?.second?.toInt()
                ?: error("could not find first digit in $line")
            val lastDigit = line.findLastAnyOf(extendedDigits)?.second?.toInt()
                ?: error("could not find last digit in $line")

            firstDigit to lastDigit
        }.sumOf { it.first * 10 + it.second }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    val testInput2 = readInput("Day01_test2")
    check(part1(testInput) == 142)
    check(part2(testInput2) == 281)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
