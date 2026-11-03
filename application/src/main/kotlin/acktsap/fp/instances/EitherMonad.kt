package acktsap.fp.instances

import acktsap.fp.Either
import acktsap.fp.ForEither
import acktsap.fp.Kind
import acktsap.fp.asEither
import acktsap.fp.flatMap
import acktsap.fp.typeclass.Monad

class EitherMonad<E> : Monad<ForEither<E>> {
    /** 값을 Right로 감싸며 오류 타입 E는 인스턴스에 고정한다. */
    override fun <A> pure(value: A): Either<E, A> = Either.Right(value)

    /** Right에서만 다음 계산을 호출하고 Left는 그대로 전달한다. */
    override fun <A, B> flatMap(
        value: Kind<ForEither<E>, A>,
        next: (A) -> Kind<ForEither<E>, B>,
    ): Either<E, B> = value.asEither().flatMap { next(it).asEither() }
}
