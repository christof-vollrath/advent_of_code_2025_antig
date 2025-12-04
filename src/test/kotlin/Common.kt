import java.lang.IllegalArgumentException
import kotlin.math.*

fun readResource(name: String) = Thread.currentThread().contextClassLoader.getResource(name)?.readText()

fun <T> List<List<T>>.transpose(): List<List<T>> {
    if (isEmpty()) return emptyList()
    val result = mutableListOf<List<T>>()
    val n = get(0).size
    for (i in  0 until n) {
        val col = mutableListOf<T>()
        for (row in this) {
            col.add(row[i])
        }
        result.add(col)
    }
    return result
}

fun <E> List<List<E>>.turnRight(): List<List<E>> {
    val result = mutableListOf<List<E>>()
    val n = this[0].size
    for (x in  0 until n) {
        val col = mutableListOf<E>()
        for (y in  (n-1) downTo 0) {
            col.add(this[y][x])
        }
        result.add(col)
    }
    return result
}

fun <E> List<E>.permute():List<List<E>> {
    if (size == 1) return listOf(this)
    val perms = mutableListOf<List<E>>()
    val sub = get(0)
    for(perm in drop(1).permute())
        for (i in 0..perm.size){
            val newPerm=perm.toMutableList()
            newPerm.add(i, sub)
            perms.add(newPerm)
        }
    return perms
}

fun <E> combine(e: Collection<Collection<E>>): Collection<Collection<E>> =
    if (e.isEmpty()) listOf(emptyList())
    else {
        e.first().flatMap { firstVariant ->
            combine(e.drop(1)).map {
                listOf(firstVariant) + it
            }
        }
    }

tailrec fun gcd(a: Int, b: Int): Int = // Greatest Common Divisor (Euclid, see: https://en.wikipedia.org/wiki/Greatest_common_divisor)
    when {
        a == 0 -> b
        b == 0 -> a
        a > b -> gcd(a-b, b)
        else -> gcd(a, b-a)
    }

tailrec fun gcd(a: Long, b: Long): Long =
    when {
        a == 0L -> b
        b == 0L -> a
        a > b -> gcd(a-b, b)
        else -> gcd(a, b-a)
    }

fun lcm(a: Int, b: Int) = abs(a * b) / gcd(a, b) // less common multiple (see: https://en.wikipedia.org/wiki/Least_common_multiple)
fun lcm(numbers: List<Int>) = numbers.drop(1).fold(numbers[0]) { acc, curr ->
    lcm(acc, curr)
}

fun lcm(a: Long, b: Long) = abs(a * b) / gcd(a, b)
fun lcm(numbers: List<Long>) = numbers.drop(1).fold(numbers[0]) { acc, curr ->
    lcm(acc, curr)
}

// see https://www.mathsisfun.com/polar-cartesian-coordinates.html
data class PolarCoordinate(val dist: Double, val angle: Double)

data class CartesianCoordinate(val x: Double, val y: Double) {
    fun toPolar(): PolarCoordinate {
        val dist = sqrt(x.pow(2) + y.pow(2))
        val h = atan(y / x)
        val angle = when {
            x >= 0 && y >= 0 -> h // Quadrant I
            x < 0 && y >= 0 -> h + PI // Quadrant II
            x < 0 && y < 0 -> h + PI // Quadrant III
            x >= 0 && y < 0 -> h + PI * 2 // Quadrant IV
            else -> throw IllegalArgumentException("Unknown quadrant for x=$x y=$y")
        }
        return PolarCoordinate(dist, angle)
    }
}

fun divisors(n: Int): Sequence<Int> = sequence {
    val largerDivisors = mutableListOf<Int>()
    val sqrtN = sqrt(n.toDouble()).toInt()
    
    for (i in 1..sqrtN) {
        if (n % i == 0) {
            yield(i)
            val complement = n / i
            if (complement != i) {
                largerDivisors.add(complement)
            }
        }
    }
    
    yieldAll(largerDivisors.reversed())
}

