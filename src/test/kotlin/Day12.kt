import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class Day12 {
    data class Orientation(val bits: List<Coord2>, val width: Int, val height: Int, val size: Int)

    data class Shape(val coords: Set<Coord2>) {
        val width = if (coords.isEmpty()) 0 else coords.maxOf { it.x } + 1
        val height = if (coords.isEmpty()) 0 else coords.maxOf { it.y } + 1
        val size = coords.size

        fun rotate(): Shape {
            val h = height
            return Shape(coords.map { Coord2(h - 1 - it.y, it.x) }.toSet()).normalize()
        }

        fun flip(): Shape {
            val w = width
            return Shape(coords.map { Coord2(w - 1 - it.x, it.y) }.toSet()).normalize()
        }

        fun normalize(): Shape {
            if (coords.isEmpty()) return this
            val minX = coords.minOf { it.x }
            val minY = coords.minOf { it.y }
            return Shape(coords.map { Coord2(it.x - minX, it.y - minY) }.toSet())
        }

        fun allOrientations(w: Int, h: Int): Set<Orientation> {
            val res = mutableSetOf<Orientation>()
            var current = this.normalize()
            repeat(4) {
                if (current.width <= w && current.height <= h) res.add(current.toOrientation())
                val flipped = current.flip()
                if (flipped.width <= w && flipped.height <= h) res.add(flipped.toOrientation())
                current = current.rotate()
            }
            return res
        }

        private fun toOrientation(): Orientation {
            return Orientation(coords.toList(), width, height, size)
        }
    }

    data class Region(val width: Int, val height: Int, val requiredCounts: Map<Int, Int>)

    fun parseShapes(input: String): List<Shape> {
        return input.split(Regex("(?m)^\\d+:"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { block ->
                val coords = mutableSetOf<Coord2>()
                block.lines().forEachIndexed { y, line ->
                    line.forEachIndexed { x, char ->
                        if (char == '#') coords.add(Coord2(x, y))
                    }
                }
                Shape(coords).normalize()
            }
    }

    fun parseRegions(input: String): List<Region> {
        return input.trim().lines().filter { it.isNotBlank() && it.contains("x") }.map { line ->
            val parts = line.split(":")
            val dim = parts[0].trim().split("x")
            val w = dim[0].toInt()
            val h = dim[1].toInt()
            val counts = parts[1].trim().split(Regex("\\s+")).filter { it.isNotBlank() }.map { it.toInt() }
            val required = counts.mapIndexedNotNull { index, count ->
                if (count > 0) index to count else null
            }.toMap()
            Region(w, h, required)
        }
    }

    fun solve(inputText: String): Int {
        val lines = inputText.lines().filter { it.isNotBlank() }
        val shapesPart = lines.takeWhile { !it.contains("x") }.joinToString("\n")
        val regionsPart = lines.dropWhile { !it.contains("x") }.joinToString("\n")
        
        val shapes = parseShapes(shapesPart)
        val regions = parseRegions(regionsPart)
        
        return regions.count { region ->
            canFit(region, shapes)
        }
    }

    fun solve(shapesInput: String, regionsInput: String): Int {
        val shapes = parseShapes(shapesInput)
        val regions = parseRegions(regionsInput)
        return regions.count { canFit(it, shapes) }
    }

    fun canFit(region: Region, shapes: List<Shape>): Boolean {
        val w = region.width
        val h = region.height
        val totalCells = w * h
        
        val shapeTypeOrientations = shapes.map { it.allOrientations(w, h) }
        val totalShapeCells = region.requiredCounts.map { (index, count) -> shapes[index].size * count }.sum()
        
        if (totalShapeCells > totalCells) return false
        
        val grid = java.util.BitSet(totalCells)
        val remShapes = IntArray(shapes.size) { region.requiredCounts[it] ?: 0 }
        val maxEmpty = totalCells - totalShapeCells
        
        return backtrack(grid, 0, maxEmpty, remShapes, shapeTypeOrientations, w, h)
    }

    private fun backtrack(
        grid: java.util.BitSet,
        firstPossibleEmpty: Int,
        maxEmpty: Int,
        remShapes: IntArray,
        shapeTypeOrientations: List<Set<Orientation>>,
        w: Int,
        h: Int
    ): Boolean {
        if (remShapes.all { it == 0 }) return true

        val firstEmpty = grid.nextClearBit(firstPossibleEmpty)
        if (firstEmpty >= w * h) return false

        val ey = firstEmpty / w
        val ex = firstEmpty % w

        // Try to cover firstEmpty with each available shape type
        for (t in remShapes.indices) {
            if (remShapes[t] > 0) {
                for (orient in shapeTypeOrientations[t]) {
                    for (anchor in orient.bits) {
                        val dx = ex - anchor.x
                        val dy = ey - anchor.y
                        
                        if (dx >= 0 && dx + orient.width <= w && dy >= 0 && dy + orient.height <= h) {
                            if (canPlace(grid, orient, dx, dy, w)) {
                                place(grid, orient, dx, dy, w, true)
                                remShapes[t]--
                                if (backtrack(grid, firstEmpty + 1, maxEmpty, remShapes, shapeTypeOrientations, w, h)) return true
                                remShapes[t]++
                                place(grid, orient, dx, dy, w, false)
                            }
                        }
                    }
                }
            }
        }

        // Try skipping firstEmpty
        if (maxEmpty > 0) {
            grid.set(firstEmpty)
            if (backtrack(grid, firstEmpty + 1, maxEmpty - 1, remShapes, shapeTypeOrientations, w, h)) return true
            grid.clear(firstEmpty)
        }

        return false
    }

    private fun canPlace(grid: java.util.BitSet, orient: Orientation, dx: Int, dy: Int, w: Int): Boolean {
        for (bit in orient.bits) {
            if (grid.get((dy + bit.y) * w + (dx + bit.x))) return false
        }
        return true
    }

    private fun place(grid: java.util.BitSet, orient: Orientation, dx: Int, dy: Int, w: Int, value: Boolean) {
        for (bit in orient.bits) {
            grid.set((dy + bit.y) * w + (dx + bit.x), value)
        }
    }
}



val shapesInputDay12 = """
0:
###
##.
##.

1:
###
##.
.##

2:
.##
###
##.

3:
##.
###
##.

4:
###
#..
###

5:
###
.#.
###
    """.trimIndent()

val regionsInputDay12 = """
4x4: 0 0 0 0 2 0
12x5: 1 0 1 0 2 2
12x5: 1 0 1 0 3 2
    """.trimIndent()

class Day12CanFitTest : BehaviorSpec({
    val day12 = Day12()
    val shapes = day12.parseShapes(shapesInputDay12)

    Given("a small region and one shape") {
        When("the shape fits exactly") {
            val region = Day12.Region(3, 3, mapOf(0 to 1))
            Then("canFit should return true") {
                day12.canFit(region, shapes) shouldBe true
            }
        }

        When("the shape is too wide") {
            val region = Day12.Region(2, 3, mapOf(0 to 1))
            Then("canFit should return false") {
                day12.canFit(region, shapes) shouldBe false
            }
        }
    }

    Given("rotation and flipping requirements") {
        val lShapeInput = """
            0:
            ###
            #..
        """.trimIndent()
        val lShape = day12.parseShapes(lShapeInput)

        When("the shape only fits if rotated") {
            // L-shape is 3x2. In a 2x3 region, it must be rotated.
            val region = Day12.Region(2, 3, mapOf(0 to 1))
            Then("canFit should return true") {
                day12.canFit(region, lShape) shouldBe true
            }
        }
    }

    Given("multiple shapes") {
        When("they can fit together") {
            // Two 2x2 squares in a 4x2 region
            val squareInput = "0:\n##\n##"
            val square = day12.parseShapes(squareInput)
            val region = Day12.Region(4, 2, mapOf(0 to 2))
            Then("canFit should return true") {
                day12.canFit(region, square) shouldBe true
            }
        }

        When("they overlap") {
            // Two 2x2 squares in a 3x2 region (must overlap)
            val squareInput = "0:\n##\n##"
            val square = day12.parseShapes(squareInput)
            val region = Day12.Region(3, 2, mapOf(0 to 2))
            Then("canFit should return false") {
                day12.canFit(region, square) shouldBe false
            }
        }
    }
})

class Day12Part1Test : BehaviorSpec({

    Given("the example input") {
        val day12 = Day12()
        
        When("solving for the example regions") {
            val result = day12.solve(shapesInputDay12, regionsInputDay12)
            
            Then("it should return 2") {
                result shouldBe 2
            }
        }
    }

    Given("the puzzle input") {
        val day12 = Day12()
        val input = readResource("day12Input.txt") ?: ""

        When("solving for the puzzle regions") {
            val result = day12.solve(input)
            Then("the result should be 487") {
                println("Day 12 Result: $result")
                result shouldBe 487
            }
        }
    }
})


