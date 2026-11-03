package acktsap.fp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ResourceTest : StringSpec({
    "useShouldKeepNestedResourcesAliveForTheEntireEffectAndReleaseInReverseOrder" {
        val events = mutableListOf<String>()
        val first =
            Resource.make(
                IO.delay {
                    events.add("acquire first")
                    "first"
                },
            ) { name -> IO.delay<Unit> { events.add("release $name") } }
        val second =
            Resource.make(
                IO.delay {
                    events.add("acquire second")
                    "second"
                },
            ) { name -> IO.delay<Unit> { events.add("release $name") } }
        val program =
            first.flatMap { outer -> second.map { inner -> outer to inner } }
                .use { (outer, inner) ->
                    IO.delay { events.add("use $outer") }
                        .flatMap {
                            IO.delay {
                                events.add("use $inner")
                                42
                            }
                        }
                }

        events shouldBe emptyList()
        val actual = program.unsafeRunSync()

        actual shouldBe 42
        events shouldBe listOf("acquire first", "acquire second", "use first", "use second", "release second", "release first")
    }

    "useShouldReleaseOuterResourceWhenInnerAcquisitionFails" {
        val failure = IllegalStateException()
        val events = mutableListOf<String>()
        val outer = Resource.make(IO.pure("outer")) { IO.delay<Unit> { events.add("release outer") } }
        val inner = Resource.make(IO.raiseError<String>(failure)) { IO.delay<Unit> { events.add("release inner") } }

        val actual = outer.flatMap { inner }.use { IO.pure(it) }.attempt().unsafeRunSync()

        actual shouldBe failure.left()
        events shouldBe listOf("release outer")
    }

    "useShouldReleaseAfterInterruptionWithoutConvertingItToARecoverableError" {
        val interruption = InterruptedException()
        val releases = mutableListOf<Unit>()
        val resource = Resource.make(IO.pure(Unit)) { IO.delay<Unit> { releases.add(Unit) } }
        val program = resource.use { IO.raiseError<Nothing>(interruption) }.attempt()

        val actual = shouldThrow<InterruptedException> { program.unsafeRunSync() }

        actual shouldBe interruption
        releases shouldBe listOf(Unit)
    }

    "useShouldReleaseIfTheEffectBuilderThrowsBeforeReturningIO" {
        val failure = IllegalStateException()
        val releases = mutableListOf<Unit>()
        val resource = Resource.make(IO.pure(Unit)) { IO.delay<Unit> { releases.add(Unit) } }

        val actual = resource.use<Nothing> { throw failure }.attempt().unsafeRunSync()

        actual shouldBe failure.left()
        releases shouldBe listOf(Unit)
    }

    "flatMapShouldSupportDeepResourceScopesWithoutStackOverflow" {
        val acquisitions = mutableListOf<Int>()
        val releases = mutableListOf<Unit>()
        val resource =
            (1..10_000).fold(Resource.pure(0)) { accumulated, _ ->
                accumulated.flatMap { count ->
                    Resource.make(
                        IO.delay {
                            acquisitions.add(count)
                            count + 1
                        },
                    ) { IO.delay<Unit> { releases.add(Unit) } }
                }
            }

        val actual = resource.use { IO.pure(it) }.unsafeRunSync()

        actual shouldBe 10_000
        acquisitions shouldBe (0 until 10_000).toList()
        releases.size shouldBe 10_000
    }
    "useShouldPropagateReleaseInterruptionAndStillReleaseOuterResources" {
        val useFailure = IllegalStateException()
        val interruption = InterruptedException()
        val outerReleases = mutableListOf<Unit>()
        val outer = Resource.make(IO.pure(Unit)) { IO.delay<Unit> { outerReleases.add(Unit) } }
        val inner = Resource.make(IO.pure(Unit)) { IO.raiseError(interruption) }
        val program = outer.flatMap { inner }.use { IO.raiseError<Nothing>(useFailure) }.attempt()

        val actual = shouldThrow<InterruptedException> { program.unsafeRunSync() }

        actual shouldBe interruption
        actual.suppressed.toList() shouldBe listOf(useFailure)
        outerReleases shouldBe listOf(Unit)
    }
})
