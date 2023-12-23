import kotlin.math.max
import kotlin.system.measureTimeMillis

fun main() {

    data class Point3D(
        val x: Int,
        val y: Int,
        val z: Int
    )

    fun String.toPoint3D() = split(',').let { (x, y, z) ->
        Point3D(x.toInt(), y.toInt(), z.toInt())
    }

    data class Brick(
        val x: IntRange,
        val y: IntRange,
        val z: IntRange
    ) {
        val top = z.last
        val bottom = z.first
    }

    fun Brick(edge1: Point3D, edge2: Point3D) = Brick(
        x = edge1.x..edge2.x,
        y = edge1.y..edge2.y,
        z = edge1.z..edge2.z
    )

    infix fun IntRange.intersects(other: IntRange) = first <= other.last && last >= other.first

    infix fun Brick.intersectsXY(other: Brick) = x intersects other.x && y intersects other.y

    fun Brick.fall(fallLength: Int) = copy(z = (z.first - fallLength)..(z.last - fallLength))

    infix fun Brick.isBellow(other: Brick) = top == other.bottom - 1 && this intersectsXY other

    fun List<String>.parseInput() = map { line ->
        val (edge1, edge2) = line.split('~').map { it.toPoint3D() }
        Brick(edge1, edge2)
    }

    // https://www.reddit.com/r/adventofcode/comments/18o7014/comment/kefhah0/?utm_source=share&utm_medium=web3x&utm_name=web3xcss&utm_term=1&utm_content=share_button

    // Apply gravity to the bricks (fall)
    fun List<Brick>.settle(): List<Brick> {
        val bricks = sortedBy { it.bottom }.toMutableList()
        for (i in bricks.indices) {
            var newBottom = 1
            for (j in 0..<i) {
                if (bricks[i] intersectsXY bricks[j]) {
                    newBottom = max(newBottom, bricks[j].top + 1)
                }
            }
            val fall = bricks[i].bottom - newBottom
            if (fall > 0) {
                bricks[i] = bricks[i].fall(fall)
            }
        }
        return bricks.toList()
    }

    data class BrickSupports(
        val bricksAbove: Map<Brick, Set<Brick>>,
        val bricksBellow: Map<Brick, Set<Brick>>
    ) {
        val bricks by lazy { (bricksAbove.keys + bricksBellow.keys).toSet() }
    }

    fun BrickSupports.bellow(brick: Brick) = bricksBellow.getValue(brick)
    fun BrickSupports.above(brick: Brick) = bricksAbove.getValue(brick)

    fun List<Brick>.brickSupports(): BrickSupports {
        val bricksAbove = associateWith { mutableSetOf<Brick>() }
        val bricksBellow = associateWith { mutableSetOf<Brick>() }
        for ((i, brick) in withIndex()) {
            for (j in i + 1..<size) {
                if (brick isBellow this[j]) {
                    bricksAbove.getValue(brick) += this[j]
                    bricksBellow.getValue(this[j]) += brick
                }
            }
        }
        return BrickSupports(bricksAbove, bricksBellow)
    }

    fun BrickSupports.unremovable() =
        bricksBellow.filter { it.value.size < 2 }.flatMap { it.value }.toSet()

    fun BrickSupports.countFallingBricksByRemoving(brickToRemove: Brick): Int {
        val queue = mutableListOf(brickToRemove)
        val falling = mutableSetOf<Brick>()
        while (queue.isNotEmpty()) {
            val brick = queue.removeFirst()
            falling.add(brick)
            val fallingAbove = above(brick)
                .filter { brickAbove ->
                    bellow(brickAbove).all { it in falling }
                }.toSet()
            queue.addAll(fallingAbove)
        }
        return falling.size - 1
    }

    fun part1(input: List<Brick>): Int {
        val brickSupports = input.settle().brickSupports()
        return brickSupports.bricks.size - brickSupports.unremovable().size
    }

    fun part2(input: List<Brick>): Int {
        val brickSupports = input.settle().brickSupports()
        return brickSupports.bricks.fold(0) { acc, brick ->
            acc + brickSupports.countFallingBricksByRemoving(brick)
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day22_test").parseInput()
    check(part1(testInput) == 5)
    check(part2(testInput) == 7)

    val input = readInput("Day22").parseInput()
    measureTimeMillis {
        part1(input).println()
    }.also { println("Took ${it}ms") }
    measureTimeMillis {
        part2(input).println()
    }.also { println("Took ${it}ms") }
}
