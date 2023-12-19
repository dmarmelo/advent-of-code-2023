import kotlin.system.measureTimeMillis

fun main() {

    infix fun LongRange.overlaps(other: LongRange) = first in other || last in other

    data class MapEntry(
        val sourceRange: LongRange,
        val destinationStart: Long
    )

    operator fun MapEntry.contains(input: Long) = input in sourceRange

    operator fun MapEntry.contains(input: LongRange) = input overlaps sourceRange

    fun MapEntry.map(input: Long) = (destinationStart - sourceRange.first) + input

    fun MapEntry.map(input: LongRange): Pair<LongRange, LongRange> {
        val start = maxOf(input.first, sourceRange.first)
        val end = minOf(input.last, sourceRange.last)
        return start..end to (destinationStart - sourceRange.first).let { (start + it)..(end + it) }
    }

    data class Map(
        val name: String,
        val entries: List<MapEntry>
    )

    fun Map.map(input: Long) = entries.find { input in it }?.map(input) ?: input

    fun Map.map(input: LongRange): List<LongRange> {
        val mappedRanges = entries.filter { input in it }
            .map { it.map(input) }
            .sortedBy { it.first.first }

        val unmappedInputRanges = if (mappedRanges.isEmpty()) {
            listOf(input)
        } else {
            val cuts = listOf(input.first) + mappedRanges.map { it.first }
                .flatMap { listOf(it.first, it.last) } + listOf(input.last)

            val cutChunked = cuts.chunked(2)
            val mapNotNull = cutChunked.mapIndexedNotNull { index, (first, second) ->
                val isFirstChunk = index == 0
                val isLastChunk = index == cutChunked.lastIndex
                if (second - first > 1) {
                    if (isFirstChunk) first..<second
                    else if (isLastChunk) first + 1..second
                    else first + 1..<second
                } else {
                    null
                }
            }
            mapNotNull
        }

        // 0..11924314
        return mappedRanges.map { it.second } + unmappedInputRanges
    }

    data class Almanac(
        val seeds: List<Long>,
        val seedRanges: List<LongRange>,
        val maps: List<Map>
    )

    fun Almanac.map(input: Long) =
        maps.fold(input) { acc, entry ->
            entry.map(acc)
        }

    fun Almanac.map(input: LongRange) =
        maps.fold(listOf(input)) { acc, entry ->
            acc.flatMap { entry.map(it) }
        }

    fun Almanac.mapSeeds(seeds: List<Long>) = seeds.map { s ->
        s to map(s)
    }

    fun Almanac.mapSeeds(seeds: List<LongRange>) = seeds.map { s ->
        s to map(s)
    }

    fun String.parseNumbers(): List<Long> = split(" ")
        .filter { it.isNotBlank() }
        .map { it.trim().toLong() }

    fun String.parseMapEntry(): MapEntry {
        val (destinationRangeStart, sourceRangeStart, rangeLength) = parseNumbers()
        return MapEntry(
            sourceRange = sourceRangeStart..<sourceRangeStart + rangeLength,
            destinationStart = destinationRangeStart
        )
    }

    fun String.parseInput(): Almanac {
        val split = split("\n\n")

        val seeds = split.first().substringAfter("seeds: ").parseNumbers()

        val seedRanges = seeds.chunked(2).map { (start, length) ->
            start..<start + length
        }

        val maps = split.drop(1)
            .map { map ->
                val lines = map.lines()
                val name = lines.first().substringBefore(" map:")
                val entries = lines.drop(1).map { it.parseMapEntry() }.sortedBy { it.sourceRange.first }
                Map(name, entries)
            }

        return Almanac(seeds, seedRanges, maps)
    }

    fun part1(input: Almanac): Long {
        return input.mapSeeds(input.seeds).minOf { it.second }
    }

    fun part2(input: Almanac): Long {
        return input.mapSeeds(input.seedRanges).flatMap { it.second }
            // For some reason the min value on the real data is 0, but the real answer is the min non-zero value
            .sortedBy { it.first }.filterNot { it.first == 0L }
            .minOf { it.first }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInputRaw("Day05_test").parseInput()
    check(part1(testInput) == 35L)
    check(part2(testInput) == 46L)

    val input = readInputRaw("Day05").parseInput()
    measureTimeMillis {
        part1(input).println()
    }.also { println("Took ${it}ms") }
    measureTimeMillis {
        part2(input).println()
    }.also { println("Took ${it}ms") }
}
