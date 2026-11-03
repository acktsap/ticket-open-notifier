package acktsap.fp.typeclass

import acktsap.fp.Kind

interface Monad<F> : Applicative<F> {
    /** 앞 계산의 값에 의존하는 다음 계산을 선택해 연결한다. 좌·우 항등과 결합 법칙을 지켜야 한다. */
    fun <A, B> flatMap(
        value: Kind<F, A>,
        next: (A) -> Kind<F, B>,
    ): Kind<F, B>

    /** flatMap과 동일한 문맥 처리 규칙을 유지하며 값만 변환한다. */
    override fun <A, B> map(
        value: Kind<F, A>,
        transform: (A) -> B,
    ): Kind<F, B> = flatMap(value) { pure(transform(it)) }

    /** 함수를 담은 계산을 먼저 연결하고, 그 함수로 인자 계산의 값을 변환한다. */
    override fun <A, B> ap(
        function: Kind<F, (A) -> B>,
        argument: Kind<F, A>,
    ): Kind<F, B> = flatMap(function) { transform -> map(argument, transform) }
}
