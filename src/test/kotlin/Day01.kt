import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.datatest.withData

// Day 1: Safe Dial Rotation
// The safe has a dial with numbers 0-99 in a circle
// Rotations: L (left/lower) or R (right/higher) followed by distance
// Wraps around: L from 0 goes to 99, R from 99 goes to 0

enum class Direction {
    LEFT, RIGHT;
    
    companion object {
        fun fromChar(c: Char): Direction = when (c) {
            'L' -> LEFT
            'R' -> RIGHT
            else -> throw IllegalArgumentException("Invalid direction: $c")
        }
    }
}

data class Rotation(val direction: Direction, val distance: Int)

fun parseRotation(input: String): Rotation {
    val direction = Direction.fromChar(input[0])
    val distance = input.substring(1).toInt()
    return Rotation(direction, distance)
}

fun parseRotations(input: String): List<Rotation> {
    return input.lines()
        .filter { it.isNotBlank() }
        .map { parseRotation(it) }
}

fun applyRotation(currentPosition: Int, rotation: Rotation): Int {
    return when (rotation.direction) {
        Direction.RIGHT -> (currentPosition + rotation.distance) % 100
        Direction.LEFT -> {
            val newPos = currentPosition - rotation.distance
            if (newPos >= 0) newPos else (100 + (newPos % 100)) % 100
        }
    }
}

fun applyRotations(startPosition: Int, rotations: List<Rotation>) = applyRotationsWithZeroCount(startPosition, rotations).finalPosition

data class DialResult(val finalPosition: Int, val zeroCount: Int)

fun applyRotationsWithZeroCount(startPosition: Int, rotations: List<Rotation>): DialResult {
    var position = startPosition
    var zeroCount = 0
    
    for (rotation in rotations) {
        position = applyRotation(position, rotation)
        if (position == 0) {
            zeroCount++
        }
    }
    
    return DialResult(position, zeroCount)
}

class Day01Part1Test : BehaviorSpec({
    Given("a safe dial with numbers 0-99") {
        When("starting at 11 and rotating R8") {
            Then("should point at 19") {
                applyRotation(11, Rotation(Direction.RIGHT, 8)) shouldBe 19
            }
        }

        When("starting at 19 and rotating L19") {
            Then("should point at 0") {
                applyRotation(19, Rotation(Direction.LEFT, 19)) shouldBe 0
            }
        }

        When("starting at 5 and rotating L10") {
            Then("should point at 95") {
                applyRotation(5, Rotation(Direction.LEFT, 10)) shouldBe 95
            }
        }

        When("starting at 95 and rotating R5") {
            Then("should point at 0") {
                applyRotation(95, Rotation(Direction.RIGHT, 5)) shouldBe 0
            }
        }

        When("wrapping around from 0 left by 1") {
            Then("should point at 99") {
                applyRotation(0, Rotation(Direction.LEFT, 1)) shouldBe 99
            }
        }

        When("wrapping around from 99 right by 1") {
            Then("should point at 0") {
                applyRotation(99, Rotation(Direction.RIGHT, 1)) shouldBe 0
            }
        }
    }

    Given("test case 1: starting at 11") {
        val rotations = listOf(
            Rotation(Direction.RIGHT, 8),
            Rotation(Direction.LEFT, 19)
        )

        When("applying R8 then L19") {
            Then("should end at 0") {
                applyRotations(11, rotations) shouldBe 0
            }
        }
    }

    Given("test case 2: starting at 5") {
        val rotations = listOf(
            Rotation(Direction.LEFT, 10),
            Rotation(Direction.RIGHT, 5)
        )

        When("applying L10 then R5") {
            Then("should end at 0") {
                applyRotations(5, rotations) shouldBe 0
            }
        }
    }

    Given("parsing rotation strings") {
        data class ParseTestCase(
            val input: String,
            val expected: Rotation
        )

        withData(
            nameFn = { "parsing '${it.input}'" },
            ParseTestCase("R8", Rotation(Direction.RIGHT, 8)),
            ParseTestCase("L19", Rotation(Direction.LEFT, 19)),
            ParseTestCase("L10", Rotation(Direction.LEFT, 10)),
            ParseTestCase("R5", Rotation(Direction.RIGHT, 5)),
            ParseTestCase("R99", Rotation(Direction.RIGHT, 99)),
            ParseTestCase("L0", Rotation(Direction.LEFT, 0))
        ) { testCase ->
            parseRotation(testCase.input) shouldBe testCase.expected
        }
    }

    Given("parsing multiple rotations from string") {
        When("given a multi-line string with rotations") {
            val input = """
                L68
                L30
                R48
            """.trimIndent()

            Then("should parse all rotations correctly") {
                val rotations = parseRotations(input)
                rotations shouldBe listOf(
                    Rotation(Direction.LEFT, 68),
                    Rotation(Direction.LEFT, 30),
                    Rotation(Direction.RIGHT, 48)
                )
            }
        }

        When("given a string with blank lines") {
            val input = """
                L10
                
                R20
                
            """.trimIndent()

            Then("should skip blank lines") {
                val rotations = parseRotations(input)
                rotations shouldBe listOf(
                    Rotation(Direction.LEFT, 10),
                    Rotation(Direction.RIGHT, 20)
                )
            }
        }
    }

    Given("a sequence that never hits 0") {
        val rotations = listOf(
            Rotation(Direction.RIGHT, 10),
            Rotation(Direction.LEFT, 5)
        )

        Then("zero count should be 0") {
            applyRotationsWithZeroCount(50, rotations).zeroCount shouldBe 0
        }
    }

    Given("a sequence that hits 0 multiple times") {
        val rotations = listOf(
            Rotation(Direction.LEFT, 50),  // 50 -> 0
            Rotation(Direction.RIGHT, 100), // 0 -> 0
            Rotation(Direction.LEFT, 100)   // 0 -> 0
        )

        Then("zero count should be 3") {
            applyRotationsWithZeroCount(50, rotations).zeroCount shouldBe 3
        }
    }

    Given("R1000") {
        val rotations = listOf(
            Rotation(Direction.RIGHT, 1000),
        )

        Then("zero count should be 0") {
            applyRotationsWithZeroCount(50, rotations).zeroCount shouldBe 0
        }
    }

    Given("example") {
        When("starting at 50 with the example sequence") {
            val input = """
                L68
                L30
                R48
                L5
                R60
                L55
                L1
                L99
                R14
                L82
            """.trimIndent()

            Then("final position should be 32 and password (zero count) should be 3") {
                val rotations = parseRotations(input)
                val (finalPosition, zeroCount) = applyRotationsWithZeroCount(50, rotations)
                finalPosition shouldBe 32
                zeroCount shouldBe 3
            }
        }
    }

    Given("puzzle input") {
        When("reading from day01input.txt") {
            val input = readResource("day01input.txt")!!

            Then("should calculate the correct password") {
                val rotations = parseRotations(input)
                val zeroCount = applyRotationsWithZeroCount(50, rotations).zeroCount
                zeroCount shouldBe 980
            }
        }
    }
})
