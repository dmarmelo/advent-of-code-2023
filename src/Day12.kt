import kotlin.system.measureTimeMillis

fun main() {

    data class Line(
        val springs: String,
        val groups: List<Int>
    )

    fun List<String>.parseInput() = map { line ->
        val (springs, numbers) = line.split(' ')
        val groups = numbers.split(',').map { it.toInt() }
        Line(springs, groups)
    }

    // https://gist.github.com/Nathan-Fenner/781285b77244f06cf3248a04869e7161
    // https://github.com/Kroppeb/AdventOfCodeSolutions2/blob/master/solutions/src/solutions/y2023/day%2012.kt
    // https://www.reddit.com/r/adventofcode/comments/18hbbxe/2023_day_12python_stepbystep_tutorial_with_bonus/
    // https://www.reddit.com/r/adventofcode/comments/18hg99r/2023_day_12_simple_tutorial_with_memoization/
    // https://www.reddit.com/r/adventofcode/comments/18hbjdi/2023_day_12_part_2_this_image_helped_a_few_people/

    val cache = mutableMapOf<Line, Long>()

    fun solve(line: Line): Long {
        if (line.springs.isEmpty()) {
            return if (line.groups.isEmpty()) 1 else 0
        }

        /*if (numbers.isEmpty()) {
            return if (line.any { it == '#' }) 0 else 1
        }

        if (line.length < numbers.sum() + numbers.size - 1) {
            // The line is not long enough for all runs
            return 0
        }*/

        cache[line]?.let { return it }

        val value = when (line.springs.first()) {
            '.' -> solve(Line(line.springs.drop(1), line.groups))
            '#' -> {
                // No more groups
                if (line.groups.isEmpty()) {
                    return 0
                }
                // Current group count
                val current = line.groups.first()
                // String has enought length for the group?
                if (line.springs.length < current) {
                    return 0
                }
                // Is there a '.' in the length of the group
                if (line.springs.substring(0, current).any { it == '.' }) {
                    return 0
                }
                // Rest of the string needed for the group?
                if (line.springs.length == current) {
                    // Is the last group?
                    return if (line.groups.size == 1) 1
                    else 0
                }
                // Char next to the group cannot be a '#'
                if (line.springs[current] == '#') {
                    return 0
                }
                // All OK, continue with the next group
                return solve(Line(line.springs.drop(current + 1), line.groups.drop(1)))
            }

            else -> solve(Line('#' + line.springs.drop(1), line.groups)) +
                    solve(Line('.' + line.springs.drop(1), line.groups))
        }
        cache[line] = value
        return value
    }

    fun part1(input: List<Line>): Long {
        return input.sumOf(::solve)
    }

    fun Line.unfold() = Line(
        List(5) { springs }.joinToString("?"),
        List(5) { groups }.flatten()
    )

    fun part2(input: List<Line>): Long {
        return input.map { it.unfold() }.sumOf(::solve)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day12_test").parseInput()
    check(part1(testInput) == 21L)
    check(part2(testInput) == 525152L)

    val input = readInput("Day12").parseInput()
    measureTimeMillis {
        part1(input).println()
    }.also { println("Took ${it}ms") }
    measureTimeMillis {
        part2(input).println()
    }.also { println("Took ${it}ms") }
}
