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
        fun isPossibleFrom(cubesInBag: CubeSet) = rounds.all {
            it.red <= cubesInBag.red && it.green <= cubesInBag.green && it.blue <= cubesInBag.blue
        }

        fun minSetOfCubes() = CubeSet(
            red = rounds.maxOf { it.red },
            green = rounds.maxOf { it.green },
            blue = rounds.maxOf { it.blue }
        )
    }

    fun List<String>.parseInput() = map { line ->
        val (game, rest) = line.split(": ")
        val (_, gameId) = game.split(" ")
        val rounds = rest.split("; ").map { round ->
            val cubes = round.split(", ").associate {
                val (number, color) = it.split(" ")
                color to number.toInt()
            }
            CubeSet(
                red = cubes["red"] ?: 0,
                green = cubes["green"] ?: 0,
                blue = cubes["blue"] ?: 0,
            )
        }
        Game(id = gameId.toInt(), rounds = rounds)
    }

    fun part1(input: List<Game>): Int {
        val cubesInBag = CubeSet(red = 12, green = 13, blue = 14)
        return input.filter { it.isPossibleFrom(cubesInBag) }.sumOf { it.id }
    }

    fun part2(input: List<Game>): Int {
        return input.map { it.minSetOfCubes() }.sumOf { it.red * it.green * it.blue }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test").parseInput()
    check(part1(testInput) == 8)
    check(part2(testInput) == 2286)

    val input = readInput("Day02").parseInput()
    part1(input).println()
    part2(input).println()
}
