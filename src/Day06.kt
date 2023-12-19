import kotlin.math.*

fun main() {

    data class Race(
        val maxTime: Long,
        val distanceRecord: Long
    )

    fun String.parseNumbers() = split("\\s+".toRegex())
        .map { it.trim().toLong() }

    fun List<String>.parseInputPart1(): List<Race> {
        val times = first().substringAfter("Time:").trim().parseNumbers()
        val distances = last().substringAfter("Distance:").trim().parseNumbers()
        return times.zip(distances).map { Race(it.first, it.second) }
    }

    fun List<String>.parseInputPart2(): Race {
        val time = first().substringAfter("Time:").replace("\\s+".toRegex(), "").toLong()
        val distance = last().substringAfter("Distance:").replace("\\s+".toRegex(), "").toLong()
        return Race(time, distance)
    }

    fun quadraticSolve(a: Double, b: Double, c: Double = 0.0): Pair<Double, Double> {
        val delta = b.pow(2) - (4 * a * c)
        val sqrt = sqrt(delta)
        val s1 = (-b + sqrt) / 2 * a
        val s2 = (-b - sqrt) / 2 * a
        return if (s1 > s2) s2 to s1
        else s1 to s2
    }

    fun Race.solve(): Int {
        // (maxTime - holdTime) * holdTime > distanceRecord
        // -holdTime^2 + maxTime * holdTime - distanceRecord > 0
        val (s1, s2) = quadraticSolve(-1.0, maxTime.toDouble(), -distanceRecord.toDouble())
        val ceil = ceil(s1)
        val floor = floor(s2)
        val r1 = if (ceil == s1) ceil.roundToInt() + 1 else ceil.roundToInt()
        val r2 = if (floor == s2) floor.roundToInt() - 1 else floor.roundToInt()
        return r2 - r1 + 1
    }

    fun part1(input: List<Race>): Int {
        return input.map { it.solve() }.product()
    }

    fun part2(input: Race): Int {
        return input.solve()
    }

    // test if implementation meets criteria from the description, like:
    val testInputPart1 = readInput("Day06_test").parseInputPart1()
    val testInputPart2 = readInput("Day06_test").parseInputPart2()
    check(part1(testInputPart1) == 288)
    check(part2(testInputPart2) == 71503)

    val inputPart1 = readInput("Day06").parseInputPart1()
    val inputPart2 = readInput("Day06").parseInputPart2()
    part1(inputPart1).println()
    part2(inputPart2).println()
}
