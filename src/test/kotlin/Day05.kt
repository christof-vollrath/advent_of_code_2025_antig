import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInput = """
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

fun mergeRanges(ranges: List<LongRange>): List<LongRange> = sequence {
    if (ranges.isEmpty()) return@sequence
    val sorted = ranges.sortedBy { it.first }
    var current = sorted[0]
    for (next in sorted.drop(1)) {
        if (current.last >= next.first - 1) {
            current = current.first..maxOf(current.last, next.last)
        } else {
            yield(current)
            current = next
        }
    }
    yield(current)
}.toList()

fun countAllFreshIngredients(input: String): Long =
    countAllFreshIngredients(parseFreshRanges(input))

fun countAllFreshIngredients(ranges: List<LongRange>): Long =
    mergeRanges(ranges).sumOf { it.last - it.first + 1 }

class Day05Part1Test : BehaviorSpec({
    Given("Day 5 Part 1 Example") {

        When("parsing fresh ranges") {
            val ranges = parseFreshRanges(exampleInput)
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
            val ids = parseAvailableIds(exampleInput)
            Then("it should return correct IDs") {
                ids shouldBe listOf(1L, 5L, 8L, 11L, 17L, 32L)
            }
        }

        When("counting fresh ingredients") {
            val count = countFreshIngredients(exampleInput)
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

class Day05Part2Test : BehaviorSpec({
    Given("Day 5 Part 2 Example") {
        When("merging ranges") {
            val ranges = parseFreshRanges(exampleInput)
            val merged = mergeRanges(ranges)
            Then("it should return merged ranges") {
                merged shouldBe listOf(3L..5L, 10L..20L)
                // Let's re-read example carefully.
                // 10-14, 12-18, 16-20
                // 10-14 overlaps 12-18 (12,13,14) -> 10-18
                // 10-18 overlaps 16-20 (16,17,18) -> 10-20
                // So merged should be 3-5, 10-20
                // Let's adjust expectation based on manual trace or just implement logic and see.
                // The example says: "The ingredient IDs that these ranges consider to be fresh are 3, 4, 5, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, and 20."
                // 3,4,5 -> 3-5 (count 3)
                // 10..20 -> 11 numbers (10,11,12,13,14,15,16,17,18,19,20)
                // Total 14.
            }
        }

        When("counting all fresh ingredients") {
            val count = countAllFreshIngredients(exampleInput)
            Then("it should return 14") {
                count shouldBe 14
            }
        }
    }

    Given("Day 5 Part 2 Puzzle Input") {
        val input = readResource("day05Input.txt")!!
        When("counting all fresh ingredients") {
            val count = countAllFreshIngredients(input)
            Then("it should return the correct count") {
                count shouldBe 334714395325710L
            }
        }
    }
})
