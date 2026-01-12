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

class Day10Part2Test : BehaviorSpec({
    val examples = exampleInputDay10.split("\n")

    Given("the Day 10 example machines for Part 2") {
        val examplesAndResults = listOf(
            examples[0] to 10,
            examples[1] to 12,
            examples[2] to 11
        )
        examplesAndResults.forEachIndexed { index, (input, expected) ->
            When("calculating for machine ${index + 1}") {
                val machine = parseMachine(input)
                val presses = solveMinPressesPart2(machine)
                Then("it should return $expected") {
                    presses shouldBe expected
                }
            }
        }
        When("calculating the sum for all machines") {
            val results = examples.map { solveMinPressesPart2(parseMachine(it)) ?: 0 }
            Then("sum should be 33") {
                results.sum() shouldBe 33
            }
        }
    }

    Given("puzzle input for Part 2") {
        val input = readResource("day10Input.txt")?.split("\n")?.filter { it.isNotBlank() }
        if (input != null) {
            When("calculating the total presses for all machines") {
                val results = input.map { solveMinPressesPart2(parseMachine(it)) ?: 0 }
                val sum = results.sum()
                Then("sum should be right") {
                    println("Day 10 Part 2 Result: $sum")
                sum shouldBe 18960
                }
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

/**
 * Solves Part 1: Minimum button presses to match indicator light pattern.
 * 
 * DESIGN DECISION: Gaussian Elimination over GF(2)
 * Toggling lights is equivalent to addition in the finite field GF(2) (XOR logic).
 * A machine configuration can be represented as a system of linear equations: Ax = b
 * where A is the matrix of button effects, x is the vector of button presses (0 or 1), 
 * and b is the target light configuration.
 * 
 * We use Gaussian Elimination to reach Reduced Row Echelon Form (RREF). 
 * This approach is chosen because:
 * 1. It handles redundancy: If buttons are linearly dependent, RREF identifies the basis.
 * 2. It identifies free variables: If the system is underdetermined, we can search 
 *    the small space of free variables to find the solution with the minimum Hamming weight (fewest presses).
 */
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

/**
 * Solves Part 2: Minimum button presses to match exact joltage levels.
 * 
 * DESIGN DECISION: Gaussian Elimination + Targeted Search
 * Unlike Part 1, Part 2 involves standard integer addition (joltage increases by 1).
 * The problem is a linear system Ax = b over non-negative integers.
 * 
 * Why Gaussian Elimination instead of BFS?
 * - BFS (Breadth-First Search) is ideal for finding the shortest path in state-space.
 * - However, with joltage targets reaching ~200 across 8 counters, the state space 
 *   ($200^8$) is too large for memory (leads to OutOfMemoryError).
 * - Linear Algebra (RREF) reduces the problem from searching states to searching 
 *   the relationships between buttons. 
 * - Since the number of buttons and counters is small, RREF simplifies the system 
 *   to a few free variables, even when target values are large.
 */
fun solveMinPressesPart2(machine: Machine): Int? {
    val b = machine.requirements.map { it.toDouble() }.toDoubleArray()
    if (b.isEmpty()) return 0
    val n = b.size
    val m = machine.buttons.size
    
    // Augmented matrix
    val matrix = Array(n) { DoubleArray(m + 1) }
    for (i in 0 until n) {
        for (j in 0 until m) {
            if (machine.buttons[j].get(i)) matrix[i][j] = 1.0
        }
        matrix[i][m] = b[i]
    }
    
    // RREF
    var pivotCount = 0
    val pivotCols = IntArray(n) { -1 }
    for (j in 0 until m) {
        if (pivotCount >= n) break
        var sel = pivotCount
        while (sel < n && Math.abs(matrix[sel][j]) < 1e-9) sel++
        if (sel == n) continue
        
        val temp = matrix[sel]
        matrix[sel] = matrix[pivotCount]
        matrix[pivotCount] = temp
        
        val div = matrix[pivotCount][j]
        for (k in j..m) matrix[pivotCount][k] /= div
        
        for (i in 0 until n) {
            if (i != pivotCount) {
                val factor = matrix[i][j]
                for (k in j..m) matrix[i][k] -= factor * matrix[pivotCount][k]
            }
        }
        pivotCols[pivotCount] = j
        pivotCount++
    }
    
    // Consistency check
    for (i in pivotCount until n) {
        if (Math.abs(matrix[i][m]) > 1e-9) return null
    }
    
    val isPivot = BooleanArray(m)
    for (i in 0 until pivotCount) isPivot[pivotCols[i]] = true
    val freeCols = (0 until m).filter { !isPivot[it] }
    
    var minSum = Int.MAX_VALUE
    
    fun search(freeIdx: Int, freeVals: IntArray) {
        if (freeIdx == freeCols.size) {
            // Check pivot variables
            var currentSum = freeVals.sum()
            for (i in 0 until pivotCount) {
                var pivotVal = matrix[i][m]
                for (fi in freeCols.indices) {
                    pivotVal -= matrix[i][freeCols[fi]] * freeVals[fi]
                }
                val rounded = Math.round(pivotVal).toInt()
                if (Math.abs(rounded - pivotVal) > 1e-6 || rounded < 0) return
                currentSum += rounded
            }
            if (currentSum < minSum) minSum = currentSum
            return
        }
        
        // Brute force range for free variable.
        // Usually, 0..max(b) is safe.
        val maxB = b.maxOrNull()?.toInt() ?: 0
        for (v in 0..maxB) {
            freeVals[freeIdx] = v
            search(freeIdx + 1, freeVals)
        }
    }
    
    
    search(0, IntArray(freeCols.size))
    
    return if (minSum == Int.MAX_VALUE) null else minSum
}

