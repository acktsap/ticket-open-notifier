package acktsap.fp

/** 환경 전달과 IO를 합성한 ReaderT이다. local로 바꾼 범위를 제외하면 모든 단계가 같은 환경을 공유한다. */
class ReaderIO<R, out A>(private val build: (R) -> IO<A>) : Kind<ForReaderIO<R>, A> {
    /** 환경을 연결한 IO를 만든다. 환경을 사용하는 함수와 실제 효과는 IO 실행 시까지 미뤄진다. */
    fun run(environment: R): IO<A> = IO.defer { build(environment) }

    /** 동일한 환경을 유지하면서 실행 결과에 순수 변환을 적용한다. */
    fun <B> map(transform: (A) -> B): ReaderIO<R, B> = ReaderIO { run(it).map(transform) }

    /** 결과 값으로 다음 계산을 선택하고 두 계산에 같은 환경을 전달한다. */
    fun <B> flatMap(next: (A) -> ReaderIO<R, B>): ReaderIO<R, B> =
        ReaderIO { environment -> run(environment).flatMap { next(it).run(environment) } }

    /** 같은 환경에서 다음 계산을 연결하고 앞 계산의 값은 버린다. */
    fun <B> then(next: ReaderIO<R, B>): ReaderIO<R, B> = flatMap { next }

    /** 이 계산에 전달할 환경만 변환한다. 뒤에 연결된 바깥 계산에는 원래 환경이 유지된다. */
    fun <R2> local(transform: (R2) -> R): ReaderIO<R2, A> = ReaderIO { run(transform(it)) }

    companion object {
        /** 현재 전달된 환경을 결과 값으로 읽는다. */
        fun <R> ask(): ReaderIO<R, R> = ReaderIO(IO.Companion::pure)

        /** 환경을 읽지 않고 이미 계산된 값을 반환하는 계산을 만든다. */
        fun <R, A> pure(value: A): ReaderIO<R, A> = ReaderIO { IO.pure(value) }

        /** 환경이 필요 없는 IO를 ReaderIO 안으로 올린다. 이 시점에는 IO를 실행하지 않는다. */
        fun <R, A> liftIO(effect: IO<A>): ReaderIO<R, A> = ReaderIO { effect }
    }
}
