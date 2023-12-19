private enum class HandType {
    FIVE_OF_A_KIND, FOUR_OF_A_KIND, FULL_HOUSE, THREE_OF_A_KIND, TWO_PAIR, ONE_PAIR, HIGH_CARD
}

fun main() {
    val JOKER = 'J'
    val cardsStrength = listOf('A', 'K', 'Q', JOKER, 'T', '9', '8', '7', '6', '5', '4', '3', '2')
    val cardsStrengthWildJokers = listOf('A', 'K', 'Q', 'T', '9', '8', '7', '6', '5', '4', '3', '2', JOKER)

    data class Hand(
        val cards: String,
        val bid: Int
    ) {
        init {
            require(cards.length == 5) { "A hand can only have 5 cards ($cards)" }
        }
    }

    fun String.eachCount(): Map<Char, Int> = groupingBy { it }.eachCount()

    fun getHandType(hand: String, jokersWild: Boolean = false): HandType {
        val jokerCount = hand.count { it == JOKER }
        val distinct = if (jokersWild) {
            hand.filterNot { it == JOKER }.eachCount().count()
        } else {
            hand.toHashSet().count()
        }
        val otherCount = hand.filterNot { it == JOKER }.eachCount()
        val counts = if (jokersWild && jokerCount > 0 && otherCount.isNotEmpty()) {
            val otherCountMutable = otherCount.toMutableMap()
            val maxKey = otherCountMutable.maxByOrNull { it.value }!!.key
            otherCountMutable[maxKey] = otherCountMutable[maxKey]!! + jokerCount
            otherCountMutable.toMap()
        } else {
            hand.eachCount()
        }
        val result = when {
            counts.any { it.value == 5 } -> HandType.FIVE_OF_A_KIND
            counts.any { it.value == 4 } -> HandType.FOUR_OF_A_KIND
            counts.any { it.value == 3 } && distinct == 2 -> HandType.FULL_HOUSE
            counts.any { it.value == 3 } -> HandType.THREE_OF_A_KIND
            counts.any { it.value == 2 } && distinct == 3 -> HandType.TWO_PAIR
            distinct == 4 -> HandType.ONE_PAIR
            else -> HandType.HIGH_CARD
        }
        return result
    }

    fun compareCardStrength(card1: Char, card2: Char, jokersWild: Boolean = false): Int {
        val strenghtList = if (jokersWild) cardsStrengthWildJokers else cardsStrength
        return (strenghtList.indexOf(card2) - strenghtList.indexOf(card1)).coerceIn(-1..1)
    }

    fun handComparator(jokersWild: Boolean = false) = Comparator<Hand> { hand1, hand2 ->
        val handType = (
                getHandType(hand2.cards, jokersWild).ordinal -
                        getHandType(hand1.cards, jokersWild).ordinal
                ).coerceIn(-1..1)
        if (handType != 0) handType
        else hand1.cards.zip(hand2.cards)
            .firstOrNull { (c1, c2) -> c1 != c2 }
            ?.let { (c1, c2) -> compareCardStrength(c1, c2, jokersWild) }
            ?: 0
    }

    fun List<String>.parseInput() = map { line ->
        val (hand, bid) = line.split(" ")
        Hand(hand, bid.toInt())
    }

    // TODO Kotlin context

    fun List<Hand>.calculateWinnings(comparator: Comparator<Hand>) = sortedWith(comparator)
        .mapIndexed { index, hand ->
            (index + 1) * hand.bid
        }.sum()

    fun part1(input: List<Hand>): Int {
        return input.calculateWinnings(handComparator())
    }

    fun part2(input: List<Hand>): Int {
        return input.calculateWinnings(handComparator(true))
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test").parseInput()
    check(part1(testInput) == 6440)
    check(part2(testInput) == 5905)

    val input = readInput("Day07").parseInput()
    part1(input).println() // 248836197
    part2(input).println() // 251195607
}
