package acktsap.fp

/** 실행할 효과의 설명을 나타낸다. 실행마다 효과를 반복하며, 동기 순차 실행과 자원 해제를 지원한다. */
sealed class IO<out A> private constructor() : Kind<ForIO, A> {
    private data class Pure<A>(val value: A) : IO<A>()

    private data class Delay<A>(val thunk: () -> A) : IO<A>()

    private data class Bind<A, B>(val source: IO<A>, val next: (A) -> IO<B>) : IO<B>()

    private data class Attempt<A>(val source: IO<A>) : IO<Either<Throwable, A>>()

    private data class Guarantee<A>(val source: IO<A>, val finalizer: IO<Unit>) : IO<A>()

    private sealed interface Frame {
        data class Continue(val next: (Any?) -> IO<Any?>) : Frame

        data object Catch : Frame

        data class Finalize(val effect: IO<Unit>) : Frame

        data class Restore(val value: Any?) : Frame

        data class Rethrow(val error: Throwable) : Frame
    }

    /** 실행 시 성공 결과에 순수 변환을 적용한다. 지금은 변환 함수를 실행하지 않는다. */
    fun <B> map(transform: (A) -> B): IO<B> = flatMap { pure(transform(it)) }

    /** 앞 IO의 성공 값으로 다음 IO를 선택하며, 앞 단계가 실패하면 다음 함수를 호출하지 않는다. */
    fun <B> flatMap(next: (A) -> IO<B>): IO<B> = Bind(this, next)

    /** 앞 IO가 성공한 뒤 다음 IO를 실행하고, 앞 단계의 결과 값은 버린다. */
    fun <B> then(next: IO<B>): IO<B> = flatMap { next }

    /** 추가 효과를 실행한 뒤 원래 값을 유지한다. 추가 효과가 실패하면 전체 계산도 실패한다. */
    fun tap(effect: (A) -> IO<Unit>): IO<A> = flatMap { value -> effect(value).map { value } }

    /** 복구 가능한 실행 예외를 Left로 반환한다. 인터럽트·취소·JVM 치명적 오류는 계속 전파한다. */
    fun attempt(): IO<Either<Throwable, A>> = Attempt(this)

    /**
     * 성공·실패 모두에서 정리 효과를 실행한다. attempt로 복구할 수 없는 오류에서도 정리를 수행한다.
     * 정리 중 복구 불가능한 오류가 발생하면 원래의 일반 오류보다 우선해 전파한다.
     */
    fun guarantee(finalizer: IO<Unit>): IO<A> = Guarantee(this, finalizer)

    private sealed interface Frames {
        data object Empty : Frames

        data class Push(val head: Frame, val tail: Frames) : Frames
    }

    private sealed interface Execution {
        data class Evaluate(val source: IO<Any?>, val frames: Frames) : Execution

        data class Succeed(val value: Any?, val frames: Frames) : Execution

        data class Fail(val error: Throwable, val frames: Frames) : Execution

        data class Done(val result: Either<Throwable, Any?>) : Execution
    }

    /** 효과를 실제로 실행하며 예외를 던질 수 있다. 애플리케이션 또는 테스트의 최종 실행 경계에서 호출한다. */
    @Suppress("UNCHECKED_CAST")
    fun unsafeRunSync(): A = execute(Execution.Evaluate(this, Frames.Empty)).fold({ throw it }, { it as A })

