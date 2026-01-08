import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlin.math.abs

val exampleInputDay09 = """
7,1
11,1
11,7
9,7
9,5
2,5
2,3
7,3""".trimIndent()

fun parseCoords(input: String): List<Coord2> =
    input.lineSequence()
        .filter { it.isNotBlank() }
        .map { line ->
            val (x, y) = line.split(",").map { it.trim().toInt() }
            Coord2(x, y)
        }.toList()

fun rectangleArea(c1: Coord2, c2: Coord2): Long {
    val width = abs(c1.x - c2.x).toLong() + 1
    val height = abs(c1.y - c2.y).toLong() + 1
    return width * height
}

fun findLargestRectangle(coords: List<Coord2>): Long {
    var maxArea = 0L
    for (i in coords.indices) {
        for (j in i + 1 until coords.size) {
            val area = rectangleArea(coords[i], coords[j])
            if (area > maxArea) {
                maxArea = area
            }
        }
    }
    return maxArea
}

/**
 * Coordinate Compression:
 * Maps sparse, large world coordinates to a dense, small grid.
 * For N unique coordinates, we create a grid of size 2N-1 to represent:
 * - The integer coordinates themselves (indices 0, 2, 4...)
 * - The spaces/gaps between them (indices 1, 3, 5...)
 */
class CoordinateCompressor(coords: List<Int>) {
    val values = coords.distinct().sorted()
    private val valToIndex = values.withIndex().associate { it.value to it.index }
    val size: Int get() = values.size
    val gridW: Int get() = 2 * size - 1

    fun map(v: Int): Int = valToIndex[v]!!
    
    /**
     * Translates a grid index back to a world coordinate value.
     * - Even indices (0, 2, 4...) map to actual tile coordinates.
     * - Odd indices (1, 3, 5...) map to the midpoints between tiles.
     * This is essential for ray casting, as we need to sample points 
     * both ON tiles and IN THE GAPS between them.
     */
    fun getDouble(gridIndex: Int): Double =
        if (gridIndex % 2 == 0) values[gridIndex / 2].toDouble()
        else (values[gridIndex / 2] + values[gridIndex / 2 + 1]) / 2.0
}

/**
 * 2D Prefix Sums (Summed-Area Table):
 * Allows O(1) sum queries over any rectangular sub-grid.
 * Used here to instantly check if a candidate rectangle contains any "bad" (outside) tiles.
 */
class SummedAreaTable(grid: Array<BooleanArray>) {
    private val width = grid.size
    private val height = if (width > 0) grid[0].size else 0
    private val sumTable = Array(width + 1) { IntArray(height + 1) }

    init {
        for (i in 0 until width) {
            for (j in 0 until height) {
                val value = if (grid[i][j]) 0 else 1
                sumTable[i + 1][j + 1] = value + sumTable[i][j + 1] + sumTable[i + 1][j] - sumTable[i][j]
            }
        }
    }

    fun isAllGreen(xRange: IntRange, yRange: IntRange): Boolean {
        val sum = sumTable[xRange.last + 1][yRange.last + 1] - 
                  sumTable[xRange.first][yRange.last + 1] - 
                  sumTable[xRange.last + 1][yRange.first] + 
                  sumTable[xRange.first][yRange.first]
        return sum == 0
    }
}

class RectilinearPolygonGrid(coords: List<Coord2>) {
    private val compressX = CoordinateCompressor(coords.map { it.x })
    private val compressY = CoordinateCompressor(coords.map { it.y })
    private val sat: SummedAreaTable

