fun main() {

    data class Node(
        val value: String,
        val left: String,
        val right: String
    )

    data class Map(
        val instructions: List<Direction>,
        val nodes: List<Node>
    )

    fun <T> Iterable<T>.asInfiniteSequence() = sequence<T> {
        while (true) {
            yieldAll(this@asInfiniteSequence)
        }
    }

    fun List<String>.parseInput(): Map {
        val instructions = first().map { if (it == 'L') Direction.LEFT else Direction.RIGHT }
        val nodes = drop(2).map {
            val (node, left, right) = it.split(" = (", ", ", ")")
            Node(node, left, right)
        }
        return Map(instructions, nodes)
    }

    fun part1(input: Map): Int {
        var stepsCount = 0
        var currentNode = input.nodes.first { it.value == "AAA" }
        for (instruction in input.instructions.asInfiniteSequence()) {
            stepsCount++
            val next = when (instruction) {
                Direction.LEFT -> currentNode.left
                Direction.RIGHT -> currentNode.right
                else -> error("Unknown instruction in context: $instruction")
            }
            if (next == "ZZZ") {
                break
            }
            currentNode = input.nodes.first { it.value == next }
        }
        return stepsCount
    }

    fun distanceTofirstEndingNodeEndingWithZ(map: Map, start: String): Long {
        var stepsCount = 0L
        var currentNode = map.nodes.first { it.value == start }
        for (instruction in map.instructions.asInfiniteSequence()) {
            stepsCount++
            val next = when (instruction) {
                Direction.LEFT -> currentNode.left
                Direction.RIGHT -> currentNode.right
                else -> error("Unknown instruction in context: $instruction")
            }
            if (next.endsWith('Z')) {
                break
            }
            currentNode = map.nodes.first { it.value == next }
        }
        return stepsCount
    }

    // https://www.baeldung.com/kotlin/lcm
    // Least Common Multiple
    fun findLCM(a: Long, b: Long): Long {
        val larger = if (a > b) a else b
        val maxLcm = a * b
        var lcm = larger
        while (lcm <= maxLcm) {
            if (lcm % a == 0L && lcm % b == 0L) {
                return lcm
            }
            lcm += larger
        }
        return maxLcm
    }

    fun findLCMOfListOfNumbers(numbers: List<Long>): Long {
        var result = numbers[0]
        for (i in 1 until numbers.size) {
            result = findLCM(result, numbers[i])
        }
        return result
    }

    fun part2(input: Map): Long {
        val currentNodes = input.nodes.filter { it.value.endsWith('A') }
        val minDistances = currentNodes.map { distanceTofirstEndingNodeEndingWithZ(input, it.value) }
        return findLCMOfListOfNumbers(minDistances)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test").parseInput()
    check(part1(testInput) == 2)

    val testInput2 = readInput("Day08_test2").parseInput()
    check(part1(testInput2) == 6)

    val testInput3 = readInput("Day08_test3").parseInput()
    check(part2(testInput3) == 6L)

    val input = readInput("Day08").parseInput()
    part1(input).println()
    part2(input).println()
}
