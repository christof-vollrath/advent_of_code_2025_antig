import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.math.min

/**
 * Ford-Johnson Algorithm (Merge-Insertion Sort)
 *
 * Designed to minimize comparisons.
 *
 * My prompts to Antigravity/Gemini3Flash
 *
 * Can you explain to me the Ford-Johnson-algoirthm for sorting?
 *
 * Please implement this Alogrithm. Write it in a file called Sort.kt under test and include some tests.
 */

private data class Node<T : Comparable<T>>(val winner: T, val loser: T) : Comparable<Node<T>> {
    override fun compareTo(other: Node<T>): Int = this.winner.compareTo(other.winner)
}

fun <T : Comparable<T>> fordJohnsonSort(list: List<T>): List<T> {
    if (list.size <= 1) return list
    return sortRecursive(list)
}

private fun <T : Comparable<T>> sortRecursive(list: List<T>): List<T> {
    if (list.size <= 1) return list

    // 1. Group into pairs
    val pairs = list.chunked(2).filter { it.size == 2 }
    val nodes = pairs.map { 
        val (a, b) = it
        if (a > b) Node(a, b) else Node(b, a)
    }
    val oddElement = if (list.size % 2 != 0) list.last() else null

    // 2. Recursively sort the winners
    val sortedNodes = sortRecursive(nodes)
    val sortedWinners = sortedNodes.map { it.winner }

    // 3. Create the initial chain starting with L1 followed by all sorted winners
    val mainChain = mutableListOf(sortedNodes[0].loser)
    mainChain.addAll(sortedWinners)

    // 4. Prepare pending elements: losers L2..Lk and the odd element
    data class PendingInsertion(val element: T, val winner: T?)
    val pending = mutableListOf<PendingInsertion>()
    for (i in 1 until sortedNodes.size) {
        pending.add(PendingInsertion(sortedNodes[i].loser, sortedNodes[i].winner))
    }
    if (oddElement != null) {
        pending.add(PendingInsertion(oddElement, null))
    }

    if (pending.isEmpty()) return mainChain

    // 5. Insert pending elements in Jacobsthal groups
    val jacobsthalIndices = generateJacobsthalSequence(pending.size + 1)
    
    var lastJacobsthal = 1
    for (j in jacobsthalIndices.drop(2)) {
        if (lastJacobsthal >= pending.size + 1) break
        
        val end = min(pending.size + 1, j)
        val start = lastJacobsthal + 1
        
        for (k in end downTo start) {
            val toInsert = pending[k - 2]
            val searchBound = if (toInsert.winner != null) {
                mainChain.indexOf(toInsert.winner)
            } else {
                mainChain.size
            }
            
            val insertPos = mainChain.subList(0, searchBound).binarySearch(toInsert.element).let {
                if (it < 0) -it - 1 else it
            }
            mainChain.add(insertPos, toInsert.element)
        }
        lastJacobsthal = j
    }

    return mainChain
}

private fun generateJacobsthalSequence(max: Int): List<Int> {
    val seq = mutableListOf(0, 1)
    while (seq.last() < max) {
        seq.add(seq[seq.size - 1] + 2 * seq[seq.size - 2])
    }
    return seq
}

class SortTest : BehaviorSpec({
    Given("The Ford-Johnson sorting algorithm") {
        When("sorting various lists") {
            val testCases = mapOf(
                "empty list" to emptyList<Int>(),
                "single element" to listOf(42),
                "two elements unsorted" to listOf(10, 5),
                "two elements sorted" to listOf(5, 10),
                "three elements" to listOf(3, 1, 2),
                "five elements (classic)" to listOf(5, 4, 3, 1, 2),
                "already sorted" to (1..10).toList(),
                "reverse sorted" to (10 downTo 1).toList(),
                "duplicates" to listOf(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5),
                "larger random" to List(50) { (0..100).random() }
            )

            testCases.forEach { (name, list) ->
                Then("it should correctly sort $name") {
                    val actual = fordJohnsonSort(list)
                    actual shouldBe list.sorted()
                    actual.size shouldBe list.size
                }
            }
        }
    }
})

