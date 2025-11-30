import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CommonTest_readResource : StringSpec({
    "readResource should return content of existing resource" {
        readResource("test_resource.txt") shouldBe "Hello, World!"
    }

    "readResource should return null for non-existent resource" {
        readResource("non_existent.txt") shouldBe null
    }
})

