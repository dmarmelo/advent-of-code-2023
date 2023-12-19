import kotlin.system.measureTimeMillis

fun main() {

    data class Point2D(
        val row: Int,
        val column: Int
    )

    fun Point2D.move(direction: Direction) = when (direction) {
        Direction.LEFT -> copy(column = column - 1)
        Direction.RIGHT -> copy(column = column + 1)
        Direction.UP -> copy(row = row - 1)
        Direction.DOWN -> copy(row = row + 1)
    }

    data class Beam(
        val position: Point2D,
        val flow: Direction
    )

    fun Beam.move(direction: Direction = flow) = copy(
        position = position.move(direction),
        flow = direction
    )

    fun Beam.flow(tile: Char) = when (tile) {
        '|' -> when (flow) {
            Direction.LEFT, Direction.RIGHT -> listOf(move(Direction.UP), move(Direction.DOWN))
            Direction.UP, Direction.DOWN -> listOf(move(flow))
        }

        '-' -> when (flow) {
            Direction.LEFT, Direction.RIGHT -> listOf(move(flow))
            Direction.UP, Direction.DOWN -> listOf(move(Direction.LEFT), move(Direction.RIGHT))
        }

        '/' -> when (flow) {
            Direction.LEFT -> listOf(move(Direction.DOWN))
            Direction.RIGHT -> listOf(move(Direction.UP))
            Direction.UP -> listOf(move(Direction.RIGHT))
            Direction.DOWN -> listOf(move(Direction.LEFT))
        }

        '\\' -> when (flow) {
            Direction.LEFT -> listOf(move(Direction.UP))
            Direction.RIGHT -> listOf(move(Direction.DOWN))
            Direction.UP -> listOf(move(Direction.LEFT))
            Direction.DOWN -> listOf(move(Direction.RIGHT))
        }

        else -> listOf(move())
    }

    fun solve(contraption: List<String>, startBeam: Beam): Int {
        val height = contraption.size
        val width = contraption.first().length

        val seen = mutableSetOf<Beam>()
        val queue = ArrayDeque<Beam>().apply { add(startBeam) }

        while (queue.isNotEmpty()) {
            val beam = queue.removeFirst()
            seen += beam

            val nextFlows = beam.flow(contraption[beam.position.row][beam.position.column])
                .filter { it.position.row in 0..<height && it.position.column in 0..<width }
                .filter { it !in seen }
            queue.addAll(nextFlows)
        }

        return seen.map { it.position }.toHashSet().size
    }

    fun part1(input: List<String>): Int {
        val start = Beam(position = Point2D(0, 0), flow = Direction.RIGHT)
        return solve(input, start)
    }

    fun part2(input: List<String>): Int {
        val height = input.size
        val width = input.first().length
        val possibleStarts = buildList {
            repeat(height) {
                add(Beam(position = Point2D(0, it), flow = Direction.RIGHT))
                add(Beam(position = Point2D(width - 1, it), flow = Direction.LEFT))
            }
            repeat(width) {
                add(Beam(position = Point2D(0, it), flow = Direction.DOWN))
                add(Beam(position = Point2D(height - 1, it), flow = Direction.UP))
            }
        }
        return possibleStarts.maxOf { solve(input, it) }
    }
2
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
    check(part1(testInput) == 46)
    check(part2(testInput) == 51)

    val input = readInput("Day16")
    measureTimeMillis {
        part1(input).println()
    }.also { println("Took ${it}ms") }
    measureTimeMillis {
        part2(input).println()
    }.also { println("Took ${it}ms") }
}
