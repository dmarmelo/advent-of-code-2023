fun main() {
    data class CubeSet(
        val red: Int,
        val green: Int,
        val blue: Int
    )

    data class Game(
        val id: Int,
        val rounds: List<CubeSet>
    ) {
        fun isPossible(availableCubes: CubeSet) = rounds.all {
            it.red <= availableCubes.red && it.green <= availableCubes.green && it.blue <= availableCubes.blue
        }

        val minimunSetOfCubes = CubeSet(
            red = rounds.maxOf { it.red },
            green = rounds.maxOf { it.green },
            blue = rounds.maxOf { it.blue }
        )

        val power = minimunSetOfCubes.let { it.red * it.green * it.blue }
    }

    fun String.parseCubeSet(): CubeSet {
        val cubes = this.split(", ").associate {
            val (number, color) = it.split(" ")
            color to number.toInt()
        }
        return CubeSet(
            red = cubes["red"] ?: 0,
            green = cubes["green"] ?: 0,
            blue = cubes["blue"] ?: 0,
        )
    }

    fun String.parseRounds(): List<CubeSet> {
        return this.split("; ").map { it.parseCubeSet() }
    }

    fun String.parseGame(): Game {
        val (game, rest) = this.split(": ")
        return Game(
            id = game.substringAfter("Game ").toInt(),
            rounds = rest.parseRounds()
        )
    }

    fun List<String>.parseInput() = map { it.parseGame() }

    fun part1(input: List<Game>): Int {
        val cubesInBag = CubeSet(red = 12, green = 13, blue = 14)
        return input.filter { it.isPossible(cubesInBag) }.sumOf { it.id }
    }

    fun part2(input: List<Game>): Int {
        return input.sumOf { it.power }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test").parseInput()
    check(part1(testInput) == 8)
    check(part2(testInput) == 2286)

    val input = readInput("Day02").parseInput()
    part1(input).println()
    part2(input).println()
}
