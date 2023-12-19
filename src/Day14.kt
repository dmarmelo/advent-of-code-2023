private enum class TiltDirection {
    NORTH, WEST, SOUTH, EAST
}

fun main() {

    fun List<String>.transpose() = first().indices.map { column ->
        indices.map { this[it][column] }.joinToString("")
    }

    /*fun List<String>.tilt(direction: TiltDirection): List<String> {
        val grid = when (direction) {
            TiltDirection.NORTH, TiltDirection.SOUTH -> this.transpose()
            else -> this
        }

        val tiltedGrid = grid.map { column ->
            column.split("#").joinToString("#") { section ->
                val length = section.length
                val roundRocks = section.count { it == 'O' }
                val rocks = "O".repeat(roundRocks)
                val empty = ".".repeat(length - roundRocks)
                when (direction) {
                    TiltDirection.NORTH, TiltDirection.WEST -> rocks + empty
                    else -> empty + rocks
                }
            }
        }

        return when (direction) {
            TiltDirection.NORTH, TiltDirection.SOUTH -> tiltedGrid.transpose()
            else -> tiltedGrid
        }
    }*/

    fun List<String>.tilt(direction: TiltDirection): List<String> {
        val rows = indices
        val cols = this[0].indices
        val new = Array(rows.count()) { CharArray(cols.count()) { '.' } }

        when (direction) {
            TiltDirection.NORTH -> {
                for (j in cols) {
                    var index = 0
                    for (i in rows) {
                        if (this[i][j] == 'O') {
                            new[index++][j] = 'O'
                        } else if (this[i][j] == '#') {
                            new[i][j] = '#'
                            index = i + 1
                        }
                    }
                }
            }

            TiltDirection.WEST -> {
                for (i in rows) {
                    var index = 0
                    for (j in cols) {
                        if (this[i][j] == 'O') {
                            new[i][index++] = 'O'
                        } else if (this[i][j] == '#') {
                            new[i][j] = '#'
                            index = j + 1
                        }
                    }
                }
            }

            TiltDirection.SOUTH -> {
                for (j in cols) {
                    var index = rows.last
                    for (i in rows.reversed()) {
                        if (this[i][j] == 'O') {
                            new[index--][j] = 'O'
                        } else if (this[i][j] == '#') {
                            new[i][j] = '#'
                            index = i - 1
                        }
                    }
                }
            }

            TiltDirection.EAST -> {
                for (i in rows) {
                    var index = cols.last
                    for (j in cols.reversed()) {
                        if (this[i][j] == 'O') {
                            new[i][index--] = 'O'
                        } else if (this[i][j] == '#') {
                            new[i][j] = '#'
                            index = j - 1
                        }
                    }
                }
            }
        }
        return new.map { it.joinToString("") }
    }

    fun List<String>.calculateLoad() = withIndex().sumOf { (i, row) ->
        row.count { it == 'O' } * (size - i)
    }

    fun part1(input: List<String>): Int {
        return input.tilt(TiltDirection.NORTH).calculateLoad()
    }

    fun List<String>.cycle() = tilt(TiltDirection.NORTH)
        .tilt(TiltDirection.WEST)
        .tilt(TiltDirection.SOUTH)
        .tilt(TiltDirection.EAST)

    // https://github.com/eagely/adventofcode/blob/main/src/main/kotlin/solutions/y2023/Day14.kt

    fun part2(input: List<String>): Int {
        var grid = input
        val seen = mutableMapOf<List<String>, Int>()
        val total = 1_000_000_000
        var cycle = 0

        while (cycle < total) {
            if (grid in seen) {
                break
            }
            seen[grid] = cycle

            grid = grid.cycle()
            cycle++
        }

        val cyclePeriod = cycle - seen[grid]!!
        if (cyclePeriod > 0) {
            val remainingCycles = (total - cycle) % cyclePeriod
            repeat(remainingCycles) {
                grid = grid.cycle()
            }
        }

        return grid.calculateLoad()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
    check(part1(testInput) == 136)
    check(part2(testInput) == 64)

    val input = readInput("Day14")
    part1(input).println()
    part2(input).println()
}
