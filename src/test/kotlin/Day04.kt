import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

// Parse the grid into a 2D list of characters
fun parseGrid(input: String): Plane<Char> =
    input.trim().split("\n").map { it.toList() }

// Count adjacent rolls of paper (@) for a given position
fun countAdjacentRolls(grid: Plane<Char>, row: Int, col: Int): Int {
    val coord = Coord2(col, row)
    return coord.neighbors8().count { neighbor ->
        grid.getOrNull(neighbor) == '@'
    }
}

// Mark positions with less than 4 adjacent rolls
fun markAccessibleRolls(grid: Plane<Char>): Plane<Char> {
    val height = grid.size
    val width = grid[0].size
    val result = grid.map { it.toMutableList() }
    
    for (row in 0 until height) {
        for (col in 0 until width) {
            // Only check positions with rolls
            if (grid[row][col] == '@') {
                val adjacentCount = countAdjacentRolls(grid, row, col)
                if (adjacentCount < 4) {
                    result[row][col] = 'x'
                }
            }
        }
    }
    
    return result
}

// Count all accessible rolls (marked with 'x')
fun countAccessibleRolls(markedGrid: Plane<Char>): Int =
    markedGrid.sumOf { row -> row.count { it == 'x' } }

// Complete solution for part 1
fun solveDay04Part1(input: String): Int {
    val grid = parseGrid(input)
    val markedGrid = markAccessibleRolls(grid)
    return countAccessibleRolls(markedGrid)
}

class Day04Part1Test : BehaviorSpec({
    Given("a grid input") {
        When("parsing the grid") {
            Then("it should create a 2D list of characters") {
                val input = """
                    ..@@
                    @@@.
                """.trimIndent()
                
                val grid = parseGrid(input)
                
                grid.size shouldBe 2
                grid[0].size shouldBe 4
                grid[0] shouldBe listOf('.', '.', '@', '@')
                grid[1] shouldBe listOf('@', '@', '@', '.')
            }
        }
    }
    
    Given("a position in the grid") {
        When("counting adjacent rolls of paper") {
            Then("it should count all 8 adjacent positions correctly") {
                forAll(
                    // Grid position (row, col), expected count
                    // Grid is: .@@@
                    //          @@@.
                    row(0, 0, 3), // Top-left corner '.': has @ at (0,1), (1,0), (1,1)
                    row(0, 1, 4), // Position '@': has @ at (0,2), (1,0), (1,1), (1,2)
                    row(1, 1, 4), // Position '@': has @ at (0,1), (0,2), (1,0), (1,2)
                    row(0, 3, 2), // Top-right '@': has @ at (0,2), (1,2)
                    row(1, 3, 3)  // Bottom-right '.': has @ at (0,2), (0,3), (1,2)
                ) { row, col, expected ->
                    val input = """
                        .@@@
                        @@@.
                    """.trimIndent()
                    val grid = parseGrid(input)
                    
                    countAdjacentRolls(grid, row, col) shouldBe expected
                }
            }
        }
        
        When("checking edge cases") {
            Then("it should handle positions with no adjacent rolls") {
                val input = """
                    @...
                    ....
                    ....
                    ...@
                """.trimIndent()
                val grid = parseGrid(input)
                
                countAdjacentRolls(grid, 0, 0) shouldBe 0
                countAdjacentRolls(grid, 3, 3) shouldBe 0
            }
        }
    }
    
    Given("the example grid") {
        val exampleInput = """
            ..@@.@@@@.
            @@@.@.@.@@
            @@@@@.@.@@
            @.@@@@..@.
            @@.@@@@.@@
            .@@@@@@@.@
            .@.@.@.@@@
            @.@@@.@@@@
            .@@@@@@@@.
            @.@.@@@.@.
        """.trimIndent()
        
        When("marking accessible rolls") {
            val grid = parseGrid(exampleInput)
            val markedGrid = markAccessibleRolls(grid)
            
            Then("it should mark rolls with less than 4 adjacent rolls") {
                // Convert to string for easier comparison
                val markedString = markedGrid.joinToString("\n") { it.joinToString("") }
                val expectedOutput = """
                    ..xx.xx@x.
                    x@@.@.@.@@
                    @@@@@.x.@@
                    @.@@@@..@.
                    x@.@@@@.@x
                    .@@@@@@@.@
                    .@.@.@.@@@
                    x.@@@.@@@@
                    .@@@@@@@@.
                    x.x.@@@.x.
                """.trimIndent()
                
                markedString shouldBe expectedOutput
            }
        }
        
        When("counting accessible rolls") {
            val grid = parseGrid(exampleInput)
            val markedGrid = markAccessibleRolls(grid)
            val count = countAccessibleRolls(markedGrid)
            
            Then("it should count 13 accessible rolls") {
                count shouldBe 13
            }
        }
        
        When("solving the complete example") {
            val result = solveDay04Part1(exampleInput)
            
            Then("it should return 13") {
                result shouldBe 13
            }
        }
    }
})
