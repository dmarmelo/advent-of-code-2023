import kotlin.system.measureTimeMillis

fun IntRange.split(value: Int) = listOf(first..<value, value + 1..last)

private enum class PartRating {
    X, M, A, S
}

private data class Part<T>(
    val ratings: Map<PartRating, T>
) {
    operator fun get(rating: PartRating) = ratings[rating]!!
}

private sealed class ConditionResult
private class NextWorkflow(val code: String) : ConditionResult()
private data object NoResult : ConditionResult()
private data object Accepted : ConditionResult()
private data object Rejected : ConditionResult()

private enum class ConditionComparator {
    LESS_THAN, GREATER_THAN
}

private sealed class Contition {
    abstract operator fun invoke(part: Part<Int>): ConditionResult
    abstract operator fun invoke(part: Part<IntRange>): Map<Part<IntRange>, ConditionResult>
}

private data class ParameterizedContition(
    val rating: PartRating,
    val comparator: ConditionComparator,
    val value: Int,
    val conditionResult: ConditionResult
) : Contition() {
    override fun invoke(part: Part<Int>) = when (comparator) {
        ConditionComparator.LESS_THAN -> if (part[rating] < value) conditionResult else NoResult
        ConditionComparator.GREATER_THAN -> if (part[rating] > value) conditionResult else NoResult
    }

    override fun invoke(part: Part<IntRange>): Map<Part<IntRange>, ConditionResult> {
        val partRating = part[rating]
        return if (value in partRating) {
            val noResultPart = Part(part.ratings.toMutableMap().apply { set(rating, value..value) }) to NoResult

            partRating.split(value)
                .map { range ->
                    Part(part.ratings.toMutableMap().apply { set(rating, range) })
                }
                .map { this(it) }
                .reduce { a, b -> a + b } + noResultPart
        } else {
            mapOf(
                part to when (comparator) {
                    ConditionComparator.LESS_THAN -> if (partRating.last < value) conditionResult else NoResult
                    ConditionComparator.GREATER_THAN -> if (partRating.first > value) conditionResult else NoResult
                }
            )
        }
    }
}

private data class ValueContition(
    val value: ConditionResult
) : Contition() {
    override fun invoke(part: Part<Int>) = value
    override fun invoke(part: Part<IntRange>) = mapOf(part to value)
}

private data class Workflow(
    val code: String,
    val conditions: List<Contition>
)

fun main() {

    fun String.toPartRating() = PartRating.valueOf(uppercase())
    fun Char.toPartRating() = toString().toPartRating()

    fun Char.toConditionComparator() = when (this) {
        '<' -> ConditionComparator.LESS_THAN
        '>' -> ConditionComparator.GREATER_THAN
        else -> error("Unknown ConditionComparator: $this")
    }

    fun String.toResult() = when (this) {
        "A" -> Accepted
        "R" -> Rejected
        else -> NextWorkflow(this)
    }

    fun Char.toResult() = toString().toResult()

    data class Input(
        val workflows: List<Workflow>,
        val parts: List<Part<Int>>
    )

    fun String.toWorkflow(): Workflow {
        val code = substringBefore('{')
        val conditions = substringAfter('{').dropLast(1).split(',')
            .map { conditionBlock ->
                val parts = conditionBlock.split(':')
                if (parts.size == 1) {
                    ValueContition(parts.first().toResult())
                } else {
                    val condition = parts[0]
                    ParameterizedContition(
                        condition[0].toPartRating(),
                        condition[1].toConditionComparator(),
                        condition.substring(2).toInt(),
                        parts[1].toResult()
                    )
                }
            }

        return Workflow(code, conditions)
    }

    fun String.toPart(): Part<Int> {
        val ratings = drop(1).dropLast(1).split(',')
            .associate {
                val (rating, value) = it.split('=')
                rating.toPartRating() to value.toInt()
            }
        return Part(ratings)
    }

    fun List<String>.parseInput(): Input {
        val (workflows, parts) = fold(mutableListOf(mutableListOf<String>())) { acc, line ->
            if (line.isBlank()) {
                acc.add(mutableListOf())
            } else {
                acc.last().add(line)
            }
            acc
        }
        return Input(
            workflows = workflows.map { it.toWorkflow() },
            parts = parts.map { it.toPart() }
        )
    }

    fun Workflow.run(part: Part<Int>): ConditionResult {
        for (condition in conditions) {
            val result = condition(part)
            if (result !is NoResult) {
                return result
            }
        }
        return NoResult
    }

    fun Workflow.run(part: Part<IntRange>): Map<Part<IntRange>, ConditionResult> {
        val result = mutableMapOf<Part<IntRange>, ConditionResult>()
        var queue = setOf(part)
        for (condition in conditions) {
            if (queue.isEmpty()) break
            val map = queue.map { condition(it) }
            val reduced = map.reduce { a, b -> a + b }
            result += reduced.filter { it.value !is NoResult }
            queue = reduced.filter { it.value is NoResult }.keys
        }
        return result
    }

    fun List<Workflow>.run(part: Part<Int>): ConditionResult {
        val workflowMap = associateBy { it.code }
        var next = workflowMap["in"]!!
        while (true) {
            when (val result = next.run(part)) {
                Accepted -> return Accepted
                Rejected -> return Rejected
                is NextWorkflow -> next = workflowMap[result.code]!!
                NoResult -> continue
            }
        }
    }

    fun List<Workflow>.run(part: Part<IntRange>): Map<Part<IntRange>, ConditionResult> {
        val workflowMap = associateBy { it.code }

        val result = mutableMapOf<Part<IntRange>, ConditionResult>()
        var queue = mapOf(part to NextWorkflow("in"))
        while (queue.isNotEmpty()) {
            val map = queue.map { (part, next) ->
                workflowMap[next.code]!!.run(part)
            }
            val reduce = map.reduce { a, b -> a + b }
            result += reduce.filter { it.value !is NextWorkflow }
            @Suppress("UNCHECKED_CAST")
            queue = reduce.filter { it.value is NextWorkflow } as Map<Part<IntRange>, NextWorkflow>
        }
        return result
    }

    fun part1(input: Input): Int {
        return input.parts
            .associateWith { input.workflows.run(it) }
            .filter { it.value is Accepted }
            .keys.sumOf { it.ratings.values.sum() }
    }

    fun part2(input: Input): Long {
        val ratingRange = 1..4000
        val startPart = Part(
            mapOf(
                PartRating.X to ratingRange,
                PartRating.M to ratingRange,
                PartRating.A to ratingRange,
                PartRating.S to ratingRange
            )
        )
        val run = input.workflows.run(startPart)

        val keys = run.filter { it.value is Accepted }.keys.sumOf { part ->
            part.ratings.map {
                it.value.count().toLong()
            }.product()
        }

        return keys
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day19_test").parseInput()
    check(part1(testInput) == 19114)
    check(part2(testInput) == 167_409_079_868_000L)

    val input = readInput("Day19").parseInput()
    measureTimeMillis {
        part1(input).println()
    }.also { println("Took ${it}ms") }
    measureTimeMillis {
        part2(input).println()
    }.also { println("Took ${it}ms") }
}
