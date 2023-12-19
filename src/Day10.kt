private enum class CompassDirection {
    NORTH, SOUTH, EAST, WEST
}

private val CompassDirection.opposite
    get() = when (this) {
        CompassDirection.NORTH -> CompassDirection.SOUTH
        CompassDirection.SOUTH -> CompassDirection.NORTH
        CompassDirection.EAST -> CompassDirection.WEST
        CompassDirection.WEST -> CompassDirection.EAST
    }

private fun CompassDirection.isOpposite(other: CompassDirection) = this.opposite == other

private enum class Tile {
    VERTICAL,
    HORIZONTAL,
    BEND_NORTH_EAST,
    BEND_NORTH_WEST,
    BEND_SOUTH_WEST,
    BEND_SOUTH_EAST,
    GROUND,
    STARTING_POSITION
}

private fun Char.toTile() = when (this) {
    '|' -> Tile.VERTICAL
    '-' -> Tile.HORIZONTAL
    'L' -> Tile.BEND_NORTH_EAST
    'J' -> Tile.BEND_NORTH_WEST
    '7' -> Tile.BEND_SOUTH_WEST
    'F' -> Tile.BEND_SOUTH_EAST
    '.' -> Tile.GROUND
    'S' -> Tile.STARTING_POSITION
    else -> error("Found unknown tile: $this")
}

private fun Tile.toChar() = when (this) {
    Tile.VERTICAL -> '│'
    Tile.HORIZONTAL -> '─'
    Tile.BEND_NORTH_EAST -> '╰'
    Tile.BEND_NORTH_WEST -> '╯'
    Tile.BEND_SOUTH_WEST -> '╮'
    Tile.BEND_SOUTH_EAST -> '╭'
    Tile.GROUND -> '.'
    Tile.STARTING_POSITION -> 's'
}

private fun Tile.openings() = when (this) {
    Tile.VERTICAL -> listOf(CompassDirection.NORTH, CompassDirection.SOUTH)
    Tile.HORIZONTAL -> listOf(CompassDirection.EAST, CompassDirection.WEST)
    Tile.BEND_NORTH_EAST -> listOf(CompassDirection.NORTH, CompassDirection.EAST)
    Tile.BEND_NORTH_WEST -> listOf(CompassDirection.NORTH, CompassDirection.WEST)
    Tile.BEND_SOUTH_WEST -> listOf(CompassDirection.SOUTH, CompassDirection.WEST)
    Tile.BEND_SOUTH_EAST -> listOf(CompassDirection.SOUTH, CompassDirection.EAST)
    else -> emptyList()
}

private fun Tile.isPipe() = this in listOf(
    Tile.VERTICAL,
    Tile.HORIZONTAL,
    Tile.BEND_NORTH_EAST,
    Tile.BEND_NORTH_WEST,
    Tile.BEND_SOUTH_WEST,
    Tile.BEND_SOUTH_EAST
)

private typealias Map = List<List<Tile>>

