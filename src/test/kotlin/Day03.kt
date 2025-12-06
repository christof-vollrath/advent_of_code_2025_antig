import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

fun largestJoltage(bank: String): Int {
    if (bank.length < 2) return 0
    
    var maxJoltage = 0
    
    // Try all pairs of batteries (positions i and j where i < j)
    for (i in 0 until bank.length - 1) {
        for (j in i + 1 until bank.length) {
            val joltage = "${bank[i]}${bank[j]}".toInt()
            if (joltage > maxJoltage) {
                maxJoltage = joltage
            }
        }
    }
    
    return maxJoltage
}

fun largestJoltageFunctional(bank: String): Int {
    if (bank.length < 2) return 0
    
    // Find all positions with their digits, excluding the last position (needs a digit after)
    val candidatePositions = bank.dropLast(1).withIndex()
    
    // Find the position with the largest digit (that has at least one digit after it)
    val firstPos = candidatePositions.maxByOrNull { it.value }?.index ?: return 0
    val maxDigit = bank[firstPos]
    
    // Find the largest digit that comes after this position
    val remainingDigits = bank.substring(firstPos + 1)
    val secondDigit = remainingDigits.maxOrNull() ?: return 0
    
    return "$maxDigit$secondDigit".toInt()
}

fun sumLargestJoltage(input: String) =
    input.split("\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }.sumOf { largestJoltageFunctional(it) }

class Day03Part1Test : BehaviorSpec({
    Given("a bank of batteries") {
        When("finding the largest joltage") {
            Then("it should return the correct maximum joltage") {
                forAll(
                    row("987654321111111", 98),
                    row("811111111111119", 89),
                    row("234234234234278", 78),
                    row("818181911112111", 92)
                ) { bank, expected ->
                    largestJoltage(bank) shouldBe expected
                    largestJoltageFunctional(bank) shouldBe expected
                }
            }
        }
    }
    Given("example input") {
        val example = """
            987654321111111
            811111111111119
            234234234234278
            818181911112111""".trimIndent()
        When("summing the joltage") {
            val result = sumLargestJoltage(example)
            Then("it should have calculated the correct sum") {
                result shouldBe 357
            }
        }
    }
    
    Given("the puzzle input") {
        val input = readResource("day03Input.txt")!!
        When("calculating the sum of largest joltages") {
            val result = sumLargestJoltage(input)
            Then("it should print the result") {
                println("Result for Day 3 Part 1: $result")
                result shouldBe 16973
            }
        }
    }
})

class Day03Part1PerformanceTest : BehaviorSpec({
    Given("a large bank of batteries") {
        When("comparing performance of both approaches") {
            Then("functional approach should be faster") {
                // Create a large test input
                val largeBank = (1..1000).joinToString("") { (it % 10).toString() }

                // Warm up
                repeat(100) {
                    largestJoltage(largeBank)
                    largestJoltageFunctional(largeBank)
                }
                
                // Benchmark brute force approach
                val bruteForceStart = System.nanoTime()
                repeat(1000) {
                    largestJoltage(largeBank)
                }
                val bruteForceTime = System.nanoTime() - bruteForceStart
                
                // Benchmark functional approach
                val functionalStart = System.nanoTime()
                repeat(1000) {
                    largestJoltageFunctional(largeBank)
                }
                val functionalTime = System.nanoTime() - functionalStart
                
                println("Performance comparison (1000 iterations on ${largeBank.length} digits):")
                println("  Brute force: ${bruteForceTime / 1_000_000.0} ms")
                println("  Functional:  ${functionalTime / 1_000_000.0} ms")
                println("  Speedup:     ${bruteForceTime.toDouble() / functionalTime}x")
                
                // Verify both give same result
                largestJoltage(largeBank) shouldBe largestJoltageFunctional(largeBank)
            }
        }
    }
})
