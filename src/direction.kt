enum class Direction {
    LEFT, RIGHT, UP, DOWN;

    val opposite by lazy {
        when (this) {
            LEFT -> RIGHT
            RIGHT -> LEFT
            UP -> DOWN
            DOWN -> UP
        }
    }
}

/*val Direction.opposite
    get() = when (this) {
        Direction.LEFT -> Direction.RIGHT
        Direction.RIGHT -> Direction.LEFT
        Direction.UP -> Direction.DOWN
        Direction.DOWN -> Direction.UP
    }*/