    init {
        val grid = Array(compressX.gridW) { BooleanArray(compressY.gridW) }
        val edges = coords.indices.map { i -> coords[i] to coords[(i + 1) % coords.size] }

        for (jj in 0 until compressY.gridW) {
            val y = compressY.getDouble(jj)
            
            // Discrete Ray Casting:
            // For each row (jj), we find vertical edges that cross the y-coordinate.
            // Using the rule 'min <= y < max' ensures vertices are crossed exactly once.
            val horizontalRanges = if (jj % 2 == 0) {
                val yInt = compressY.values[jj / 2]
                edges.filter { it.first.y == it.second.y && it.first.y == yInt }
                     .map { kotlin.math.min(it.first.x, it.second.x)..kotlin.math.max(it.first.x, it.second.x) }
            } else emptyList()

            val crossX = edges.filter { (v1, v2) ->
                v1.x == v2.x && y >= kotlin.math.min(v1.y, v2.y).toDouble() && y < kotlin.math.max(v1.y, v2.y).toDouble()
            }.map { it.first.x }.distinct().sorted()

            for (ii in 0 until compressX.gridW) {
                val x = compressX.getDouble(ii)
                val onBoundary = if (ii % 2 == 0) {
                    val xInt = compressX.values[ii / 2]
                    val onVertical = edges.any { it.first.x == it.second.x && it.first.x == xInt && y >= kotlin.math.min(it.first.y, it.second.y) && y <= kotlin.math.max(it.first.y, it.second.y) }
                    val onHorizontal = if (jj % 2 == 0) horizontalRanges.any { xInt in it } else false
                    onVertical || onHorizontal
                } else {
                    if (jj % 2 == 0) {
                        val x1 = compressX.values[ii / 2]
                        val x2 = compressX.values[ii / 2 + 1]
                        horizontalRanges.any { x1 >= it.first && x2 <= it.last }
                    } else false
                }

                if (onBoundary) {
                    grid[ii][jj] = true
                } else {
                    // Jordan Curve Theorem: A point is inside if a ray from it crosses the boundary an odd number of times.
                    grid[ii][jj] = crossX.count { it > x } % 2 != 0
                }
            }
        }
        sat = SummedAreaTable(grid)
    }

    fun containsRectangle(c1: Coord2, c2: Coord2): Boolean {
        val xIndices = (2 * compressX.map(kotlin.math.min(c1.x, c2.x)) .. 2 * compressX.map(kotlin.math.max(c1.x, c2.x)))
        val yIndices = (2 * compressY.map(kotlin.math.min(c1.y, c2.y)) .. 2 * compressY.map(kotlin.math.max(c1.y, c2.y)))
        return sat.isAllGreen(xIndices, yIndices)
    }
}

fun findLargestRectanglePart2(coords: List<Coord2>): Long {
    val grid = RectilinearPolygonGrid(coords)
    var maxArea = 0L
    for (i in coords.indices) {
        for (j in i + 1 until coords.size) {
            val c1 = coords[i]
            val c2 = coords[j]
            val area = (abs(c1.x - c2.x) + 1).toLong() * (abs(c1.y - c2.y) + 1)
            if (area > maxArea && grid.containsRectangle(c1, c2)) {
                maxArea = area
            }
        }
    }
    return maxArea
}

class Day09Part1Test : BehaviorSpec({
    Given("example input") {
        val coords = parseCoords(exampleInputDay09)
        Then("coords should be parsed correctly") {
            coords.size shouldBe 8
            coords[0] shouldBe Coord2(7, 1)
        }
        When("finding the largest rectangle") {
            val largestArea = findLargestRectangle(coords)
            Then("area should be 50") {
                largestArea shouldBe 50
            }
        }
    }

    Given("puzzle input") {
        val input = readResource("day09Input.txt") ?: ""
        if (input.isNotBlank()) {
            val coords = parseCoords(input)
            When("finding the largest rectangle") {
                val largestArea = findLargestRectangle(coords)
                largestArea shouldBeGreaterThan 2147477792
                Then("calculate and print the largest area") {
                    println("Day 09 Part 1 Result: $largestArea")
                    largestArea shouldBe 4733727792
                }
            }
        }
    }
})

class Day09Part2Test : BehaviorSpec({
    Given("example input") {
        val coords = parseCoords(exampleInputDay09)
        When("finding the largest rectangle in the loop") {
            val largestArea = findLargestRectanglePart2(coords)
            Then("area should be 24") {
                largestArea shouldBe 24L
            }
        }
    }

    Given("puzzle input") {
        val input = readResource("day09Input.txt") ?: ""
        if (input.isNotBlank()) {
            val coords = parseCoords(input)
            When("finding the largest rectangle in the loop") {
                val largestArea = findLargestRectanglePart2(coords)
                largestArea shouldBeGreaterThan 1565906223
                Then("calculate and print the largest area") {
                    println("Day 09 Part 2 Result: $largestArea")
                    largestArea shouldBe 1566346198
                }
            }
        }
    }
})
