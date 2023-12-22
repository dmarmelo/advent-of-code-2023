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

    fun Point2D.moveAllDirections() = Direction.entries.map { move(it) }

    fun List<String>.parseInput() = map { line ->
        line.map { it }
    }

    fun step(input: List<List<Char>>, currentPositions: Set<Point2D>) = currentPositions
        .flatMap { it.moveAllDirections() }
        .filter {
            val row = (if (it.row < 0) (it.row % input.size) + input.size else it.row) % input.size
            val column = (if (it.column < 0) (it.column % input.first().size) + input.first().size else it.column) % input.first().size
            input[row][column] in listOf('.', 'S')
        }
        .toSet()

    fun print(input: List<List<Char>>, currentPositions: Set<Point2D>) {
        input.forEachIndexed { irow, row ->
            row.mapIndexed { icolumn, column ->
                if (column != 'S' && Point2D(irow, icolumn) in currentPositions) 'O'
                else column
            }.joinToString("").also(::println)
        }
    }

    fun part1(input: List<List<Char>>, maxSteps: Int): Int {
        val mapWidth = input.first().size
        val startIndex = input.flatten().indexOf('S')
        val start = Point2D(startIndex / mapWidth, startIndex % mapWidth)

        var currentPositions = setOf(start)
        repeat(maxSteps) {
            currentPositions = step(input, currentPositions)
        }

        //print(input, currentPositions)

        return currentPositions.size
    }

    fun part2(input: List<List<Char>>): Long {
        val mapWidth = input.first().size
        val startIndex = input.flatten().indexOf('S')
        val start = Point2D(startIndex / mapWidth, startIndex % mapWidth)

        val maxSteps = 26501365
        val i = maxSteps % mapWidth

        // 65 (131/2), 196 (+131), 327 (+131)



        val first = part1(input, 65).also(::println)
        val second = part1(input, 196).also(::println)
        val third = part1(input, 327).also(::println)


        return input.size.toLong()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day21_test").parseInput()
    check(part1(testInput, 6) == 16)

    val input = readInput("Day21").parseInput()
    measureTimeMillis {
        part1(input, 64).println()
    }.also { println("Took ${it}ms") }
    measureTimeMillis {
        part2(input).println()
    }.also { println("Took ${it}ms") }
}
