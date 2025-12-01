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

fun applyRotation(currentPosition: Int, rotation: Rotation): Int {
    return when (rotation.direction) {
        Direction.RIGHT -> (currentPosition + rotation.distance) % 100
        Direction.LEFT -> {
            val newPos = currentPosition - rotation.distance
            if (newPos >= 0) newPos else (100 + (newPos % 100)) % 100
        }
    }
}

fun applyRotations(startPosition: Int, rotations: List<Rotation>): Int {
    return rotations.fold(startPosition) { position, rotation ->
        applyRotation(position, rotation)
    }
}

fun solvePart1(input: String): Int {
    val rotations = input.lines()
        .filter { it.isNotBlank() }
        .map { parseRotation(it) }
    return applyRotations(0, rotations)
}

class Day01Test : BehaviorSpec({
    given("a safe dial with numbers 0-99") {
        `when`("starting at 11 and rotating R8") {
            then("should point at 19") {
                applyRotation(11, Rotation(Direction.RIGHT, 8)) shouldBe 19
            }
        }

        `when`("starting at 19 and rotating L19") {
            then("should point at 0") {
                applyRotation(19, Rotation(Direction.LEFT, 19)) shouldBe 0
            }
        }

        `when`("starting at 5 and rotating L10") {
            then("should point at 95") {
                applyRotation(5, Rotation(Direction.LEFT, 10)) shouldBe 95
            }
        }

        `when`("starting at 95 and rotating R5") {
            then("should point at 0") {
                applyRotation(95, Rotation(Direction.RIGHT, 5)) shouldBe 0
            }
        }

        `when`("wrapping around from 0 left by 1") {
            then("should point at 99") {
                applyRotation(0, Rotation(Direction.LEFT, 1)) shouldBe 99
            }
        }

        `when`("wrapping around from 99 right by 1") {
            then("should point at 0") {
                applyRotation(99, Rotation(Direction.RIGHT, 1)) shouldBe 0
            }
        }
    }

    given("test case 1: starting at 11") {
        val rotations = listOf(
            Rotation(Direction.RIGHT, 8),
            Rotation(Direction.LEFT, 19)
        )

        `when`("applying R8 then L19") {
            then("should end at 0") {
                applyRotations(11, rotations) shouldBe 0
            }
        }
    }

    given("test case 2: starting at 5") {
        val rotations = listOf(
            Rotation(Direction.LEFT, 10),
            Rotation(Direction.RIGHT, 5)
        )

        `when`("applying L10 then R5") {
            then("should end at 0") {
                applyRotations(5, rotations) shouldBe 0
            }
        }
    }

    given("parsing rotation strings") {
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
})
