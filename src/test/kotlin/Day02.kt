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
    Given("an int id") {
        When("checking if it is invalid") {
            Then("it should correctly identify invalid and valid ids") {
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

    Given("a range of ids") {
        When("summing invalid ids") {
            Then("it should return the correct sum") {
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

    Given("multiple ranges of ids") {
        When("summing invalid ids") {
            Then("it should return the correct sum for the complex example") {
                val input = "11-22,95-115,998-1012,1188511880-1188511890,222220-222224," +
                        "1698522-1698528,446443-446449,38593856-38593862,565653-565659," +
                        "824824821-824824827,2121212118-2121212124"
                sumInvalidIdsInRanges(input) shouldBe 1227775554L
            }
        }
    }

    Given("the puzzle input") {
        When("calculating the sum of invalid ids") {
            Then("it should print the result") {
                val input = readResource("day02Input.txt")!!
                val result = sumInvalidIdsInRanges(input)
                println("Result for Day 2 Part 1: $result")
                result shouldBe 28146997880L // Placeholder, will update after run
            }
        }
    }
})

fun isInvalidId2(input: Long): Boolean {
    return isInvalidId2(input.toString())
}
fun isInvalidId2(input: String): Boolean {
    val len = input.length
    return divisors(len)
        .filter { it != len } // Exclude the number itself
        .any { patternLength ->
            val pattern = input.substring(0, patternLength)
            val repeated = pattern.repeat(len / patternLength)
            input == repeated
        }
}

fun sumInvalidIdsInRange2(range: String): Long {
    val parts = range.split("-")
    val start = parts[0].toLong()
    val end = parts[1].toLong()
    var sum = 0L
    for (i in start..end) {
        if (isInvalidId2(i)) {
            sum += i
        }
    }
    return sum
}

fun sumInvalidIdsInRanges2(input: String): Long {
    return input.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .sumOf { sumInvalidIdsInRange2(it) }
}

class Day02Part2Test : BehaviorSpec({
    Given("an int id") {
        When("checking if it is invalid with the new rule") {
            Then("it should correctly identify invalid and valid ids") {
                forAll(
                    row(12341234L, true),
                    row(123123123L, true),
                    row(1212121212L, true),
                    row(1111111L, true),
                    row(12345L, false),
                    row(1212121L, false), // 7 chars, 12 repeated 3.5 times
                    row(123123124L, false)
                ) { input, expected ->
                    isInvalidId2(input) shouldBe expected
                }
            }
        }
    }

    Given("multiple ranges of ids") {
        When("summing invalid ids with the new rule") {
            Then("it should return the correct sum for the complex example") {
                val input = "11-22,95-115,998-1012,1188511880-1188511890,222220-222224," +
                        "1698522-1698528,446443-446449,38593856-38593862,565653-565659," +
                        "824824821-824824827,2121212118-2121212124"
                sumInvalidIdsInRanges2(input) shouldBe 4174379265L
            }
        }
    }

    Given("the puzzle input") {
        When("calculating the sum of invalid ids with the new rule") {
            Then("it should print the result") {
                val input = readResource("day02Input.txt")!!
                val result = sumInvalidIdsInRanges2(input)
                println("Result for Day 2 Part 2: $result")
                result shouldBe 40028128307L
            }
        }
    }
})
