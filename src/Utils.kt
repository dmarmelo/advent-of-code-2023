import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.io.path.readText

/**
 * Reads all text from the given input txt file.
 */
fun readInputRaw(name: String) = Path("src/$name.txt").readText()

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)

fun Sequence<Int>.product(): Int =
        reduce { acc, item -> acc * item }

fun Iterable<Int>.product(): Int =
        reduce { acc, item -> acc * item }

fun Iterable<Long>.product(): Long =
        reduce { acc, item -> acc * item }
