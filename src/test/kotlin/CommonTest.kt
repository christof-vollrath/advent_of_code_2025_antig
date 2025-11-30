import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.datatest.withData

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

