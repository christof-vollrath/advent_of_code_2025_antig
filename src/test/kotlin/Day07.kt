import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.BehaviorSpec

class Day07 {
    data class Result(val map: List<String>, val splits: Int)

    fun solve(input: List<String>): Result {
        if (input.isEmpty()) return Result(emptyList(), 0)
        val height = input.size
        val width = input[0].length
        val outputGrid = input.map { it.toCharArray() }.toTypedArray()
        
        fun findStart(): Coord2? = input.withIndex().firstNotNullOfOrNull { (r, row) ->
            val c = row.indexOf('S')
            if (c != -1) Coord2(c, r) else null
        }

        val start = findStart() ?: return Result(input, 0)

        // Active columns in the current row
        var currentCols: Set<Int> = setOf(start.x)
        var hitSplitters = 0

        // Iterate through rows starting from finding S
        for (r in start.y until height - 1) {
            val nextR = r + 1

            val nextCols = sequence {
                for (c in currentCols) {
                    val targetChar = input[nextR][c]

                    if (targetChar == '^') {
                        hitSplitters++
                        // Split: add left and right to next row
                        val left = c - 1
                        val right = c + 1
                        if (left >= 0) yield(left)
                        if (right < width) yield(right)
                    } else {
                        // Continue straight down
                        yield(c)
                    }
                }
            }.toSet()

            // Mark beams on the output grid for the next row
            for (c in nextCols) {
                if (outputGrid[nextR][c] == '.') {
                    outputGrid[nextR][c] = '|'
                }
            }
            
            if (nextCols.isEmpty()) break
            currentCols = nextCols
        }
        
        return Result(outputGrid.map { String(it) }, hitSplitters)
    }
}

class Day07Part1Test : BehaviorSpec({
    val exampleInput = """
        .......S.......
        ...............
        .......^.......
        ...............
        ......^.^......
        ...............
        .....^.^.^.....
        ...............
        ....^.^...^....
        ...............
        ...^.^...^.^...
        ...............
        ..^...^.....^..
        ...............
        .^.^.^.^.^...^.
        ...............
    """.trimIndent().lines()

    val expectedOutput = """
        .......S.......
        .......|.......
        ......|^|......
        ......|.|......
        .....|^|^|.....
        .....|.|.|.....
        ....|^|^|^|....
        ....|.|.|.|....
        ...|^|^|||^|...
        ...|.|.|||.|...
        ..|^|^|||^|^|..
        ..|.|.|||.|.|..
        .|^|||^||.||^|.
        .|.|||.||.||.|.
        |^|^|^|^|^|||^|
        |.|.|.|.|.|||.|
    """.trimIndent().lines()

    Given("Day 7 Part 1") {
        When("example input is provided") {
            val result = Day07().solve(exampleInput)
            
            Then("it produces the expected map with beams") {
                result.map shouldBe expectedOutput
            }
            
            Then("it counts correct number of splits") {
                result.splits shouldBe 21
            }
        }
        When("puzzle input is provided") {
            val input = readResource("day07Input.txt")!!.lines()
            val result = Day07().solve(input)

            Then("it counts splits") {
                println("Splits in puzzle input: ${result.splits}")
                result.splits shouldBe 1656
            }
        }
    }
})
