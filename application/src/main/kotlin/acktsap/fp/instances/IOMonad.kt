package acktsap.fp.instances

import acktsap.fp.ForIO
import acktsap.fp.IO
import acktsap.fp.Kind
import acktsap.fp.asIO
import acktsap.fp.typeclass.Monad

object IOMonad : Monad<ForIO> {
    /** 이미 계산된 값을 실행 효과가 없는 IO로 감싼다. */
    override fun <A> pure(value: A): IO<A> = IO.pure(value)

    /** IO 실행 시 앞 결과로 다음 IO를 선택한다. 생성 시 콜백을 실행하지 않는다. */
    override fun <A, B> flatMap(
        value: Kind<ForIO, A>,
        next: (A) -> Kind<ForIO, B>,
    ): IO<B> = value.asIO().flatMap { next(it).asIO() }
}
