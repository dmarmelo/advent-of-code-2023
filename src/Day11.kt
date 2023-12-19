import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun main() {

    // https://github.com/Zordid/adventofcode-kotlin-2023/blob/main/src/main/kotlin/utils/Combinatorics.kt
    // https://gist.github.com/dmarmelo/c4be4852c5ab87dfa8802dd7926a9296
    fun <T> List<T>.combinations(size: Int): Sequence<List<T>> =
        when (size) {
            0 -> emptySequence()
            1 -> asSequence().map { listOf(it) }
            else -> sequence {
                this@combinations.forEachIndexed { index, element ->
                    val head = listOf(element)
                    val tail = this@combinations.subList(index + 1, this@combinations.size)
                    tail.combinations(size - 1).forEach { tailCombinations ->
                        yield(head + tailCombinations)
                    }
                }
            }
        }

    data class Point2D(
        val row: Int,
        val column: Int
    )

    data class Map(
        val emptyRows: List<Int>,
        val emptyColumns: List<Int>,
        val galaxies: List<Point2D>
    )

    fun List<String>.parseInput(): Map {
        val emptyRows = withIndex().filter { it.value.all { it == '.' } }.map { it.index }
        val emptyColumns = first().indices.map { column ->
            indices.map { this[it][column] }
        }.withIndex().filter { it.value.all { it == '.' } }.map { it.index }

        val galaxies = flatMapIndexed { row, line ->
            line.mapIndexedNotNull { column, char ->
                if (char == '#') Point2D(row, column)
                else null
            }
        }

        return Map(emptyRows, emptyColumns, galaxies)
    }

    fun Map.solve(expansionRatio: Int) = galaxies.combinations(2)
        .map { (g1, g2) ->
            val startRow = min(g1.row, g2.row)
            val endRow = max(g1.row, g2.row)

            val startColumn = min(g1.column, g2.column)
            val endColumn = max(g1.column, g2.column)

            val emptyRows = emptyRows.count { it in (startRow + 1)..<endRow } * expansionRatio.toLong()
            val emptyColumns = emptyColumns.count { it in (startColumn + 1)..<endColumn } * expansionRatio.toLong()

            abs(g2.row - g1.row) + emptyRows + abs(g2.column - g1.column) + emptyColumns
        }.sum()

    fun part1(input: Map): Long {
        return input.solve(1)
    }

    fun part2(input: Map): Long {
        return input.solve(999_999)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day11_test").parseInput()
    check(part1(testInput) == 374L)
    check(part2(testInput) == 82000210L)

    val input = readInput("Day11").parseInput()
    part1(input).println()
    part2(input).println()
}
