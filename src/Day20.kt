import kotlin.system.measureTimeMillis

private data class Pulse(
    val from: String,
    val to: String,
    val level: Boolean
)

private sealed interface Module {
    val name: String
    val destinations: List<String>

    fun pulse(pulse: Pulse): Boolean?
    fun reset()

    data class FlipFlop(
        override val name: String,
        override val destinations: List<String>
    ) : Module {
        var state: Boolean = false

        override fun pulse(pulse: Pulse) =
            if (!pulse.level) {
                state = !state
                state
            } else null

        override fun reset() {
            state = false
        }
    }

    data class Conjuction(
        override val name: String,
        override val destinations: List<String>,
        val inputModules: List<String>
    ) : Module {
        val state = mutableMapOf<String, Boolean>()

        init {
            reset()
        }

        override fun pulse(pulse: Pulse): Boolean {
            state[pulse.from] = pulse.level
            return state.values.all { it }.not()
        }

        override fun reset() {
            inputModules.forEach { state[it] = false }
        }
    }

    data class Broadcast(
        override val name: String,
        override val destinations: List<String>
    ) : Module {
        override fun pulse(pulse: Pulse) = pulse.level
        override fun reset() {
        }
    }
}

fun main() {

    fun List<String>.parseInput(): Map<String, Module> {
        val modules = map { module ->
            val (name, destinationsList) = module.split(" -> ")
            val destinations = destinationsList.split(", ")
            when {
                name == "broadcaster" -> Module.Broadcast(name, destinations)
                name.startsWith('%') -> Module.FlipFlop(name.drop(1), destinations)
                name.startsWith('&') -> Module.Conjuction(name.drop(1), destinations, emptyList())
                else -> error("Unknown module found: $module")
            }
        }
        val updatedConjunctions = modules.filterIsInstance<Module.Conjuction>().map { conjuction ->
            conjuction.copy(inputModules = modules.filter { conjuction.name in it.destinations }.map { it.name })
        }
        return (modules.filter { it !is Module.Conjuction } + updatedConjunctions).associateBy { it.name }
    }

    fun Map<String, Module>.reset() {
        values.forEach { it.reset() }
    }

    fun Map<String, Module>.pressButton(): List<Pulse> {
        val pulses = mutableListOf<Pulse>()
        val queue = ArrayDeque(listOf(Pulse("button", "broadcaster", false)))
        while (queue.isNotEmpty()) {
            val pulse = queue.removeFirst()
            pulses += pulse
            val module = this[pulse.to] ?: continue
            val nextPulses = module.pulse(pulse)?.let { level ->
                module.destinations.map { Pulse(module.name, it, level) }
            } ?: emptyList()
            queue.addAll(nextPulses)
        }
        return pulses
    }

    fun part1(input: Map<String, Module>): Int {
        input.reset()

        val pulseCounts = mutableMapOf(
            false to 0,
            true to 0
        )
        repeat(1000) {
            val signals = input.pressButton()
            signals.groupingBy { it.level }.eachCountTo(pulseCounts)
        }
        return pulseCounts[false]!! * pulseCounts[true]!!
    }

    // https://www.reddit.com/r/adventofcode/comments/18mqnrl/2023_day_20_click_click_click_click/
    // https://aoc.csokavar.hu/?day=20
    // https://dreampuf.github.io/GraphvizOnline/#digraph%20G%20%7B%0A%0A%20%20zq%20-%3E%20fd%2C%20gk%2C%20pp%2C%20ph%2C%20ss%2C%20dr%2C%20pl%3B%0A%20%20qg%20-%3E%20jh%2C%20nk%3B%0A%20%20lm%20-%3E%20lg%2C%20qm%3B%0A%20%20fk%20-%3E%20lr%3B%0A%20%20pp%20-%3E%20hh%3B%0A%20%20bf%20-%3E%20sj%2C%20qm%3B%0A%20%20qm%20-%3E%20kb%2C%20jl%2C%20bs%2C%20kx%2C%20bl%2C%20cz%2C%20dd%3B%0A%20%20db%20-%3E%20dc%2C%20jn%3B%0A%20%20kl%20-%3E%20dc%2C%20qv%3B%0A%20%20xm%20-%3E%20jh%3B%0A%20%20ss%20-%3E%20zq%2C%20nd%3B%0A%20%20vq%20-%3E%20bh%2C%20dc%3B%0A%20%20bl%20-%3E%20bs%3B%0A%20%20fd%20-%3E%20gk%3B%0A%20%20dc%20-%3E%20tx%2C%20vq%2C%20ct%2C%20df%2C%20fx%3B%0A%20%20dj%20-%3E%20zq%2C%20pp%3B%0A%20%20fv%20-%3E%20vj%2C%20zq%3B%0A%20%20pv%20-%3E%20lm%2C%20qm%3B%0A%20%20dg%20-%3E%20zz%2C%20jh%3B%0A%20%20fc%20-%3E%20fk%3B%0A%20%20qv%20-%3E%20dc%2C%20db%3B%0A%20%20ls%20-%3E%20rx%3B%0A%20%20tx%20-%3E%20ls%3B%0A%20%20vl%20-%3E%20fc%3B%0A%20%20dr%20-%3E%20fd%3B%0A%20%20dd%20-%3E%20ls%3B%0A%20%20kx%20-%3E%20jl%3B%0A%20%20sj%20-%3E%20qm%2C%20bl%3B%0A%20%20vj%20-%3E%20zq%3B%0A%20%20nk%20-%3E%20jh%2C%20vl%3B%0A%20%20xr%20-%3E%20kr%2C%20jh%3B%0A%20%20nz%20-%3E%20ls%3B%0A%20%20cz%20-%3E%20bf%3B%0A%20%20ms%20-%3E%20qm%3B%0A%20%20ct%20-%3E%20fx%3B%0A%20%20lg%20-%3E%20qm%2C%20ms%3B%0A%20%20lr%20-%3E%20dg%3B%0A%20%20pl%20-%3E%20dr%3B%0A%20%20rt%20-%3E%20zq%2C%20dj%3B%0A%20%20jn%20-%3E%20dc%3B%0A%20%20zz%20-%3E%20zm%3B%0A%20%20kf%20-%3E%20kl%2C%20dc%3B%0A%20%20jl%20-%3E%20cz%3B%0A%20%20hh%20-%3E%20fv%2C%20zq%3B%0A%20%20df%20-%3E%20mr%3B%0A%20%20jh%20-%3E%20zz%2C%20lr%2C%20vl%2C%20fc%2C%20nz%2C%20fk%2C%20qg%3B%0A%20%20fx%20-%3E%20hq%3B%0A%20%20hq%20-%3E%20df%2C%20dc%3B%0A%20%20kb%20-%3E%20qm%2C%20kx%3B%0A%20%20ph%20-%3E%20ls%3B%0A%20%20broadcaster%20-%3E%20kb%2C%20vq%2C%20ss%2C%20qg%3B%0A%20%20nd%20-%3E%20pl%2C%20zq%3B%0A%20%20gk%20-%3E%20rt%3B%0A%20%20mr%20-%3E%20dc%2C%20kf%3B%0A%20%20bs%20-%3E%20pv%3B%0A%20%20bh%20-%3E%20dc%2C%20ct%3B%0A%20%20kr%20-%3E%20jh%2C%20xm%3B%0A%20%20zm%20-%3E%20xr%2C%20jh%3B%0A%20%20%0A%20%20broadcaster%20%5Bshape%3DMdiamond%5D%3B%0A%20%20rx%20%5Bshape%3DMsquare%5D%3B%0A%20%20ls%2C%20dd%2C%20tx%2C%20ph%2C%20nz%20%5Bshape%3DMcircle%5D%3B%0A%0A%7D

    fun part2(input: Map<String, Module>): Long {
        var buttonPressCount = 1L
        val attachedToRX = input.values.first { "rx" in it.destinations } as Module.Conjuction
        val feeders = input.values.filter { it.name in attachedToRX.inputModules }
        for (feeder in feeders) {
            input.reset()
            var press = 0
            while (true) {
                press++
                val signals = input.pressButton()
                //if (signals.any { it.to == feeder.name && !it.level })
                if (signals.any { it.from == feeder.name && it.to == attachedToRX.name && it.level })
                    break
            }
            buttonPressCount *= press
        }
        return buttonPressCount
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day20_test").parseInput()
    check(part1(testInput) == 32000000)

    val testInput2 = readInput("Day20_test2").parseInput()
    check(part1(testInput2) == 11687500)

    val input = readInput("Day20").parseInput()
    measureTimeMillis {
        part1(input).println()
    }.also { println("Took ${it}ms") }
    measureTimeMillis {
        part2(input).println()
    }.also { println("Took ${it}ms") }
}