    companion object {
        private tailrec fun execute(execution: Execution): Either<Throwable, Any?> =
            when (execution) {
                is Execution.Done -> execution.result
                else -> execute(advance(execution))
            }

        private fun advance(execution: Execution): Execution =
            when (execution) {
                is Execution.Evaluate -> evaluate(execution.source, execution.frames)
                is Execution.Succeed -> resume(execution.value, execution.frames)
                is Execution.Fail -> unwind(execution.error, execution.frames)
                is Execution.Done -> execution
            }

        /** 치명적 오류도 실패 상태로 받아 해제 프레임을 거친 뒤 전파한다. */
        @Suppress("UNCHECKED_CAST")
        private fun evaluate(
            source: IO<Any?>,
            frames: Frames,
        ): Execution =
            when (source) {
                is Pure -> Execution.Succeed(source.value, frames)
                is Delay ->
                    runCatching(source.thunk).fold(
                        onSuccess = { Execution.Succeed(it, frames) },
                        onFailure = { Execution.Fail(it, frames) },
                    )
                is Bind<*, *> -> {
                    val bind = source as Bind<Any?, Any?>
                    Execution.Evaluate(bind.source, Frames.Push(Frame.Continue(bind.next), frames))
                }
                is Attempt<*> -> Execution.Evaluate(source.source, Frames.Push(Frame.Catch, frames))
                is Guarantee<*> -> Execution.Evaluate(source.source, Frames.Push(Frame.Finalize(source.finalizer), frames))
            }

        private fun resume(
            value: Any?,
            frames: Frames,
        ): Execution =
            when (frames) {
                Frames.Empty -> Execution.Done(value.right())
                is Frames.Push ->
                    when (val frame = frames.head) {
                        is Frame.Continue ->
                            runCatching { frame.next(value) }.fold(
                                onSuccess = { Execution.Evaluate(it, frames.tail) },
                                onFailure = { Execution.Fail(it, frames.tail) },
                            )
                        Frame.Catch -> Execution.Succeed(value.right(), frames.tail)
                        is Frame.Finalize -> Execution.Evaluate(frame.effect, Frames.Push(Frame.Restore(value), frames.tail))
                        is Frame.Restore -> Execution.Succeed(frame.value, frames.tail)
                        is Frame.Rethrow -> Execution.Fail(frame.error, frames.tail)
                    }
            }

        private fun unwind(
            error: Throwable,
            frames: Frames,
        ): Execution =
            when (frames) {
                Frames.Empty -> Execution.Done(error.left())
                is Frames.Push ->
                    when (val frame = frames.head) {
                        is Frame.Continue, is Frame.Restore -> Execution.Fail(error, frames.tail)
                        Frame.Catch ->
                            if (isRecoverable(error)) {
                                Execution.Succeed(error.left(), frames.tail)
                            } else {
                                Execution.Fail(error, frames.tail)
                            }
                        is Frame.Finalize -> Execution.Evaluate(frame.effect, Frames.Push(Frame.Rethrow(error), frames.tail))
                        is Frame.Rethrow -> Execution.Fail(combineFailures(frame.error, error), frames.tail)
                    }
            }

        private fun combineFailures(
            original: Throwable,
            finalizer: Throwable,
        ): Throwable =
            if (!isRecoverable(finalizer) && isRecoverable(original)) {
                suppress(finalizer, original)
            } else {
                suppress(original, finalizer)
            }

        private fun suppress(
            primary: Throwable,
            secondary: Throwable,
        ): Throwable = primary.also { if (primary !== secondary) it.addSuppressed(secondary) }

        /** 이미 계산된 값을 IO에 넣는다. 인자 계산을 지연하려면 delay를 사용한다. */
        fun <A> pure(value: A): IO<A> = Pure(value)

        /** 실행할 때마다 thunk를 다시 평가하는 IO를 만든다. 생성 시에는 평가하지 않는다. */
        fun <A> delay(thunk: () -> A): IO<A> = Delay(thunk)

        /** 효과 실행뿐 아니라 다음 IO를 만드는 작업까지 실행 시점으로 미룬다. */
        fun <A> defer(thunk: () -> IO<A>): IO<A> = pure(Unit).flatMap { thunk() }

        /** 생성 시가 아닌 실행 시 지정한 오류로 실패하는 IO를 만든다. */
        fun <A> raiseError(error: Throwable): IO<A> = delay { throw error }

        /** 획득 성공 후 사용 효과가 끝나면 해제 효과를 실행한다. 사용 실패 시에도 해제하며, 획득 실패 시에는 해제하지 않는다. */
        fun <R, A> bracket(
            acquire: IO<R>,
            release: (R) -> IO<Unit>,
            use: (R) -> IO<A>,
        ): IO<A> = acquire.flatMap { resource -> defer { use(resource) }.guarantee(defer { release(resource) }) }

        /** Right는 성공 IO로, Left는 실행 시 해당 예외로 실패하는 IO로 바꾼다. */
        fun <A> fromEither(value: Either<Throwable, A>): IO<A> = value.fold(::raiseError, ::pure)
    }
}