fun main() {

    fun List<String>.parseInput() = map { line ->
        line.map { it.toTile() }
    }

    data class Point2D(
        val row: Int,
        val column: Int
    )

    data class Position(
        val point: Point2D,
        val to: CompassDirection,
        val tile: Tile
    )

    fun getPipesSurrounding(point: Point2D, map: Map) = buildList {
        if (point.row > 0)
            add(
                Position(
                    Point2D(point.row - 1, point.column),
                    CompassDirection.NORTH,
                    map[point.row - 1][point.column]
                )
            )
        if (point.row < map.lastIndex)
            add(
                Position(
                    Point2D(point.row + 1, point.column),
                    CompassDirection.SOUTH,
                    map[point.row + 1][point.column]
                )
            )
        if (point.column > 0)
            add(Position(Point2D(point.row, point.column - 1), CompassDirection.WEST, map[point.row][point.column - 1]))
        if (point.column < map.first().lastIndex)
            add(Position(Point2D(point.row, point.column + 1), CompassDirection.EAST, map[point.row][point.column + 1]))
    }.filter { it.tile.isPipe() }

    fun Tile.canGoTo(direction: CompassDirection) = isPipe() &&
            openings().contains(direction)

    fun Position.canGoTo(next: Position): Boolean {
        val bothPipes = tile.isPipe() && next.tile.isPipe()
        val canExitThrought = tile.openings().filterNot { it == to.opposite }
        val canEnterThrought = canExitThrought.map { it.opposite }
        val canPass = next.tile.openings().filter { it == next.to.opposite }.any { it in canEnterThrought }
        return bothPipes && canPass
    }

    fun part1(input: Map): Int {
        val startIndex = input.flatten().indexOfFirst { it == Tile.STARTING_POSITION }
        val startRow = startIndex / input.size
        val startColumn = startIndex % input.first().size

        val (start, finnish) = getPipesSurrounding(Point2D(startRow, startColumn), input)
            .filter { it.tile.canGoTo(it.to.opposite) }

        var current = start
        var count = 2
        while (true) {
            count++
            val next = getPipesSurrounding(current.point, input).first {
                current.canGoTo(it)
            }
            if (finnish.point == next.point) {
                break
            }
            current = next
        }
        return count / 2
    }

    fun part2(input: Map): Int {
        val startIndex = input.flatten().indexOfFirst { it == Tile.STARTING_POSITION }
        val startRow = startIndex / input.first().size
        val startColumn = startIndex % input.first().size

        val (start, finnish) = getPipesSurrounding(Point2D(startRow, startColumn), input)
            .filter { it.tile.canGoTo(it.to.opposite) }

        val sTile = Tile.entries.first { it.openings().containsAll(listOf(start.to, finnish.to)) }

        val pipePoints = mutableListOf(Point2D(startRow, startColumn) to sTile)

        var current = start
        pipePoints.add(start.point to start.tile)
        while (true) {
            val next = getPipesSurrounding(current.point, input).first {
                current.canGoTo(it)
            }
            pipePoints.add(next.point to next.tile)
            if (finnish.point == next.point) {
                break
            }
            current = next
        }

        val orderedPoints = pipePoints.groupBy { it.first.row }.values
            .sortedBy { it.first().first.row }
            .map { row -> row.sortedBy { it.first.column } }

        val pipeCoordinates = pipePoints.map { it.first }

        val holesPoints = List(input.size) { row ->
            List(input.first().size) { column ->
                Point2D(row, column)
            }
        }.flatten().filterNot { it in pipeCoordinates }

        val map = (pipePoints + holesPoints.map { it to Tile.GROUND }).groupBy { it.first.row }.values
            .sortedBy { it.first().first.row }
            .map { row ->
                row.sortedBy { it.first.column }
                    .map { it.second }
            }

        //map.onEach { println(it.map { it.toChar() }.joinToString("")) }

        var enclousedTilesCount = 0
        val enclousedTiles = mutableListOf<Point2D>()
        for ((irow, row) in map.withIndex()) {
            var withinLoop = false
            var pipeCount = 0
            var previousPipe: Tile? = null
            for ((icol, tile) in row.withIndex()) {
                // https://github.com/fred-corp/Advent-of-Code/blob/main/2023/day10/day10.js
                if (tile.isPipe() && tile != Tile.HORIZONTAL) {
                    val isVerticalWallExtended =
                        // F-J -> ╭─╯
                        (previousPipe == Tile.BEND_SOUTH_EAST && tile == Tile.BEND_NORTH_WEST) ||
                                // L-7 -> ╰─╮
                                (previousPipe == Tile.BEND_NORTH_EAST && tile == Tile.BEND_SOUTH_WEST)
                    if (!isVerticalWallExtended) {
                        pipeCount++
                    }
                    previousPipe = tile
                    withinLoop = pipeCount % 2 == 1
                }

                if (withinLoop && tile == Tile.GROUND) {
                    enclousedTilesCount++
                    enclousedTiles.add(Point2D(irow, icol))
                }
            }
        }
        map.onEachIndexed { irow, row ->
            row.mapIndexed { icol, tile ->
                if (Point2D(irow, icol) in enclousedTiles) '*'
                else tile.toChar()
            }.joinToString("").println()
        }
        return enclousedTilesCount
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day10_test").parseInput()
    check(part1(testInput) == 8)
    val testInput2 = readInput("Day10_test2").parseInput()
    check(part2(testInput2) == 4)
    val testInput3 = readInput("Day10_test3").parseInput()
    check(part2(testInput3) == 8)
    val testInput4 = readInput("Day10_test4").parseInput()
    check(part2(testInput4) == 10)

    val input = readInput("Day10").parseInput()
    part1(input).println()
    part2(input).println()
}
