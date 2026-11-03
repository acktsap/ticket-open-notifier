package acktsap.fp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import kotlinx.collections.immutable.toPersistentList

class ValidatedTest : StringSpec({
    "zipShouldAccumulateAllIndependentFailuresInInputOrder" {
        checkAll(Arb.boolean(), Arb.boolean(), Arb.boolean()) { missingFirst, missingSecond, missingThird ->
            val first = Validated.required(1.takeUnless { missingFirst }, "first")
            val second = Validated.required(2.takeUnless { missingSecond }, "second")
            val third = Validated.required(3.takeUnless { missingThird }, "third")
            val expected =
                listOfNotNull(
                    "first".takeIf { missingFirst },
                    "second".takeIf { missingSecond },
                    "third".takeIf { missingThird },
                )

            val actual = first.zip(second, Int::plus).zip(third, Int::plus).toEither()

            actual shouldBe if (expected.isEmpty()) 6.right() else expected.toPersistentList().left()
        }
    }

    "zipShouldNotExecuteTheCombinerWhenAnInputIsInvalid" {
        val first = Validated.required<Int, Int>(null, 42)
        val second = Validated.required(1, 99)

        val actual = first.zip(second) { _, _ -> error("Invalid inputs must not be combined") }.toEither()

        actual shouldBe listOf(42).toPersistentList().left()
    }
})
