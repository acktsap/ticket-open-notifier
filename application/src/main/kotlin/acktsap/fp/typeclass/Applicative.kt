package acktsap.fp.typeclass

import acktsap.fp.Kind

interface Applicative<F> : Functor<F> {
    /** 추가 효과나 실패 없이 이미 계산된 값을 문맥 F 안에 넣는다. */
    fun <A> pure(value: A): Kind<F, A>

    /** 문맥 안의 함수를 문맥 안의 인자에 적용한다. 실행 순서와 오류 누적 방식은 인스턴스가 정한다. */
    fun <A, B> ap(
        function: Kind<F, (A) -> B>,
        argument: Kind<F, A>,
    ): Kind<F, B>

    /** 순수 함수를 문맥 안으로 올려 적용하며 원래 계산의 문맥을 유지한다. */
    override fun <A, B> map(
        value: Kind<F, A>,
        transform: (A) -> B,
    ): Kind<F, B> = ap(pure(transform), value)

    /** 서로의 결과에 의존하지 않는 두 계산의 값을 결합한다. */
    fun <A, B, C> map2(
        first: Kind<F, A>,
        second: Kind<F, B>,
        combine: (A, B) -> C,
    ): Kind<F, C> = ap(map(first) { a -> { b: B -> combine(a, b) } }, second)
}
