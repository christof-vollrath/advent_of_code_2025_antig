import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

fun isInvalidId(input: String): Boolean {
    if (input.length % 2 != 0) {
        return false
    }
    val mid = input.length / 2
    val firstHalf = input.substring(0, mid)
    val secondHalf = input.substring(mid)
    return firstHalf == secondHalf
}

fun isInvalidId(input: Long): Boolean {
    return isInvalidId(input.toString())
}

fun sumInvalidIdsInRange(range: String): Long {
    val parts = range.split("-")
    val start = parts[0].toLong()
    val end = parts[1].toLong()
    var sum = 0L
    for (i in start..end) {
        if (isInvalidId(i)) {
            sum += i
        }
    }
    return sum
}

fun sumInvalidIdsInRanges(input: String): Long {
    return input.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .sumOf { sumInvalidIdsInRange(it) }
}

class Day02Part1Test : BehaviorSpec({
    given("an int id") {
        `when`("checking if it is invalid") {
            then("it should correctly identify invalid and valid ids") {
                forAll(
                    row(55L, true),
                    row(6464L, true),
                    row(123123L, true),
                    row(11L, true),
                    row(1212L, true),
                    row(123L, false),
                    row(1213L, false),
                    row(56L, false),
                    row(121212L, false) // 121 != 212
                ) { input, expected ->
                    isInvalidId(input) shouldBe expected
                }
            }
        }
    }

    given("a range of ids") {
        `when`("summing invalid ids") {
            then("it should return the correct sum") {
                forAll(
                    row("11-22", 33L),
                    row("1188511880-1188511890", 1188511885L),
                    row("1698522-1698528", 0L)
                ) { range, expected ->
                    sumInvalidIdsInRange(range) shouldBe expected
                }
            }
        }
    }

    given("multiple ranges of ids") {
        `when`("summing invalid ids") {
            then("it should return the correct sum for the complex example") {
                val input = "11-22,95-115,998-1012,1188511880-1188511890,222220-222224," +
                        "1698522-1698528,446443-446449,38593856-38593862,565653-565659," +
                        "824824821-824824827,2121212118-2121212124"
                sumInvalidIdsInRanges(input) shouldBe 1227775554L
            }
        }
    }

    given("the puzzle input") {
        `when`("calculating the sum of invalid ids") {
            then("it should print the result") {
                val input = readResource("day02Input.txt")!!
                val result = sumInvalidIdsInRanges(input)
                println("Result for Day 2 Part 1: $result")
                result shouldBe 28146997880L // Placeholder, will update after run
            }
        }
    }
})

