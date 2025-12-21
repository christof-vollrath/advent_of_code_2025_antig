import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

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

fun buildCircuits(coords: List<Coord3>, sortedDistances: List<CoordsWithDistance>, connections: Int): List<Int> {
    val unionFind = UnionFind(coords.size)
    val coordToIndex = coords.withIndex().associate { it.value to it.index }

    sortedDistances.take(connections).forEach { connection ->
        val i = coordToIndex[connection.coord1]!!
        val j = coordToIndex[connection.coord2]!!
        unionFind.union(i, j)
    }

    return unionFind.getSizes()
}

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
                }
            When("circuits for 10 connections") {
                val sizes = buildCircuits(coords, coordsWithDistance, 10).sortedDescending()
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
            val sizes = buildCircuits(coords, coordsWithDistance, 1000).sortedDescending()
            Then("calculate the product of the three largest circuits") {
                val result = sizes.take(3).reduce { acc, s -> acc * s }
                println("Result: $result")
                result shouldBe 32103
            }
        }
    }
})
