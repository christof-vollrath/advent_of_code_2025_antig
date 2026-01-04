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

fun findLargestRectanglePart2(coords: List<Coord2>): Long {
    val xs = coords.map { it.x }.distinct().sorted()
    val ys = coords.map { it.y }.distinct().sorted()
    
    val xMap = xs.withIndex().associate { it.value to it.index }
    val yMap = ys.withIndex().associate { it.value to it.index }
    
    val gridW = 2 * xs.size - 1
    val gridH = 2 * ys.size - 1
    val isGreen = Array(gridW) { BooleanArray(gridH) }
    
    val edges = coords.indices.map { i -> coords[i] to coords[(i + 1) % coords.size] }

    for (jj in 0 until gridH) {
        val y = if (jj % 2 == 0) ys[jj / 2].toDouble() else (ys[jj / 2] + ys[jj / 2 + 1]) / 2.0
        
        // Horizontal edges that exactly match this y (if jj is even)
        val horizontalRanges = if (jj % 2 == 0) {
            val yInt = ys[jj / 2]
            edges.filter { it.first.y == it.second.y && it.first.y == yInt }
                 .map { kotlin.math.min(it.first.x, it.second.x)..kotlin.math.max(it.first.x, it.second.x) }
        } else emptyList()

        // Vertical edges that cross this y. 
        // For ray casting at integer y, use the rule: min <= y < max.
        // For ray casting at gap y (midpoint), min < y < max is the same.
        val crossX = edges.filter { (v1, v2) ->
            v1.x == v2.x && y >= kotlin.math.min(v1.y, v2.y).toDouble() && y < kotlin.math.max(v1.y, v2.y).toDouble()
        }.map { it.first.x }.distinct().sorted()

        for (ii in 0 until gridW) {
            val x = if (ii % 2 == 0) xs[ii / 2].toDouble() else (xs[ii / 2] + xs[ii / 2 + 1]) / 2.0
            
            // 1. Is it on a boundary?
            val onBoundary = if (ii % 2 == 0) {
                // Point (x, y)
                val xInt = xs[ii / 2]

                val onVertical = edges.any { it.first.x == it.second.x && it.first.x == xInt && y >= kotlin.math.min(it.first.y, it.second.y) && y <= kotlin.math.max(it.first.y, it.second.y) }
                val onHorizontal = if (jj % 2 == 0) horizontalRanges.any { xInt in it } else false
                onVertical || onHorizontal
            } else {
                // Segment (xGap, y)
                if (jj % 2 == 0) {
                    val x1 = xs[ii / 2]
                    val x2 = xs[ii / 2 + 1]
                    horizontalRanges.any { x1 >= it.first && x2 <= it.last }
                } else {
                    // Gap x, gap y. Never on boundary.
                    false
                }
            }

            if (onBoundary) {
                isGreen[ii][jj] = true
            } else {
                // 2. Use ray casting rule: count crossings where crossX > x
                // Note: we only hit vertical edges that cross y according to the min <= y < max rule.
                val crossings = crossX.count { it > x }
                isGreen[ii][jj] = crossings % 2 != 0
            }
        }
    }

    val notGreenSum = Array(gridW + 1) { IntArray(gridH + 1) }
    for (i in 0 until gridW) {
        for (j in 0 until gridH) {
            val valNotGreen = if (isGreen[i][j]) 0 else 1
            notGreenSum[i + 1][j + 1] = valNotGreen + notGreenSum[i][j + 1] + notGreenSum[i + 1][j] - notGreenSum[i][j]
        }
    }

    fun isRectangleAllGreen(ii1: Int, jj1: Int, ii2: Int, jj2: Int): Boolean {
        val sum = notGreenSum[ii2 + 1][jj2 + 1] - notGreenSum[ii1][jj2 + 1] - notGreenSum[ii2 + 1][jj1] + notGreenSum[ii1][jj1]
        return sum == 0
    }

    var maxArea = 0L
    for (i in coords.indices) {
        for (j in i + 1 until coords.size) {
            val c1 = coords[i]
            val c2 = coords[j]
            val xMin = kotlin.math.min(c1.x, c2.x)
            val xMax = kotlin.math.max(c1.x, c2.x)
            val yMin = kotlin.math.min(c1.y, c2.y)
            val yMax = kotlin.math.max(c1.y, c2.y)
            
            val area = (xMax - xMin + 1).toLong() * (yMax - yMin + 1)
            if (area > maxArea) {
                val ii1 = 2 * xMap[xMin]!!
                val ii2 = 2 * xMap[xMax]!!
                val jj1 = 2 * yMap[yMin]!!
                val jj2 = 2 * yMap[yMax]!!
                
                if (isRectangleAllGreen(ii1, jj1, ii2, jj2)) {
                    maxArea = area
                }
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
