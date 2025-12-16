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

        // Queue holds the position of the beam head.
        val queue = ArrayDeque<Coord2>()
        queue.add(start)
        
        // Visited set for BEAM HEAD POSITIONS to handle merges.
        val visited = mutableSetOf<Coord2>()
        visited.add(start)

        // Set to track unique splitters hit (by location)
        val hitSplitters = mutableSetOf<Coord2>()

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val r = current.y
            val c = current.x
            
            // Beam at (r,c) tries to move DOWN
            val nextR = r + 1
            if (nextR >= height) continue
            
            val targetChar = input[nextR][c]
            
            if (targetChar == '^') {
                // Hit a splitter
                hitSplitters.add(Coord2(c, nextR))
                
                // Spawn left
                val leftC = c - 1
                if (leftC >= 0) {
                    if (outputGrid[nextR][leftC] == '.') {
                        outputGrid[nextR][leftC] = '|'
                    }
                    val leftPos = Coord2(leftC, nextR)
                    if (visited.add(leftPos)) {
                        queue.add(leftPos)
                    }
                }
                
                // Spawn right
                val rightC = c + 1
                if (rightC < width) {
                    if (outputGrid[nextR][rightC] == '.') {
                        outputGrid[nextR][rightC] = '|'
                    }
                    val rightPos = Coord2(rightC, nextR)
                    if (visited.add(rightPos)) {
                        queue.add(rightPos)
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
                val nextPos = Coord2(c, nextR)
                 if (visited.add(nextPos)) {
                    queue.add(nextPos)
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
