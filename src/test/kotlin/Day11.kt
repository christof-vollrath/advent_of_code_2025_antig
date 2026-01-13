import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class Day11 {
    fun parseInput(input: String): Map<String, List<String>> =
        input.lines()
            .filter { it.isNotBlank() }
            .map { it.split(":") }
            .associate { (device, outputs) ->
                device.trim() to outputs.trim().split(" ").filter { it.isNotBlank() }
            }

    fun countPaths(graph: Map<String, List<String>>, start: String, end: String): Int {
        val memo = mutableMapOf<String, Int>()

        fun dfs(current: String): Int {
            if (current == end) return 1
            if (current in memo) return memo[current]!!

            val outputs = graph[current] ?: return 0
            val paths = outputs.sumOf { dfs(it) }
            memo[current] = paths
            return paths
        }

        return dfs(start)
    }

    fun countPathsWithConstraints(
        graph: Map<String, List<String>>,
        start: String,
        end: String,
        required: Set<String>
    ): Long {
        val memo = mutableMapOf<Pair<String, Set<String>>, Long>()

        fun dfs(current: String, visitedRequired: Set<String>): Long {
            val nextVisitedRequired = if (current in required) visitedRequired + current else visitedRequired
            
            if (current == end) {
                return if (nextVisitedRequired.size == required.size) 1L else 0L
            }
            
            val state = current to nextVisitedRequired
            if (state in memo) return memo[state]!!

            val outputs = graph[current] ?: return 0L
            val paths = outputs.sumOf { dfs(it, nextVisitedRequired) }
            memo[state] = paths
            return paths
        }

        return dfs(start, emptySet())
    }
}

class Day11Part1Test : BehaviorSpec({
    val exampleInput = """
        aaa: you hhh
        you: bbb ccc
        bbb: ddd eee
        ccc: ddd eee fff
        ddd: ggg
        eee: out
        fff: out
        ggg: out
        hhh: ccc fff iii
        iii: out
    """.trimIndent()

    Given("the example input") {
        val day11 = Day11()
        val graph = day11.parseInput(exampleInput)

        When("counting paths from you to out") {
            val pathCount = day11.countPaths(graph, "you", "out")

            Then("it should find 5 paths") {
                pathCount shouldBe 5
            }
        }
    }

    Given("the puzzle input") {
        val day11 = Day11()
        val input = readResource("day11Input.txt") ?: throw Exception("Input not found")
        val graph = day11.parseInput(input)

        When("counting paths from you to out") {
            val pathCount = day11.countPaths(graph, "you", "out")

            Then("it should find the correct number of paths") {
                pathCount shouldBe 796
            }
        }
    }
})

class Day11Part2Test : BehaviorSpec({
    val exampleInput = """
        svr: aaa bbb
        aaa: fft
        fft: ccc
        bbb: tty
        tty: ccc
        ccc: ddd eee
        ddd: hub
        hub: fff
        eee: dac
        dac: fff
        fff: ggg hhh
        ggg: out
        hhh: out
    """.trimIndent()

    Given("the example input") {
        val day11 = Day11()
        val graph = day11.parseInput(exampleInput)

        When("counting paths from svr to out visiting dac and fft") {
            val pathCount = day11.countPathsWithConstraints(graph, "svr", "out", setOf("dac", "fft"))

            Then("it should find 2 paths") {
                pathCount shouldBe 2
            }
        }
    }

    Given("the puzzle input") {
        val day11 = Day11()
        val input = readResource("day11Input.txt") ?: throw Exception("Input not found")
        val graph = day11.parseInput(input)

        When("counting paths from svr to out visiting dac and fft") {
            val pathCount = day11.countPathsWithConstraints(graph, "svr", "out", setOf("dac", "fft"))

            Then("it should find the correct number of paths") {
                pathCount shouldBe 294053029111296L
            }
        }
    }
})
