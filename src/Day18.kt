import kotlin.math.abs
import kotlin.system.measureTimeMillis

fun main() {

    data class Point2D(
        val row: Long,
        val column: Long
    )

    fun Point2D.move(direction: Direction, steps: Long) = when (direction) {
        Direction.LEFT -> copy(column = column - steps)
        Direction.RIGHT -> copy(column = column + steps)
        Direction.UP -> copy(row = row - steps)
        Direction.DOWN -> copy(row = row + steps)
    }

    data class Instruction(
        val direction: Direction,
        val steps: Long,
        val color: String
    )

    fun String.toDirection() = when (this) {
        "U" -> Direction.UP
        "D" -> Direction.DOWN
        "L" -> Direction.LEFT
        "R" -> Direction.RIGHT
        else -> error("Unknown direction: $this")
    }

    fun Int.toDirection() = when (this) {
        3 -> Direction.UP
        1 -> Direction.DOWN
        2 -> Direction.LEFT
        0 -> Direction.RIGHT
        else -> error("Unknown direction: $this")
    }

    fun Point2D.move(instruction: Instruction) = move(instruction.direction, instruction.steps)

    fun List<String>.parseInput() = map { line ->
        val (direction, steps, color) = "(\\w) (\\d+) \\(#(.+)\\)".toRegex().matchEntire(line)!!.destructured
        Instruction(direction.toDirection(), steps.toLong(), color)
    }

    fun List<Instruction>.edges() =
        runningFold(Point2D(0, 0)) { acc, instruction ->
            acc.move(instruction)
        }

    fun List<Instruction>.fix() = map { instruction ->
        val steps = instruction.color.take(5).toLong(16)
        val direction = instruction.color.last().digitToInt().toDirection()
        instruction.copy(direction = direction, steps = steps)
    }

    fun List<Point2D>.perimeter() = zipWithNext { p1, p2 ->
        abs(p2.row - p1.row) + abs(p2.column - p1.column)
    }.sum()

    // https://www.themathdoctors.org/polygon-coordinates-and-areas/
    // https://en.wikipedia.org/wiki/Shoelace_formula
    // https://en.wikipedia.org/wiki/Pick%27s_theorem
    // https://www.reddit.com/r/adventofcode/comments/18l8mao/2023_day_18_intuition_for_why_spoiler_alone/
    // https://todd.ginsberg.com/post/advent-of-code/2023/day18/

    fun List<Point2D>.shoelace() = zipWithNext { p1, p2 ->
        p1.row * p2.column - p2.row * p1.column
    }.sum().let { abs(it) / 2 }

    fun solve(input: List<Instruction>): Long {
        val edges = input.edges()
        return edges.shoelace() + edges.perimeter() / 2 + 1
    }

    fun part1(input: List<Instruction>): Long {
        return solve(input)
    }

    fun part2(input: List<Instruction>): Long {
        return solve(input.fix())
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day18_test").parseInput()
    check(part1(testInput) == 62L)
    check(part2(testInput) == 952_408_144_115L)

    val input = readInput("Day18").parseInput()
    measureTimeMillis {
        part1(input).println()
    }.also { println("Took ${it}ms") }
    measureTimeMillis {
        part2(input).println()
    }.also { println("Took ${it}ms") }
}
