import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.*

val exampleInputDay10 = """
[.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
[...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
[.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}""".trimIndent()


class Day10Part1Test : BehaviorSpec({
    val examples = exampleInputDay10.split("\n")

    Given("the Day 10 example machines") {
        val examplesAndResults = listOf(
            examples[0] to 2,
            examples[1] to 3,
            examples[2] to 2
        )
        examplesAndResults.forEachIndexed { index, (input, expected) ->
            When("calculating for machine ${index + 1}") {
                val machine = parseMachine(input)
                val presses = solveMinPresses(machine)
                Then("it should return $expected") {
                    presses shouldBe expected
                }
            }
        }
        When("calculating the sum for all machines") {
            val results = examples.map { solveMinPresses(parseMachine(it)) ?: 0 }
            Then("sum should be right") {
                results.sum() shouldBe 7
            }
        }
    }
    Given("puzzle input") {
        val input = readResource("day10Input.txt")!!.split("\n")
        When("calculating the solution for each machine and summing the number of presses") {
            val results = input.map { solveMinPresses(parseMachine(it)) ?: 0 }
            val sum = results.sum()
            Then("sum should be right") {
                println("Day 10 Part 1 Result: $sum")
                sum shouldBe 447
            }
        }
    }
})

data class Machine(
    val target: BitSet,
    val numLights: Int,
    val buttons: List<BitSet>,
    val requirements: List<Int>
)

fun parseMachine(input: String): Machine {
    val targetStr = input.substringAfter("[").substringBefore("]")
    val target = BitSet(targetStr.length).apply {
        targetStr.forEachIndexed { i, c -> if (c == '#') set(i) }
    }

    val buttons = Regex("""\((.*?)\)""").findAll(input)
        .map { match ->
            BitSet(targetStr.length).apply {
                match.groupValues[1].split(",")
                    .filter { it.isNotBlank() }
                    .forEach { set(it.trim().toInt()) }
            }
        }.toList()

    val requirements = Regex("""\{(.*?)}""").find(input)
        ?.groupValues?.get(1)
        ?.split(",")
        ?.map { it.trim().toInt() }
        ?: emptyList()

    return Machine(target, targetStr.length, buttons, requirements)
}

fun solveMinPresses(machine: Machine): Int? {
    val n = machine.numLights
    val m = machine.buttons.size
    // Augmented matrix: n rows (lights), m+1 columns (buttons + target)
    val matrix = Array(n) { BitSet(m + 1) }
    for (i in 0 until n) {
        for (j in 0 until m) {
            if (machine.buttons[j].get(i)) {
                matrix[i].set(j)
            }
        }
        if (machine.target.get(i)) {
            matrix[i].set(m)
        }
    }

    // Gaussian elimination to RREF
    var pivotRow = 0
    val pivotCols = mutableListOf<Int>()
    for (j in 0 until m) {
        if (pivotRow >= n) break
        var sel = pivotRow
        while (sel < n && !matrix[sel].get(j)) {
            sel++
        }
        if (sel == n) continue

        // Swap rows
        val temp = matrix[sel]
        matrix[sel] = matrix[pivotRow]
        matrix[pivotRow] = temp

        pivotCols.add(j)

        // Eliminate other entries in column j
        for (i in 0 until n) {
            if (i != pivotRow && matrix[i].get(j)) {
                matrix[i].xor(matrix[pivotRow])
            }
        }
        pivotRow++
    }

    // Check for consistency
    for (i in pivotRow until n) {
        if (matrix[i].get(m)) return null // Inconsistent
    }

    // Identify free variables
    val isPivotCol = BooleanArray(m)
    pivotCols.forEach { isPivotCol[it] = true }
    val freeCols = (0 until m).filter { !isPivotCol[it] }

    if (freeCols.isEmpty()) {
        // Unique solution
        var count = 0
        for (i in 0 until pivotRow) {
            if (matrix[i].get(m)) count++
        }
        return count
    }

    // Bitwise optimization for exhaustive search
    val pivotTargets = pivotCols.indices.map { i -> matrix[i].get(m) }
    val dependencyMasks = pivotCols.indices.map { i ->
        var mask = 0L
        freeCols.forEachIndexed { fi, fCol ->
            if (matrix[i].get(fCol)) mask = mask or (1L shl fi)
        }
        mask
    }

    var minWeight = Int.MAX_VALUE
    val numCombinations = 1L shl freeCols.size
    for (bits in 0 until numCombinations) {
        var currentWeight = java.lang.Long.bitCount(bits)
        if (currentWeight >= minWeight) continue

        for (i in pivotCols.indices) {
            val valP = pivotTargets[i] xor (java.lang.Long.bitCount(bits and dependencyMasks[i]) % 2 == 1)
            if (valP) currentWeight++
            if (currentWeight >= minWeight) break
        }
        
        if (currentWeight < minWeight) {
            minWeight = currentWeight
        }
    }

    return minWeight
}

