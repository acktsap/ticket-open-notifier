package acktsap.fp.typeclass

import acktsap.fp.Kind

interface Functor<F> {
    /** 문맥 F를 유지하며 내부 값에 순수 변환을 적용한다. 항등 변환과 함수 합성 법칙을 지켜야 한다. */
    fun <A, B> map(
        value: Kind<F, A>,
        transform: (A) -> B,
    ): Kind<F, B>
}
