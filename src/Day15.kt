fun main() {

    fun String.parseInput() = split(",")

    fun String.holidayHash() = fold(0) { acc, char ->
        ((acc + char.code) * 17) % 256
    }

    fun part1(input: List<String>): Int {
        return input.sumOf { it.holidayHash() }
    }

    data class Lens(
        val label: String,
        val focalLength: Int
    )

    fun part2(input: List<String>): Int {
        val boxes = Array<MutableList<Lens>>(256) { mutableListOf() }
        for (step in input) {
            val split = step.split('=', '-').filterNot { it.isBlank() }
            val label = split.first()
            val boxIndex = label.holidayHash()
            val box = boxes[boxIndex]
            val indexOfLens = box.indexOfFirst { it.label == label }
            val containsLens = indexOfLens >= 0
            if (containsLens && split.size == 2) {
                box[indexOfLens] = Lens(label, split.last().toInt())
            } else if (!containsLens && split.size == 2) {
                box += Lens(label, split.last().toInt())
            } else if (containsLens) {
                box.removeAt(indexOfLens)
            }
        }
        return boxes.flatMapIndexed { boxIndex, box ->
            box.mapIndexed { lensIndex, lens ->
                (1 + boxIndex) * (lensIndex + 1) * lens.focalLength
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
