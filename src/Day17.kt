import java.util.*
import kotlin.system.measureTimeMillis

fun main() {

    fun List<String>.parseInput() = map { line ->
        line.map { it.digitToInt() }
    }

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

    data class Node(
        val position: Point2D,
        val direction: Direction,
        val directionStepsCount: Int = 1
    )

    fun Node.possibleNextDirections() = Direction.entries.filterNot { it == direction.opposite }

    fun Node.move(to: Direction = direction) = copy(
        position = position.move(to),
        direction = to,
        directionStepsCount = if (direction == to) directionStepsCount + 1 else 1
    )

    fun Node.movePossible() = possibleNextDirections().map { move(it) }

    // https://github.com/Zordid/adventofcode-kotlin-2023/blob/main/src/main/kotlin/utils/GraphSearch.kt
    // https://github.com/Zordid/adventofcode-kotlin-2023/blob/main/src/main/kotlin/utils/MinPriorityQueue.kt

    fun <T> dijkstra(
        start: T,
        isGoal: (T) -> Boolean,
        getNeignbors: (T) -> Iterable<T>,
        canMove: (from: T, to: T) -> Boolean = { _, _ -> true },
        calculateCost: (T) -> Int = { _ -> 1 }
    ): Int {
        val dist = mutableMapOf(start to 0)
        //val prev = mutableMapOf<T, T>()
        val queue = PriorityQueue<IndexedValue<T>> { n1, n2 -> n1.index - n2.index }
        queue.add(IndexedValue(dist[start]!!, start))
        while (queue.isNotEmpty()) {
            val (cost, item) = queue.poll()
            if (isGoal(item)) {
                return cost
            }
            getNeignbors(item)
                .filter { canMove(item, it) }
                .forEach {
                    val oldCost = dist[it] ?: Int.MAX_VALUE
                    val newCost = cost + calculateCost(it)
                    if (newCost < oldCost) {
                        dist[it] = newCost
                        //prev[it] = item
                        queue.remove(IndexedValue(oldCost, it))
                        queue.add(IndexedValue(newCost, it))
                    }
                }
        }
        return -1
    }

    fun part1(input: List<List<Int>>): Int {
        val finish = Point2D(row = input.lastIndex, column = input.first().lastIndex)
        return dijkstra(
            start = Node(position = Point2D(0, 0), direction = Direction.RIGHT),
            isGoal = {
                it.position == finish
            },
            getNeignbors = { from ->
                from.movePossible()
                    .filter {
                        it.position.row in 0..input.lastIndex &&
                                it.position.column in 0..input.first().lastIndex
                    }
            },
            canMove = { _, to ->
                to.directionStepsCount <= 3
            },
            calculateCost = { input[it.position.row][it.position.column] }
        )
    }

    fun part2(input: List<List<Int>>): Int {
        val finish = Point2D(row = input.lastIndex, column = input.first().lastIndex)
        return dijkstra(
            start = Node(position = Point2D(0, 0), direction = Direction.RIGHT),
            isGoal = {
                it.position == finish && it.directionStepsCount >= 4
            },
            getNeignbors = { from ->
                val possibleMoves = if (from.directionStepsCount < 4) listOf(from.move())
                else from.movePossible()
                possibleMoves.filter {
                    it.position.row in 0..input.lastIndex &&
                            it.position.column in 0..input.first().lastIndex
                }
            },
            canMove = { _, to ->
                to.directionStepsCount <= 10
            },
            calculateCost = { input[it.position.row][it.position.column] }
        )
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test").parseInput()
    check(part1(testInput) == 102)
    check(part2(testInput) == 94)
    val testInput2 = readInput("Day17_test2").parseInput()
    check(part2(testInput2) == 71)

    val input = readInput("Day17").parseInput()
    measureTimeMillis {
        part1(input).println()
    }.also { println("Took ${it}ms") }
    measureTimeMillis {
        part2(input).println()
    }.also { println("Took ${it}ms") }
}
