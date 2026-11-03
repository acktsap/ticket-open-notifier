package acktsap.fp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class IOTest : StringSpec({
    "delayShouldDeferAndRepeatEffectsForEachExecution" {
        val executions = mutableListOf<Unit>()
        val program =
            IO.delay {
                executions.add(Unit)
                executions.size
            }.map { it * 2 }

        executions shouldBe emptyList()
        program.unsafeRunSync() shouldBe 2
        program.unsafeRunSync() shouldBe 4
    }

    "flatMapShouldSatisfyLeftIdentity" {
        checkAll(Arb.int(), Arb.int()) { value, offset ->
            val next: (Int) -> IO<Int> = { IO.delay { it + offset } }

            val actual = IO.pure(value).flatMap(next).unsafeRunSync()

            actual shouldBe next(value).unsafeRunSync()
        }
    }

    "flatMapShouldSatisfyRightIdentity" {
        checkAll(Arb.int()) { value ->
            val program = IO.delay { value }

            val actual = program.flatMap { IO.pure(it) }.unsafeRunSync()

            actual shouldBe program.unsafeRunSync()
        }
    }

    "flatMapShouldPreserveAssociativityIncludingEffectOrderAndFailures" {
        checkAll(Arb.int(), Arb.int(0..3)) { value, failureStage ->
            val failure = IllegalStateException()
            val events = mutableListOf<String>()
            val program =
                IO.delay {
                    events.add("source")
                    if (failureStage == 0) throw failure
                    value
                }
            val first: (Int) -> IO<Int> = { input ->
                IO.delay {
                    events.add("first")
                    if (failureStage == 1) throw failure
                    input + 1
                }
            }
            val second: (Int) -> IO<Int> = { input ->
                IO.delay {
                    events.add("second")
                    if (failureStage == 2) throw failure
                    input * 2
                }
            }

            val actual = program.flatMap(first).flatMap(second).attempt().unsafeRunSync()
            val actualTrace = events.toList()
            events.clear()
            val reassociated = program.flatMap { first(it).flatMap(second) }.attempt().unsafeRunSync()

            actual shouldBe reassociated
            actualTrace shouldBe events
        }
    }

    "flatMapShouldExecuteDeepCompositionWithoutStackOverflow" {
        val program = (1..100_000).fold(IO.pure(0)) { accumulated, _ -> accumulated.flatMap { IO.pure(it + 1) } }

        val actual = program.unsafeRunSync()

        actual shouldBe 100_000
    }

    "attemptShouldCaptureFailureAndSkipSubsequentEffects" {
        val failure = IllegalStateException()
        val subsequentExecutions = mutableListOf<Unit>()
        val program = IO.raiseError<Int>(failure).flatMap { IO.delay { subsequentExecutions.add(Unit) } }.attempt()

        val actual = program.unsafeRunSync()

        actual shouldBe failure.left()
        subsequentExecutions shouldBe emptyList()
    }

    "attemptShouldHandleDeeplyNestedErrorBoundariesWithoutStackOverflow" {
        val program =
            (1..100_000).fold(IO.pure(0)) { accumulated, _ ->
                accumulated.attempt().map { result -> result.fold({ throw it }, { it + 1 }) }
            }

        val actual = program.unsafeRunSync()

        actual shouldBe 100_000
    }

    "attemptShouldRecoverWithinItsOwnBoundaryAndAllowFollowingEffects" {
        val failure = IllegalStateException()
        val program = IO.raiseError<Int>(failure).attempt().flatMap { IO.pure(it == failure.left()) }.attempt()

        val actual = program.unsafeRunSync()

        actual shouldBe Either.Right(true)
    }
    "attemptAndGuaranteeShouldRespectTheirNestingWhenTheFinalizerFails" {
        val failure = IllegalArgumentException()
        val finalizer = IO.raiseError<Unit>(failure)

        IO.pure<String?>(null).guarantee(IO.pure(Unit)).attempt().unsafeRunSync() shouldBe null.right()
        IO.pure(42).guarantee(finalizer).attempt().unsafeRunSync() shouldBe failure.left()
        IO.pure(42).attempt().guarantee(finalizer).attempt().unsafeRunSync() shouldBe failure.left()
    }

    "guaranteeShouldUnwindDeepFailureScopesInOrderWithoutStackOverflow" {
        val events = mutableListOf<Int>()
        val failure = IllegalStateException()
        val program =
            (0 until 100_000).fold(IO.raiseError<Unit>(failure)) { effect, index ->
                effect.guarantee(IO.delay<Unit> { events.add(index) })
            }

        program.attempt().unsafeRunSync() shouldBe failure.left()
        events shouldBe (0 until 100_000).toList()
    }
})
