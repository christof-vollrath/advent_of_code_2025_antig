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
