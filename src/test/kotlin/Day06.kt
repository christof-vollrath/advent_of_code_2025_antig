
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe


fun solveDay06Part1(input: List<String>): Long {
    val problems = parseProblems(input)
    return problems.sumOf { it.solve() }
}

fun solveDay06Part2(input: List<String>): Long {
    val problems = parseProblems2(input)
    return problems.sumOf { it.solve() }
}

data class Problem(val numbers: List<Long>, val operator: Char) {
    fun solve(): Long {
        return if (operator == '+') numbers.sum()
        else numbers.reduce { acc, l -> acc * l }
    }
}

fun parseProblems(input: List<String>): List<Problem> {
    if (input.isEmpty()) return emptyList()
    
    val numberLines = input.dropLast(1)
    val operatorLine = input.last()
    
    val rows = numberLines.map { line ->
        line.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.map { it.toLong() }
    }
    
    val columns = rows.transpose()
    val operators = operatorLine.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.map { it.first() }
    
    return columns.zip(operators).map { (nums, op) ->
        Problem(nums, op)
    }
}

fun parseProblems2(input: List<String>): List<Problem> {
    if (input.isEmpty()) return emptyList()
    
    val width = input.maxOf { it.length }
    val numberLines = input.dropLast(1)
    val operatorLine = input.last()
    
    val isSpaceColumn = BooleanArray(width) { col ->
        input.all { line -> 
           col >= line.length || line[col] == ' '
        }
    }
    
    val problems = mutableListOf<Problem>()
    var currentEnd = width - 1
    
    // Scan right to left
    for (col in (width - 1) downTo 0) {
        if (isSpaceColumn[col]) {
             if (currentEnd != -1 && col < currentEnd) {
                 problems.add(extractProblem2(numberLines, operatorLine, col + 1, currentEnd))
             }
             currentEnd = col - 1
        }
    }
     // Handle leftmost problem
    if (currentEnd != -1) {
        problems.add(extractProblem2(numberLines, operatorLine, 0, currentEnd))
    }
    
    return problems
}

fun extractProblem2(numberLines: List<String>, operatorLine: String, startCol: Int, endCol: Int): Problem {
    val numbers = mutableListOf<Long>()
    
    // For each column right to left
    for (col in endCol downTo startCol) {
        val sb = StringBuilder()
        // Top to bottom
        for (line in numberLines) {
           if (col < line.length && line[col] != ' ') {
               sb.append(line[col])
           }
        }
        if (sb.isNotEmpty()) {
            numbers.add(sb.toString().toLong())
        }
    }
    
    // Find operator in this range
    var operator = ' '
     for (col in startCol..endCol) {
         if (col < operatorLine.length && operatorLine[col] != ' ') {
             operator = operatorLine[col]
             break
         }
     }
    
    return Problem(numbers, operator)
}


class Day06Part1Test : BehaviorSpec({
    Given("day 6 part 1 example") {
        val exampleInput = """
            123 328  51 64 
             45 64  387 23 
              6 98  215 314
            *   +   *   +  
        """.trimIndent().lines()

        When("parsing the problems") {
            val problems = parseProblems(exampleInput)
            Then("it should parse 4 problems correctly") {
                problems shouldBe listOf(
                    Problem(listOf(123, 45, 6), '*'),
                    Problem(listOf(328, 64, 98), '+'),
                    Problem(listOf(51, 387, 215), '*'),
                    Problem(listOf(64, 23, 314), '+')
                )
            }
        }

        When("calculating grand total") {
            val result = solveDay06Part1(exampleInput)
            Then("result should be 4277556") {
                result shouldBe 4277556
            }
        }
        }

    Given("day 6 part 2 example") {
         val exampleInput = """
            123 328  51 64 
             45 64  387 23 
              6 98  215 314
            *   +   *   +  
        """.trimIndent().lines()

        When("parsing part 2 problems") {
            val problems = parseProblems2(exampleInput)
            Then("it should parse problems correctly right-to-left") {
                 // Rightmost: 4 + 431 + 623
                 problems[0] shouldBe Problem(listOf(4, 431, 623), '+')
                 // Second from right: 175 * 581 * 32
                 problems[1] shouldBe Problem(listOf(175, 581, 32), '*')
                 // Third from right: 8 + 248 + 369
                 problems[2] shouldBe Problem(listOf(8, 248, 369), '+')
                 // Leftmost: 356 * 24 * 1
                 problems[3] shouldBe Problem(listOf(356, 24, 1), '*')
            }
        }
        
        When("calculating grand total for part 2") {
            val result = solveDay06Part2(exampleInput)
            Then("result should be 3263827") {
                result shouldBe 3263827
            }
        }
    }

    Given("puzzle input") {
        val input = readResource("day06Input.txt")!!.trim().lines()
        When("calculating grand total") {
            val result = solveDay06Part1(input)
            Then("result should be correct") {
                result shouldBe 4722948564882L
            }
        }

        When("calculating grand total for part 2") {
            val result = solveDay06Part2(input)
            Then("result should be correct for part 2") {
                result shouldBe 9581313737063L
            }
        }
    }
})
