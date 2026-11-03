package acktsap.fp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.util.concurrent.CancellationException

class EitherTest : StringSpec({
    "flatMapShouldSatisfyMonadLawsForSuccessAndFailure" {
        checkAll(Arb.int(), Arb.boolean()) { value, fail ->
            val program: Either<String, Int> = if (fail) "source".left() else value.right()
            val first: (Int) -> Either<String, Int> = { if (it % 2 == 0) (it + 1).right() else "first".left() }
            val second: (Int) -> Either<String, Int> = { if (it % 3 == 0) "second".left() else (it * 2).right() }

            value.right().flatMap(first) shouldBe first(value)
            program.flatMap { it.right() } shouldBe program
            program.flatMap(first).flatMap(second) shouldBe program.flatMap { first(it).flatMap(second) }
        }
    }

    "mapAndFlatMapShouldSkipSuccessCallbacksForLeft" {
        val failure: Either<String, Int> = "failure".left()

        val mapped: Either<String, Int> = failure.map { error("Success callback must not execute") }
        val bound: Either<String, Int> = failure.flatMap { error("Success callback must not execute") }

        mapped shouldBe failure
        bound shouldBe failure
        failure.mapLeft(String::uppercase) shouldBe "FAILURE".left()
    }

    "catchOrThrowShouldCaptureOnlyTheSelectedRecoverableException" {
        val expected = IllegalArgumentException()
        val unexpected = IllegalStateException()

        val actual = Either.catchOrThrow<IllegalArgumentException, Int> { throw expected }
        val propagated =
            shouldThrow<IllegalStateException> {
                Either.catchOrThrow<IllegalArgumentException, Int> { throw unexpected }
            }

        actual shouldBe expected.left()
        propagated shouldBe unexpected
    }

    "catchOrThrowShouldNeverRecoverCancellationEvenWhenThrowableIsSelected" {
        val cancellation = CancellationException()

        val actual =
            shouldThrow<CancellationException> {
                Either.catchOrThrow<Throwable, Int> { throw cancellation }
            }

        actual shouldBe cancellation
    }
})
