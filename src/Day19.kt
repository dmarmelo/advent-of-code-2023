import kotlin.system.measureTimeMillis

private enum class RatingCategory {
    X, M, A, S
}

private data class Part<T>(
    val ratings: Map<RatingCategory, T>
) {
    operator fun get(rating: RatingCategory) = ratings[rating]!!
    fun copy(category: RatingCategory, value: T) = Part(ratings.toMutableMap().apply { set(category, value) })
}

private data class Workflow(
    val name: String,
    val rules: List<Rule>
)

private sealed interface Rule {
    val next: RuleResult
    operator fun invoke(part: Part<Int>): RuleResult
    operator fun invoke(part: Part<IntRange>): Map<Part<IntRange>, RuleResult>

    data class LessThan(
        val category: RatingCategory,
        val value: Int,
        override val next: RuleResult
    ) : Rule {
        override fun invoke(part: Part<Int>) =
            if (part[category] < value) next else RuleResult.None

        override fun invoke(part: Part<IntRange>): Map<Part<IntRange>, RuleResult> {
            val relevant = part[category]
            return when {
                value in relevant -> mapOf(
                    part.copy(category, (relevant.first..<value)) to next,
                    part.copy(category, (value..relevant.last)) to RuleResult.None
                )

                value > relevant.first -> mapOf(
                    part.copy(category, relevant) to next
                )

                else -> mapOf(
                    part.copy(category, relevant) to RuleResult.None
                )
            }
        }
    }

    data class GreaterThan(
        val category: RatingCategory,
        val value: Int,
        override val next: RuleResult
    ) : Rule {
        override fun invoke(part: Part<Int>) =
            if (part[category] > value) next else RuleResult.None

        override fun invoke(part: Part<IntRange>): Map<Part<IntRange>, RuleResult> {
            val relevant = part[category]
            return when {
                value in relevant -> mapOf(
                    part.copy(category, ((value + 1)..relevant.last)) to next,
                    part.copy(category, (relevant.first..value)) to RuleResult.None
                )

                value < relevant.first -> mapOf(
                    part.copy(category, relevant) to next
                )

                else -> mapOf(
                    part.copy(category, relevant) to RuleResult.None
                )
            }
        }
    }

    data class Unconditional(
        override val next: RuleResult
    ) : Rule {
        override fun invoke(part: Part<Int>) = next

        override fun invoke(part: Part<IntRange>) = mapOf(part to next)
    }
}

private sealed interface RuleResult {
    class NextWorkflow(val code: String) : RuleResult
    data object Accepted : RuleResult
    data object Rejected : RuleResult
    data object None : RuleResult
}


fun main() {

    fun <K, V> List<Map<K, V>>.join(): Map<K, V> {
        return if (isEmpty()) emptyMap()
        else reduce { a, b -> a + b }
    }

    fun String.toRatingCategory() = RatingCategory.valueOf(uppercase())
    fun Char.toRatingCategory() = toString().toRatingCategory()

    fun String.toRuleResult() = when (this) {
        "A" -> RuleResult.Accepted
        "R" -> RuleResult.Rejected
        else -> RuleResult.NextWorkflow(this)
    }

    data class Input(
        val workflows: List<Workflow>,
        val parts: List<Part<Int>>
    )

    fun String.toWorkflow(): Workflow {
        val name = substringBefore('{')
        val rules = substringAfter('{').dropLast(1).split(',')
            .map { rule ->
                if (':' in rule) {
                    val (condition, next) = rule.split(':')
                    when (condition[1]) {
                        '>' -> Rule.GreaterThan(
                            condition[0].toRatingCategory(),
                            condition.substring(2).toInt(),
                            next.toRuleResult()
                        )

                        '<' -> Rule.LessThan(
                            condition[0].toRatingCategory(),
                            condition.substring(2).toInt(),
                            next.toRuleResult()
                        )

                        else -> error("Unknown condition comparator: ${condition[1]} in rule: $rule")
                    }
                } else {
                    Rule.Unconditional(rule.toRuleResult())
                }
            }

        return Workflow(name, rules)
    }

    fun String.toPart(): Part<Int> {
        val ratings = drop(1).dropLast(1).split(',')
            .associate {
                val (rating, value) = it.split('=')
                rating.toRatingCategory() to value.toInt()
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

    fun Workflow.run(part: Part<Int>) =
        rules.first { it(part) !is RuleResult.None }.next

    fun Workflow.run(part: Part<IntRange>): Map<Part<IntRange>, RuleResult> {
        val result = mutableMapOf<Part<IntRange>, RuleResult>()
        var remaining = setOf(part)
        for (rule in rules) {
            if (remaining.isEmpty()) break
            val map = remaining.map { rule(it) }.join()
            result += map.filter { it.value !is RuleResult.None }
            remaining = map.filter { it.value is RuleResult.None }.keys
        }
        return result
    }

    fun List<Workflow>.run(part: Part<Int>): RuleResult {
        val workflowMap = associateBy { it.name }
        var next = "in"
        while (true) {
            when (val result = workflowMap[next]!!.run(part)) {
                RuleResult.Accepted -> return RuleResult.Accepted
                RuleResult.Rejected -> return RuleResult.Rejected
                is RuleResult.NextWorkflow -> next = result.code
                RuleResult.None -> continue
            }
        }
    }

    fun List<Workflow>.run(part: Part<IntRange>): Map<Part<IntRange>, RuleResult> {
        val workflowMap = associateBy { it.name }
        val result = mutableMapOf<Part<IntRange>, RuleResult>()
        var remaining = mapOf(part to "in")
        while (remaining.isNotEmpty()) {
            val map = remaining.map { (part, next) ->
                workflowMap[next]!!.run(part)
            }.join()
            result += map.filter { it.value !is RuleResult.NextWorkflow }
            remaining = map.filter { it.value is RuleResult.NextWorkflow }
                .map { it.key to (it.value as RuleResult.NextWorkflow).code }
                .toMap()
        }
        return result
    }

    fun part1(input: Input): Int {
        return input.parts
            .associateWith { input.workflows.run(it) }
            .filter { it.value is RuleResult.Accepted }
            .keys.sumOf { it.ratings.values.sum() }
    }

    fun part2(input: Input): Long {
        val ratingRange = 1..4000
        val startPart = Part(
            mapOf(
                RatingCategory.X to ratingRange,
                RatingCategory.M to ratingRange,
                RatingCategory.A to ratingRange,
                RatingCategory.S to ratingRange
            )
        )
        return input.workflows.run(startPart)
            .filter { it.value is RuleResult.Accepted }
            .keys.sumOf { part ->
                part.ratings.map { it.value.count().toLong() }.product()
            }
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