data class Coord2(val x: Int, val y: Int) {
    infix fun manhattanDistance(other: Coord2): Int = abs(x - other.x) + abs(y - other.y)
    operator fun plus(direction: Coord2) = Coord2(x + direction.x, y + direction.y)
    operator fun minus(direction: Coord2) = Coord2(x - direction.x, y - direction.y)
    operator fun times(n: Int) = Coord2(x * n, y * n)
    operator fun times(matrix: List<List<Int>>) =
        Coord2(x * matrix[0][0] + y * matrix[0][1],
            x * matrix[1][0] + y * matrix[1][1])
    fun neighbors() = neighborOffsets.map { neighborOffset ->
        this + neighborOffset
    }
    fun neighbors8() = neighbor8Offsets.map { neighborOffset ->
        this + neighborOffset
    }

    companion object {
        val neighborOffsets = listOf(Coord2(-1, 0), Coord2(1, 0), Coord2(0, -1), Coord2(0, 1))
        val neighbor8Offsets = (-1..1).flatMap { y ->
            (-1..1).mapNotNull { x ->
                if (x != 0 || y != 0) Coord2(x, y)
                else null
            }
        }
        val turnMatrixLeft = listOf(
            listOf(0, 1),
            listOf(-1, 0)
        )
        val turnMatrixRight = listOf(
            listOf(0, -1),
            listOf(1, 0)
        )
    }
}
typealias Plane<E>  = List<List<E>>

fun <E> Plane<E>.getOrNull(coord: Coord2): E? {
    return if (coord.y !in 0 until size) null
    else {
        val row = get(coord.y)
        if ( ! (0 <= coord.x && coord.x < row.size)) null
        else row[coord.x]
    }
}

data class Coord3(val x: Int, val y: Int, val z: Int) {
    operator fun plus(direction: Coord3) = Coord3(x + direction.x, y + direction.y, z + direction.z)
    operator fun minus(direction: Coord3) = Coord3(x - direction.x, y - direction.y, z - direction.z)
    operator fun times(n: Int) = Coord3(x * n, y * n, z * n)
    fun neighbors() = neighborOffsets.map { neighborOffset ->
        this + neighborOffset
    }
    fun neighbors26() = neighbor26Offsets.map { neighborOffset ->
        this + neighborOffset
    }

    companion object {
        val neighborOffsets = listOf(Coord3(-1, 0, 0), Coord3(1, 0, 0), Coord3(0, -1, 0), Coord3(0, 0, 1), Coord3(0, 0, -1), Coord3(0, 0, 1))
        val neighbor26Offsets = (-1..1).flatMap { z ->
            (-1..1).flatMap { y ->
                (-1..1).mapNotNull { x ->
                    if (x != 0 || y != 0 || z != 0) Coord3(x, y, z)
                    else null
                }
            }
        }
    }
}

typealias Cube<E>  = List<List<List<E>>>

fun <E> Cube<E>.getOrNull(coord: Coord3): E? {
    return if (coord.z !in this.indices) null
    else {
        val layer = this[coord.z]
        if (coord.y !in layer.indices) null
        else {
            val row = layer[coord.y]
            if (!(0 <= coord.x && coord.x < row.size)) null
            else row[coord.x]
        }
    }
}

data class Coord4(val x: Int, val y: Int, val z: Int, val w: Int) {
    operator fun plus(direction: Coord4) = Coord4(x + direction.x, y + direction.y, z + direction.z, w + direction.w)
    fun neighbors80() = neighbor80Offsets.map { neighborOffset ->
        this + neighborOffset
    }

    companion object {
        val neighbor80Offsets = (-1..1).flatMap { w ->
            (-1..1).flatMap { z ->
                (-1..1).flatMap { y ->
                    (-1..1).mapNotNull { x ->
                        if (x != 0 || y != 0 || z != 0 || w != 0) Coord4(x, y, z, w)
                        else null
                    }
                }
            }
        }
    }
}

