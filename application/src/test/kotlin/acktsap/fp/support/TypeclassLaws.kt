package acktsap.fp.support

import acktsap.fp.Kind
import acktsap.fp.typeclass.Applicative
import acktsap.fp.typeclass.Functor
import acktsap.fp.typeclass.Monad
import io.kotest.matchers.shouldBe

private typealias IntFunction = (Int) -> Int

object TypeclassLaws {
    fun <F, O> functor(
        instance: Functor<F>,
        source: Kind<F, Int>,
        observe: (Kind<F, Int>) -> O,
        offset: Int,
    ) {
        val first: IntFunction = { it + offset }
        val second: IntFunction = { it * 2 }

        observe(instance.map(source) { it }) shouldBe observe(source)
        observe(instance.map(instance.map(source, first), second)) shouldBe
            observe(instance.map(source) { second(first(it)) })
    }

    fun <F, O> applicative(
        instance: Applicative<F>,
        source: Kind<F, Int>,
        other: Kind<F, Int>,
        observe: (Kind<F, Int>) -> O,
        input: Int,
    ) {
        functor(instance, source, observe, input)
        val identity: IntFunction = { it }
        val transform: IntFunction = { it + input }
        val first = instance.map(source) { captured -> { value: Int -> value + captured } }
        val second = instance.map(other) { captured -> { value: Int -> value * captured } }
        val compose: (IntFunction) -> (IntFunction) -> IntFunction = { f -> { g -> { value -> f(g(value)) } } }

        observe(instance.ap(instance.pure(identity), source)) shouldBe observe(source)
        observe(instance.ap(instance.pure(transform), instance.pure(input))) shouldBe observe(instance.pure(transform(input)))
        observe(instance.ap(first, instance.pure(input))) shouldBe
            observe(instance.ap(instance.pure { f: IntFunction -> f(input) }, first))
        observe(instance.ap(instance.ap(instance.ap(instance.pure(compose), first), second), source)) shouldBe
            observe(instance.ap(first, instance.ap(second, source)))
    }

    fun <F, O> monad(
        instance: Monad<F>,
        source: Kind<F, Int>,
        other: Kind<F, Int>,
        observe: (Kind<F, Int>) -> O,
        input: Int,
    ) {
        applicative(instance, source, other, observe, input)
        val first: (Int) -> Kind<F, Int> = { value -> instance.map(other) { value + it } }
        val second: (Int) -> Kind<F, Int> = { value -> instance.map(other) { value * it } }

        observe(instance.flatMap(instance.pure(input), first)) shouldBe observe(first(input))
        observe(instance.flatMap(source) { instance.pure(it) }) shouldBe observe(source)
        observe(instance.flatMap(instance.flatMap(source, first), second)) shouldBe
            observe(instance.flatMap(source) { instance.flatMap(first(it), second) })
    }
}
