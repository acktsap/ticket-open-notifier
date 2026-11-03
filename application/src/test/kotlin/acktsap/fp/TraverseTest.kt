package acktsap.fp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

class TraverseTest : StringSpec({
    "traverseIOShouldDeferConstructionAndExecuteInSnapshotOrder" {
        checkAll(Arb.list(Arb.int(), 0..30)) { values ->
            val input = values.toMutableList()
            val visited = mutableListOf<Int>()
            val constructed = mutableListOf<Int>()
            val program =
                input.traverseIO { value ->
                    constructed.add(value)
                    IO.delay {
                        visited.add(value)
                        value * 2
                    }
                }
            input.clear()

            constructed shouldBe emptyList()
            visited shouldBe emptyList()
            val actual = program.unsafeRunSync()

            actual shouldBe values.map { it * 2 }
            visited shouldBe values
            constructed shouldBe values
        }
    }

    "traverseIOShouldStopConstructingEffectsAfterTheFirstFailure" {
        checkAll(Arb.int(0..20)) { failureIndex ->
            val failure = IllegalStateException()
            val constructed = mutableListOf<Int>()
            val program =
                (0..30).traverseIO { value ->
                    constructed.add(value)
                    if (value == failureIndex) IO.raiseError(failure) else IO.pure(value)
                }

            val actual = program.attempt().unsafeRunSync()

            actual shouldBe failure.left()
            constructed shouldBe (0..failureIndex).toList()
        }
    }

    "traverseIOShouldRemainStackSafeForManyEffects" {
        val program = (1..100_000).traverseIO { IO.pure(it) }

        val actual = program.unsafeRunSync()

        actual.size shouldBe 100_000
        actual.first() shouldBe 1
        actual.last() shouldBe 100_000
    }
})
