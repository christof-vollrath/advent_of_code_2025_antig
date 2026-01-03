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
    var numComponents = n
        private set

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
            numComponents--
        }
    }

    fun getSizes(): List<Int> =
        parent.indices.filter { parent[it] == it }.map { size[it] }
}

fun buildCircuitsUnionFind(coords: List<Coord3>, sortedDistances: List<CoordsWithDistance>): Pair<List<Int>, Pair<Coord3, Coord3>?> {
    val unionFind = UnionFind(coords.size)
    val coordToIndex = coords.withIndex().associate { it.value to it.index }
    var lastConnection: Pair<Coord3, Coord3>? = null

    for (connection in sortedDistances) {
        val i = coordToIndex[connection.coord1]!!
        val j = coordToIndex[connection.coord2]!!
        if (unionFind.find(i) != unionFind.find(j)) {
            unionFind.union(i, j)
            lastConnection = connection.coord1 to connection.coord2
            if (unionFind.numComponents == 1) break
        }
    }

    return unionFind.getSizes() to lastConnection
}

typealias Circuit = MutableSet<Coord3>

fun buildCircuitsSets(coords: List<Coord3>, sortedDistances: List<CoordsWithDistance>): Pair<List<Int>, Pair<Coord3, Coord3>?> {
    /*
    val circuits = mutableSetOf<Circuit>()
    val circuitMap = mutableMapOf<Coord3, Circuit>()
    
    // Initialize: Every box is its own circuit
    coords.forEach { coord ->
        val circuit = mutableSetOf(coord)
        circuitMap[coord] = circuit
        circuits.add(circuit)
    }

    var recentConnection: Pair<Coord3, Coord3>? = null
    for (connection in sortedDistances) {
        val circuit1 = circuitMap[connection.coord1]!!
        val circuit2 = circuitMap[connection.coord2]!!
        
        if (circuit1 !== circuit2) { // Merge
            val (src, dest) = if (circuit1.size < circuit2.size) circuit1 to circuit2 else circuit2 to circuit1
            dest.addAll(src)
            src.forEach { coord -> circuitMap[coord] = dest }
            circuits.remove(src)
            recentConnection = connection.coord1 to connection.coord2
            
            if (circuits.size == 1) break
        }
    }
    
    return circuits.map { it.size } to recentConnection

     */
    val circuits = mutableSetOf<Circuit>()
    val circuitMap = mutableMapOf<Coord3, Circuit>()
    var recentConnection: Pair<Coord3, Coord3>? = null
    for (connection in sortedDistances) {
        val circuit1 = circuitMap[connection.coord1]
        val circuit2 = circuitMap[connection.coord2]

        if (circuit1 != null && circuit2 != null && circuit1 !== circuit2) { // Merge
            circuits.remove(circuit1) // Remove and add because of changing the circuit
            circuits.remove(circuit2)
            val (src, dest) = if (circuit1.size < circuit2.size) circuit1 to circuit2 else circuit2 to circuit1
            dest.addAll(src)
            src.forEach { coord -> circuitMap[coord] = dest }
            circuits.add(dest)
        } else if (circuit1 != null) {
            circuits.remove(circuit1) // Remove and add because of changing the circuit
            circuit1.add(connection.coord2)
            circuits.add(circuit1)
            circuitMap.put(connection.coord2, circuit1)
        } else if (circuit2 != null) {
            circuits.remove(circuit2) // Remove and add because of changing the circuit
            circuit2.add(connection.coord1)
            circuits.add(circuit2)
            circuitMap.put(connection.coord1, circuit2)
        } else {
            val circuit = mutableSetOf(connection.coord1, connection.coord2)
            circuits.add(circuit)
            circuitMap.put(connection.coord1, circuit)
            circuitMap.put(connection.coord2, circuit)
        }
        recentConnection = connection.coord1 to connection.coord2
        if (circuits.size == 1 && circuits.first().size == coords.size) break // Everything connected
    }
    // Make sure that every box is in a circuit
    coords.forEach { coord ->
        if (coord !in circuitMap) {
            circuits.add(mutableSetOf(coord))
        }
    }
    return circuits.map { it.size } to recentConnection
}

fun buildCircuits(coords: List<Coord3>, sortedDistances: List<CoordsWithDistance>): Pair<List<Int>, Pair<Coord3, Coord3>?> = buildCircuitsSets(coords, sortedDistances)

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
                val sizes = buildCircuits(coords, coordsWithDistance.take(10)).first.sortedDescending()
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
            val sizes = buildCircuits(coords, coordsWithDistance.take(1000)).first.sortedDescending()
            Then("calculate the product of the three largest circuits") {
                val result = sizes.take(3).reduce { acc, s -> acc * s }
                println("Result: $result")
                result shouldBe 32103
            }
        }
    }
})

class Day08Part2Test : BehaviorSpec({
    Given("example input") {
        val coords = parseListOfCoord3(exampleInputDay08)
        When("sorting by distance and building circuits for all connections") {
            val coordsWithDistance = sortedDistances(coords)
            When("circuits for all connections") {
                val (_, recentConnection) = buildCircuits(coords, coordsWithDistance)
                Then("first connection to connect all boxes should be correct") {
                    recentConnection shouldBe (Coord3(216, 146, 977) to Coord3(117, 168, 530))
                }
                Then("x product should be right") {
                    val result = recentConnection!!.first.x.toLong() * recentConnection.second.x
                    result shouldBe 25272L
                }
            }
        }
    }
    Given("puzzle input") {
        val input = readResource("day08Input.txt") ?: ""
        val coords = parseListOfCoord3(input)
        When("circuits for all connections") {
            val coordsWithDistance = sortedDistances(coords)
            val (_, recentConnection) = buildCircuits(coords, coordsWithDistance)
            Then("print and verify x product") {
                val result = recentConnection!!.first.x.toLong() * recentConnection.second.x
                println("Part 2 Result: $result")
                result shouldBe 8133642976L
            }
        }
    }})


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
