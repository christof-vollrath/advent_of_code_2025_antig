import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.system.measureTimeMillis

val exampleInputDay08 = """
162,817,812
57,618,57
906,360,560
592,479,940
352,342,300
466,668,158
542,29,236
431,825,988
739,650,466
52,470,668
216,146,977
819,987,18
117,168,530
805,96,715
346,949,466
970,615,88
941,993,340
862,61,35
984,92,344
425,690,689""".trimIndent()

fun parseListOfCoord3(input: String): List<Coord3> =
    input.lineSequence()
        .filter { it.isNotBlank() }
        .map { line ->
            val (x, y, z) = line.split(",").map { it.trim().toInt() }
            Coord3(x, y, z)
        }.toList()

data class CoordsWithDistance (
    val coord1: Coord3,
    val coord2: Coord3,
    val dist: Double
)

fun sortedDistances(coords: List<Coord3>): List<CoordsWithDistance> =
    coords.indices.flatMap { i ->
        (i + 1 until coords.size).map { j ->
            val c1 = coords[i]
            val c2 = coords[j]
            CoordsWithDistance(c1, c2, c1 euclideanDistance c2)
        }
    }.sortedBy { it.dist }

class UnionFind(n: Int) {
    private val parent = IntArray(n) { it }
    private val size = IntArray(n) { 1 }

    fun find(i: Int): Int {
        if (parent[i] == i) return i
        parent[i] = find(parent[i])
        return parent[i]
    }

    fun union(i: Int, j: Int) {
        val rootI = find(i)
        val rootJ = find(j)
        if (rootI != rootJ) {
            if (size[rootI] < size[rootJ]) {
                parent[rootI] = rootJ
                size[rootJ] += size[rootI]
            } else {
                parent[rootJ] = rootI
                size[rootI] += size[rootJ]
            }
        }
    }

    fun getSizes(): List<Int> =
        parent.indices.filter { parent[it] == it }.map { size[it] }
}

fun buildCircuitsUnionFind(coords: List<Coord3>, sortedDistances: List<CoordsWithDistance>): List<Int> {
    val unionFind = UnionFind(coords.size)
    val coordToIndex = coords.withIndex().associate { it.value to it.index }

    sortedDistances.forEach { connection ->
        val i = coordToIndex[connection.coord1]!!
        val j = coordToIndex[connection.coord2]!!
        unionFind.union(i, j)
    }

    return unionFind.getSizes()
}

typealias Circuit = MutableSet<Coord3>

fun buildCircuitsSets(coords: List<Coord3>, sortedDistances: List<CoordsWithDistance>): List<Int> {
    val circuits = mutableSetOf<Circuit>()
    val circuitMap = mutableMapOf<Coord3, Circuit>()
    sortedDistances.forEach { connection ->
        val circuit1 = circuitMap[connection.coord1]
        val circuit2 = circuitMap[connection.coord2]
        if (circuit1 != null && circuit2 != null && circuit1 !== circuit2) { // Merge
            val (src, dest) = if (circuit1.size < circuit2.size) circuit1 to circuit2 else circuit2 to circuit1
            dest.addAll(src)
            src.forEach { coord -> circuitMap[coord] = dest }
            circuits.remove(src)
        } else if (circuit1 != null) {
            circuit1.add(connection.coord2)
            circuitMap.put(connection.coord2, circuit1)
        } else if (circuit2 != null) {
            circuit2.add(connection.coord1)
            circuitMap.put(connection.coord1, circuit2)
        } else {
            val circuit = mutableSetOf(connection.coord1, connection.coord2)
            circuits.add(circuit)
            circuitMap.put(connection.coord1, circuit)
            circuitMap.put(connection.coord2, circuit)
        }
    }
    // Make sure that every box is in a circuit
    coords.forEach { coord ->
        if (coord !in circuitMap) {
            circuits.add(mutableSetOf(coord))
        }
    }
    return circuits.map { it.size }
}

fun buildCircuits(coords: List<Coord3>, sortedDistances: List<CoordsWithDistance>): List<Int> = buildCircuitsUnionFind(coords, sortedDistances)

class Day08Part1Test : BehaviorSpec({
    Given("example input") {
        val coords = parseListOfCoord3(exampleInputDay08)
        Then("should have the right coords") {
            coords.size shouldBe 20
            coords[0] shouldBe Coord3(162, 817, 812)
        }
        When("sorting by distance and building circuits for 10 connections") {
                val coordsWithDistance = sortedDistances(coords)
                Then("shortest distance should be at the begining") {
                    coordsWithDistance[0].coord1 shouldBe Coord3(162, 817, 812)
                    coordsWithDistance[0].coord2 shouldBe Coord3(425, 690, 689)
                    coordsWithDistance.size shouldBe 20*19 / 2 // every coord with every other
                }
            When("circuits for 10 connections") {
                val sizes = buildCircuits(coords, coordsWithDistance.take(10)).sortedDescending()
                Then("should have found the right circuit sizes") {
                    sizes shouldBe listOf(5, 4, 2, 2, 1, 1, 1, 1, 1, 1, 1)
                }
                Then("product of top 3 should be 40") {
                    sizes.take(3).reduce { acc, s -> acc * s } shouldBe 40
                }
            }
        }
    }

    Given("puzzle input") {
        val input = readResource("day08Input.txt") ?: ""
        val coords = parseListOfCoord3(input)
        When("sorting by distance and building circuits for 1000 connections") {
            val coordsWithDistance = sortedDistances(coords)
            val sizes = buildCircuits(coords, coordsWithDistance.take(1000)).sortedDescending()
            Then("calculate the product of the three largest circuits") {
                val result = sizes.take(3).reduce { acc, s -> acc * s }
                println("Result: $result")
                result shouldBe 32103
            }
        }
    }
})

class Day08PerformanceTest : BehaviorSpec({
    Given("puzzle input") {
        val input = readResource("day08Input.txt") ?: ""
        val coords = parseListOfCoord3(input)
        val coordsWithDistance = sortedDistances(coords).take(1000)

        When("comparing performance") {
            // Warm up
            repeat(10) {
                buildCircuitsSets(coords, coordsWithDistance)
                buildCircuitsUnionFind(coords, coordsWithDistance)
            }

            val iterations = 100
            val setsTime = measureTimeMillis {
                repeat(iterations) {
                    buildCircuitsSets(coords, coordsWithDistance)
                }
            }
            val unionFindTime = measureTimeMillis {
                repeat(iterations) {
                    buildCircuitsUnionFind(coords, coordsWithDistance)
                }
            }

            println("Average time (Sets): ${setsTime.toDouble() / iterations} ms") // 0.19 ms average
            println("Average time (Union-Find): ${unionFindTime.toDouble() / iterations} ms") // 0.15 ms average
        }
    }
})
