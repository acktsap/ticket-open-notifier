package acktsap.fp.instances

import acktsap.fp.ForReaderIO
import acktsap.fp.Kind
import acktsap.fp.ReaderIO
import acktsap.fp.asReaderIO
import acktsap.fp.typeclass.Monad

class ReaderIOMonad<R> : Monad<ForReaderIO<R>> {
    /** 환경 R을 사용하지 않고 지정한 값을 반환하는 ReaderIO를 만든다. */
    override fun <A> pure(value: A): ReaderIO<R, A> = ReaderIO.pure(value)

    /** 같은 환경 R을 공유하며 결과에 의존하는 다음 ReaderIO를 연결한다. */
    override fun <A, B> flatMap(
        value: Kind<ForReaderIO<R>, A>,
        next: (A) -> Kind<ForReaderIO<R>, B>,
    ): ReaderIO<R, B> = value.asReaderIO().flatMap { next(it).asReaderIO() }
}
