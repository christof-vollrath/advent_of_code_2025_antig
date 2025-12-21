import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.datatest.withData
import kotlin.math.sqrt


class CommonTest_readResource : StringSpec({
    "readResource should return content of existing resource" {
        readResource("test_resource.txt") shouldBe "Hello, World!"
    }

    "readResource should return null for non-existent resource" {
        readResource("non_existent.txt") shouldBe null
    }
})

class CommonTest_transpose : StringSpec({
    data class TransposeTestCase(
        val name: String,
        val input: List<List<Int>>,
        val expected: List<List<Int>>
    )

    withData(
        nameFn = { it.name },
        TransposeTestCase(
            name = "empty list",
            input = emptyList(),
            expected = emptyList()
        ),
        TransposeTestCase(
            name = "1x1 matrix",
            input = listOf(
                listOf(42)
            ),
            expected = listOf(
                listOf(42)
            )
        ),
        TransposeTestCase(
            name = "single row",
            input = listOf(
                listOf(1, 2, 3, 4)
            ),
            expected = listOf(
                listOf(1),
                listOf(2),
                listOf(3),
                listOf(4)
            )
        ),
        TransposeTestCase(
            name = "single column",
            input = listOf(
                listOf(1),
                listOf(2),
                listOf(3)
            ),
            expected = listOf(
                listOf(1, 2, 3)
            )
        ),
        TransposeTestCase(
            name = "2x3 matrix",
            input = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6)
            ),
            expected = listOf(
                listOf(1, 4),
                listOf(2, 5),
                listOf(3, 6)
            )
        ),
        TransposeTestCase(
            name = "3x2 matrix",
            input = listOf(
                listOf(1, 2),
                listOf(3, 4),
                listOf(5, 6)
            ),
            expected = listOf(
                listOf(1, 3, 5),
                listOf(2, 4, 6)
            )
        ),
        TransposeTestCase(
            name = "3x3 square matrix",
            input = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6),
                listOf(7, 8, 9)
            ),
            expected = listOf(
                listOf(1, 4, 7),
                listOf(2, 5, 8),
                listOf(3, 6, 9)
            )
        ),
        TransposeTestCase(
            name = "double transpose returns original",
            input = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6)
            ),
            expected = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6)
            )
        )
    ) { testCase ->
        if (testCase.name == "double transpose returns original") {
            testCase.input.transpose().transpose() shouldBe testCase.expected
        } else {
            testCase.input.transpose() shouldBe testCase.expected
        }
    }
})

class CommonTest_divisors : StringSpec({
    data class DivisorsTestCase(
        val name: String,
        val input: Int,
        val expected: List<Int>
    )

    withData(
        nameFn = { it.name },
        DivisorsTestCase(
            name = "divisors of 1",
            input = 1,
            expected = listOf(1)
        ),
        DivisorsTestCase(
            name = "divisors of 2 (prime)",
            input = 2,
            expected = listOf(1, 2)
        ),
        DivisorsTestCase(
            name = "divisors of 12",
            input = 12,
            expected = listOf(1, 2, 3, 4, 6, 12)
        ),
        DivisorsTestCase(
            name = "divisors of 28 (perfect number)",
            input = 28,
            expected = listOf(1, 2, 4, 7, 14, 28)
        ),
        DivisorsTestCase(
            name = "divisors of 36 (perfect square)",
            input = 36,
            expected = listOf(1, 2, 3, 4, 6, 9, 12, 18, 36)
        ),
        DivisorsTestCase(
            name = "divisors of 17 (prime)",
            input = 17,
            expected = listOf(1, 17)
        ),
        DivisorsTestCase(
            name = "divisors of 100",
            input = 100,
            expected = listOf(1, 2, 4, 5, 10, 20, 25, 50, 100)
        ),
        DivisorsTestCase(
            name = "divisors of 60",
            input = 60,
            expected = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
        )
    ) { testCase ->
        divisors(testCase.input).toList() shouldBe testCase.expected
    }
})


class CommonTest_coord3 : StringSpec({
    data class DistanceTestCase(
        val name: String,
        val c1: Coord3,
        val c2: Coord3,
        val expected: Double
    )

    withData(
        nameFn = { it.name },
        DistanceTestCase("zero distance", Coord3(0, 0, 0), Coord3(0, 0, 0), 0.0),
        DistanceTestCase("x-axis", Coord3(0, 0, 0), Coord3(3, 0, 0), 3.0),
        DistanceTestCase("y-axis", Coord3(0, 0, 0), Coord3(0, 4, 0), 4.0),
        DistanceTestCase("z-axis", Coord3(0, 0, 0), Coord3(0, 0, 5), 5.0),
        DistanceTestCase("diagonal", Coord3(0, 0, 0), Coord3(3, 4, 0), 5.0),
        DistanceTestCase("3d diagonal", Coord3(0, 0, 0), Coord3(1, 2, 2), 3.0),
        DistanceTestCase("origin to all ones", Coord3(0, 0, 0), Coord3(1, 1, 1), sqrt(3.0)),
                DistanceTestCase("negative coords", Coord3(-1, -1, -1), Coord3(1, 1, 1), sqrt(12.0))
    ) { testCase ->
        (testCase.c1 euclideanDistance testCase.c2) shouldBe testCase.expected
    }
})
