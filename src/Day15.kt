fun main() {

    fun String.parseInput() = split(",")

    fun String.holidayHash() = fold(0) { acc, char ->
        ((acc + char.code) * 17) % 256
    }

    fun part1(input: List<String>): Int {
        return input.sumOf { it.holidayHash() }
    }

    fun part2(input: List<String>): Int {
        val boxes = Array<MutableMap<String, Int>>(256) { mutableMapOf() }
        for (instruction in input) {
            if (instruction.endsWith('-')) {
                val label = instruction.substringBefore('-')
                boxes[label.holidayHash()].remove(label)
            } else {
                val label = instruction.substringBefore('=')
                boxes[label.holidayHash()][label] = instruction.substringAfter('=').toInt()
            }
        }
        return boxes.flatMapIndexed { boxIndex, box ->
            box.values.mapIndexed { lensIndex, lens ->
                (1 + boxIndex) * (lensIndex + 1) * lens
            }
        }.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInputRaw("Day15_test").parseInput()
    check(part1(testInput) == 1320)
    check(part2(testInput) == 145)

    val input = readInputRaw("Day15").parseInput()
    part1(input).println()
    part2(input).println()
}
