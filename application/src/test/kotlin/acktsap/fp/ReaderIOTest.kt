package acktsap.fp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class ReaderIOTest : StringSpec({
    "ReaderIOShouldSatisfyMonadLawsWhileSharingTheSameEnvironment" {
        checkAll(Arb.int(), Arb.int()) { value, environment ->
            val program = ReaderIO.ask<Int>().map { it + value }
            val first: (Int) -> ReaderIO<Int, Int> = { input -> ReaderIO.ask<Int>().map { it + input } }
            val second: (Int) -> ReaderIO<Int, Int> = { input -> ReaderIO.ask<Int>().map { it * input } }

            val leftIdentity = ReaderIO.pure<Int, Int>(value).flatMap(first).run(environment).unsafeRunSync()
            val rightIdentity = program.flatMap { ReaderIO.pure<Int, Int>(it) }.run(environment).unsafeRunSync()
            val associated = program.flatMap(first).flatMap(second).run(environment).unsafeRunSync()
            val reassociated = program.flatMap { first(it).flatMap(second) }.run(environment).unsafeRunSync()

            leftIdentity shouldBe first(value).run(environment).unsafeRunSync()
            rightIdentity shouldBe program.run(environment).unsafeRunSync()
            associated shouldBe reassociated
        }
    }

    "localShouldTransformOnlyItsOwnScope" {
        checkAll(Arb.int()) { environment ->
            val program =
                ReaderIO.ask<Int>().local<Int> { it * 2 }
                    .flatMap { inner -> ReaderIO.ask<Int>().map { outer -> inner to outer } }

            val actual = program.run(environment).unsafeRunSync()

            actual shouldBe (environment * 2 to environment)
        }
    }

    "runShouldDeferEnvironmentAccessAndEffectsUntilIOExecution" {
        val environmentReads = mutableListOf<Int>()
        val effects = mutableListOf<Int>()
        val program =
            ReaderIO<Int, Int> { environment ->
                environmentReads.add(environment)
                IO.delay {
                    effects.add(environment)
                    environment
                }
            }
        val description = program.run(42)

        environmentReads shouldBe emptyList()
        effects shouldBe emptyList()
        description.unsafeRunSync() shouldBe 42
        description.unsafeRunSync() shouldBe 42
        environmentReads shouldBe listOf(42, 42)
        effects shouldBe listOf(42, 42)
    }

    "flatMapShouldRemainStackSafeForDeepEnvironmentDependentComposition" {
        val program =
            (1..100_000).fold(ReaderIO.pure<Int, Int>(0)) { accumulated, _ ->
                accumulated.flatMap { value -> ReaderIO.ask<Int>().map { value + it } }
            }

        val actual = program.run(1).unsafeRunSync()

        actual shouldBe 100_000
    }
})
