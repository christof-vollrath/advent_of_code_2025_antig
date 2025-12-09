import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe


fun parseFreshRanges(input: String): List<LongRange> =
    input.substringBefore("\n\n").lines().map { line ->
        val parts = line.split("-")
        parts[0].toLong()..parts[1].toLong()
    }

fun parseAvailableIds(input: String): List<Long> =
    input.substringAfter("\n\n").lines().filter { it.isNotEmpty() }.map { it.toLong() }

fun countFreshIngredients(input: String): Int =
    countFreshIngredients(parseFreshRanges(input), parseAvailableIds(input))

fun countFreshIngredients(ranges: List<LongRange>, ids: List<Long>): Int =
    ids.count { id -> isFresh(id, ranges) }

fun isFresh(id: Long, ranges: List<LongRange>): Boolean =
    ranges.any { range -> id in range }

class Day05Part1Test : BehaviorSpec({
    Given("Day 5 Part 1 Example") {
        val input = """
            3-5
            10-14
            16-20
            12-18

            1
            5
            8
            11
            17
            32
        """.trimIndent()

        When("parsing fresh ranges") {
            val ranges = parseFreshRanges(input)
            Then("it should return correct ranges") {
                ranges shouldBe listOf(
                    3L..5L,
                    10L..14L,
                    16L..20L,
                    12L..18L
                )
            }
        }

        When("parsing available IDs") {
            val ids = parseAvailableIds(input)
            Then("it should return correct IDs") {
                ids shouldBe listOf(1L, 5L, 8L, 11L, 17L, 32L)
            }
        }

        When("counting fresh ingredients") {
            val count = countFreshIngredients(input)
            Then("it should return 3") {
                count shouldBe 3
            }
        }
    }

    Given("Day 5 Part 1 Puzzle Input") {
        val input = readResource("day05Input.txt")!!
        When("counting fresh ingredients") {
            val count = countFreshIngredients(input)
            Then("it should return the correct count") {
                count shouldBe 848
            }
        }
    }
})
