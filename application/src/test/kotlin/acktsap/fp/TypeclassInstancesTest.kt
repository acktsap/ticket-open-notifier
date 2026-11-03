package acktsap.fp

import acktsap.fp.instances.EitherMonad
import acktsap.fp.instances.IOMonad
import acktsap.fp.instances.ReaderIOMonad
import acktsap.fp.instances.ResourceMonad
import acktsap.fp.instances.ValidatedApplicative
import acktsap.fp.support.TypeclassLaws
import acktsap.fp.typeclass.traverse
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.collections.immutable.persistentListOf

class TypeclassInstancesTest : StringSpec({
    "IOMonadShouldObeyFunctorApplicativeAndMonadLawsIncludingEffectTracesAndFailures" {
        checkAll(Arb.int(), Arb.int(), Arb.boolean(), Arb.boolean()) { value, otherValue, failSource, failOther ->
            val events = mutableListOf<String>()
            val sourceFailure = IllegalStateException()
            val otherFailure = IllegalArgumentException()
            val source =
                IO.delay {
                    events.add("source")
                    if (failSource) throw sourceFailure
                    value
                }
            val other =
                IO.delay {
                    events.add("other")
                    if (failOther) throw otherFailure
                    otherValue
                }

            TypeclassLaws.monad(
                IOMonad,
                source,
                other,
                observe = { program ->
                    events.clear()
                    val result = program.asIO().attempt().unsafeRunSync()
                    result to events.toList()
                },
                input = value,
            )
        }
    }

    "EitherMonadShouldObeyTheSameLawsForLeftAndRight" {
        checkAll(Arb.int(), Arb.int(), Arb.boolean(), Arb.boolean()) { value, otherValue, failSource, failOther ->
            val instance = EitherMonad<String>()
            val source: Either<String, Int> = if (failSource) "source".left() else value.right()
            val other: Either<String, Int> = if (failOther) "other".left() else otherValue.right()

            TypeclassLaws.monad(instance, source, other, observe = { it.asEither() }, input = value)
        }
    }

    "ReaderIOMonadShouldObeyTheSameLawsWithAnEnvironmentAndEffects" {
        checkAll(Arb.int(), Arb.int(), Arb.boolean(), Arb.boolean()) { value, environment, failSource, failOther ->
            val instance = ReaderIOMonad<Int>()
            val events = mutableListOf<String>()
            val failure = IllegalStateException()
            val source =
                ReaderIO<Int, Int> { context ->
                    IO.delay {
                        events.add("source")
                        if (failSource) throw failure
                        value + context
                    }
                }
            val other =
                ReaderIO<Int, Int> { context ->
                    IO.delay {
                        events.add("other")
                        if (failOther) throw failure
                        value * context
                    }
                }

            TypeclassLaws.monad(
                instance,
                source,
                other,
                observe = { program ->
                    events.clear()
                    val result = program.asReaderIO().run(environment).attempt().unsafeRunSync()
                    result to events.toList()
                },
                input = value,
            )
        }
    }

    "ResourceMonadShouldObeyTheSameLawsIncludingAcquisitionAndReleaseOrder" {
        checkAll(Arb.int(), Arb.int(), Arb.boolean(), Arb.boolean()) { value, otherValue, failSource, failOther ->
            val events = mutableListOf<String>()
            val failure = IllegalStateException()
            val source =
                Resource.make(
                    IO.delay {
                        events.add("acquire source")
                        if (failSource) throw failure
                        value
                    },
                ) { IO.delay<Unit> { events.add("release source") } }
            val other =
                Resource.make(
                    IO.delay {
                        events.add("acquire other")
                        if (failOther) throw failure
                        otherValue
                    },
                ) { IO.delay<Unit> { events.add("release other") } }

            TypeclassLaws.monad(
                ResourceMonad,
                source,
                other,
                observe = { program ->
                    events.clear()
                    val result = program.asResource().use { IO.pure(it) }.attempt().unsafeRunSync()
                    result to events.toList()
                },
                input = value,
            )
        }
    }

    "ValidatedApplicativeShouldObeyFunctorAndApplicativeLawsWhileAccumulatingErrors" {
        checkAll(Arb.int(), Arb.int(), Arb.boolean(), Arb.boolean()) { value, otherValue, failSource, failOther ->
            val instance = ValidatedApplicative<String>()
            val source = Validated.required(value.takeUnless { failSource }, "source")
            val other = Validated.required(otherValue.takeUnless { failOther }, "other")

            TypeclassLaws.applicative(instance, source, other, observe = { it.asValidated().toEither() }, input = value)
        }
    }

    "map2ShouldDistinguishFailFastEitherFromAccumulatingValidated" {
        val first: Either<String, Int> = "first".left()
        val second: Either<String, Int> = "second".left()
        val invalidFirst = Validated.required<String, Int>(null, "first")
        val invalidSecond = Validated.required<String, Int>(null, "second")

        val stopped = EitherMonad<String>().map2(first, second, Int::plus).asEither()
        val accumulated = ValidatedApplicative<String>().map2(invalidFirst, invalidSecond, Int::plus).asValidated().toEither()

        stopped shouldBe "first".left()
        accumulated shouldBe persistentListOf("first", "second").left()
    }

    "genericTraverseShouldAlsoWorkForEitherAndStopBeforeBuildingLaterValues" {
        val instance = EitherMonad<String>()
        val visited = mutableListOf<Int>()

        val actual =
            (1..5).traverse(instance) { value ->
                visited.add(value)
                if (value == 3) "failure".left() else value.right()
            }.asEither()

        actual shouldBe "failure".left()
        visited shouldBe listOf(1, 2, 3)
    }
})
