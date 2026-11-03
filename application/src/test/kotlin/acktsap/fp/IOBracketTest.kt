package acktsap.fp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class IOBracketTest : StringSpec({
    "bracketShouldAcquireAndReleaseOncePerExecution" {
        val events = mutableListOf<String>()
        val program =
            IO.bracket(
                acquire = IO.delay { events.add("acquire") },
                release = { IO.delay<Unit> { events.add("release") } },
                use = {
                    IO.delay {
                        events.add("use")
                        42
                    }
                },
            )

        events shouldBe emptyList()
        val actual = program.unsafeRunSync()

        actual shouldBe 42
        events shouldBe listOf("acquire", "use", "release")
    }

    "bracketShouldPreserveTheUseFailureAndSuppressTheReleaseFailure" {
        val useFailure = IllegalStateException()
        val releaseFailure = IllegalArgumentException()
        val program =
            IO.bracket(
                acquire = IO.pure(Unit),
                release = { IO.raiseError(releaseFailure) },
                use = { IO.raiseError<Nothing>(useFailure) },
            )

        val actual = program.attempt().unsafeRunSync()

        actual shouldBe useFailure.left()
        useFailure.suppressed.toList() shouldBe listOf(releaseFailure)
    }

    "bracketShouldNotReleaseWhenAcquisitionFails" {
        val failure = IllegalStateException()
        val releases = mutableListOf<Unit>()
        val program =
            IO.bracket(
                acquire = IO.raiseError<Nothing>(failure),
                release = { IO.delay<Unit> { releases.add(Unit) } },
                use = { IO.pure(Unit) },
            )

        val actual = program.attempt().unsafeRunSync()

        actual shouldBe failure.left()
        releases shouldBe emptyList()
    }

    "bracketShouldSurfaceReleaseFailureAfterSuccessfulUse" {
        val failure = IllegalStateException()
        val program = IO.bracket(acquire = IO.pure(Unit), release = { IO.raiseError(failure) }, use = { IO.pure(42) })

        val actual = program.attempt().unsafeRunSync()

        actual shouldBe failure.left()
    }
})
