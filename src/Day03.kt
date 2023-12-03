fun main() {

    fun IntRange.expand(n: Int) = first - n..last + n

    data class Symbol(
        val row: Int,
        val column: Int,
        val char: Char
    )

    fun Char.isSymbol() = this != '.' && !isDigit()

    fun Symbol.isPossibleGear() = char == '*'

    data class Number(
        val row: Int,
        val columnRange: IntRange,
        val value: Int
    ) {
        val expandedRows = row - 1..row + 1
        val expandedColumns = columnRange.expand(1)
    }

    infix fun Number.adjacent(symbol: Symbol) = symbol.row in expandedRows && symbol.column in expandedColumns

    data class EngineSchematic(
        val numbers: List<Number>,
        val symbols: List<Symbol>
    )

    fun String.findPotentialPartNumbers() =
        "(\\d+)".toRegex().findAll(this)
            .map { it.value.toInt() to it.range }

    fun List<String>.parseInput(): EngineSchematic {
        val numbers = flatMapIndexed { row: Int, line: String ->
            line.findPotentialPartNumbers()
                .map { Number(row, it.second, it.first) }
        }

        val symbols = flatMapIndexed { row: Int, line: String ->
            line.mapIndexedNotNull { column, char ->
                if (char.isSymbol()) Symbol(row, column, char)
                else null
            }
        }

        return EngineSchematic(
            numbers = numbers,
            symbols = symbols
        )
    }

    fun part1(input: EngineSchematic): Int {
        return input.numbers
            .filter { number ->
                input.symbols.any { number adjacent it }
            }.sumOf { it.value }
    }

    fun part2(input: EngineSchematic): Int {
        return input.symbols
            .filter { it.isPossibleGear() }
            .mapNotNull { gear ->
                input.numbers
                    .filter { it adjacent gear }
                    .map { it.value }
                    .let {
                        if (it.size == 2) it.product()
                        else null
                    }
            }.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test").parseInput()
    check(part1(testInput) == 4361)
    check(part2(testInput) == 467835)

    val input = readInput("Day03").parseInput()
    part1(input).println()
    part2(input).println()
}
