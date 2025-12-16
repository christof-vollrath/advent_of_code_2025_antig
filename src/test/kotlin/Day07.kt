import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.BehaviorSpec

class Day07 {
    data class Result(val map: List<String>, val splits: Int)

    fun solve(input: List<String>): Result {
        if (input.isEmpty()) return Result(emptyList(), 0)
        val height = input.size
        val width = input[0].length
        val outputGrid = input.map { it.toCharArray() }.toTypedArray()
        
        var startR = -1
        var startC = -1
        for (r in 0 until height) {
            for (c in 0 until width) {
                if (input[r][c] == 'S') {
                    startR = r
                    startC = c
                    break
                }
            }
        }
        
        if (startR == -1) return Result(input, 0)

        // Queue holds the position of the beam head.
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(startR to startC)
        
        // Visited set for BEAM HEAD POSITIONS to handle merges.
        val visited = mutableSetOf<Pair<Int, Int>>()
        visited.add(startR to startC)

        // Set to track unique splitters hit (by location)
        val hitSplitters = mutableSetOf<Pair<Int, Int>>()

        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()
            
            // Beam at (r,c) tries to move DOWN
            val nextR = r + 1
            if (nextR >= height) continue
            
            val targetChar = input[nextR][c]
            
            if (targetChar == '^') {
                // Hit a splitter
                hitSplitters.add(nextR to c)
                
                // Spawn left
                val leftC = c - 1
                if (leftC >= 0) {
                    if (outputGrid[nextR][leftC] == '.') {
                        outputGrid[nextR][leftC] = '|'
                    }
                    if (visited.add(nextR to leftC)) {
                        queue.add(nextR to leftC)
                    }
                }
                
                // Spawn right
                val rightC = c + 1
                if (rightC < width) {
                    if (outputGrid[nextR][rightC] == '.') {
                        outputGrid[nextR][rightC] = '|'
                    }
                    if (visited.add(nextR to rightC)) {
                        queue.add(nextR to rightC)
                    }
                }
            } else {
                // Determine if we move into it (., S, |)
                if (targetChar == '.' || targetChar == '|') {
                     if (outputGrid[nextR][c] == '.') {
                        outputGrid[nextR][c] = '|'
                    }
                }
                // Valid move (pass through)
                 if (visited.add(nextR to c)) {
                    queue.add(nextR to c)
                }
            }
        }
        
        return Result(outputGrid.map { String(it) }, hitSplitters.size)
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
