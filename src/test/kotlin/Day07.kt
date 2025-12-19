import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.BehaviorSpec

data class Result(val map: List<String>, val splits: Int)

fun simulateTachyonBeam(input: List<String>): Result {
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

fun countTimelines(input: List<String>): Long {
    if (input.isEmpty()) return 0L
    val height = input.size
    val width = input[0].length

    val start = input.withIndex().firstNotNullOfOrNull { (r, row) ->
        val c = row.indexOf('S')
        if (c != -1) Coord2(c, r) else null
    } ?: return 1L

    var currentWays = LongArray(width)
    currentWays[start.x] = 1L

    for (row in start.y until height - 1) {
        val nextRowWays = LongArray(width)
        val nextR = row + 1
        for (col in 0 until width) {
            val count = currentWays[col]
            if (count == 0L) continue

            val charBelow = input[nextR][col]
            if (charBelow == '^') {
                if (col > 0) nextRowWays[col - 1] += count
                if (col < width - 1) nextRowWays[col + 1] += count
            } else {
                nextRowWays[col] += count
            }
        }
        currentWays = nextRowWays
    }

    return currentWays.sum()
}

val exampleInputDay07: List<String> = """
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

class Day07Part1Test : BehaviorSpec({

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

    Given("Day 7 Part 1 input") {

        When("simulating tachyon beam for the start of example") {
            val result = simulateTachyonBeam(exampleInputDay07.take(2))

            Then("it produces the expected map with no splits") {
                result.map shouldBe expectedOutput.take(2)
            }

            Then("it counts correct number of splits") {
                result.splits shouldBe 0
            }
        }

        When("simulating tachyon beam for one split") {
            val result = simulateTachyonBeam(exampleInputDay07.take(4))

            Then("it produces the expected map with no splits") {
                result.map shouldBe expectedOutput.take(4)
            }

            Then("it counts correct number of splits") {
                result.splits shouldBe 1
            }
        }

        When("simulating tachyon beam when beams are merged") {
            val result = simulateTachyonBeam(exampleInputDay07.take(8))

            Then("it produces the expected map with no splits") {
                result.map shouldBe expectedOutput.take(8)
            }

            Then("it counts correct number of splits") {
                result.splits shouldBe 6
            }
        }

        When("simulating tachyon beam for the example") {
            val result = simulateTachyonBeam(exampleInputDay07)

            Then("it produces the expected map with beams") {
                result.map shouldBe expectedOutput
            }
            
            Then("it counts correct number of splits") {
                result.splits shouldBe 21
            }
        }
        When("puzzle input is provided") {
            val input = readResource("day07Input.txt")!!.lines()
            val result = simulateTachyonBeam(input)

            Then("it counts splits") {
                println("Splits in puzzle input: ${result.splits}")
                result.splits shouldBe 1656
            }
        }
    }
})

class Day07Part2Test : BehaviorSpec({

    Given("Day 7 Part 2 input") {
        When("simulating timelines for the start of example") {
            val timelines = countTimelines(exampleInputDay07.take(2))
            Then("it should be 1") {
                timelines shouldBe 1L
            }
        }

        When("simulating timelines for one split") {
            val timelines = countTimelines(exampleInputDay07.take(4))
            Then("it should be 2") {
                timelines shouldBe 2L
            }
        }

        When("simulating timelines for merged beams section") {
            val timelines = countTimelines(exampleInputDay07.take(8))
            Then("it should be 8") {
                timelines shouldBe 8L
            }
        }

        When("simulating timelines for the full example") {
            val timelines = countTimelines(exampleInputDay07)
            Then("it should be 40") {
                timelines shouldBe 40L
            }
        }

        When("puzzle input is provided") {
            val input = readResource("day07Input.txt")!!.lines()
            val timelines = countTimelines(input)

            Then("it counts timelines") {
                timelines shouldBe 76624086587804L
            }
        }
    }
})
