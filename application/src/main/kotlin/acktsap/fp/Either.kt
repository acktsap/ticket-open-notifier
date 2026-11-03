package acktsap.fp

/** 실패 Left와 성공 Right를 값으로 표현한다. 변환 함수가 던진 예외를 자동으로 Left에 담지는 않는다. */
sealed class Either<out E, out A> : Kind<ForEither<E>, A> {
    data class Left<E>(val value: E) : Either<E, Nothing>()

    data class Right<A>(val value: A) : Either<Nothing, A>()

    /** Left와 Right 중 해당하는 콜백 하나만 실행해 공통 결과 타입으로 바꾼다. */
    inline fun <B> fold(
        ifLeft: (E) -> B,
        ifRight: (A) -> B,
    ): B =
        when (this) {
            is Left -> ifLeft(value)
            is Right -> ifRight(value)
        }

    /** Right의 값만 변환하며 Left의 오류는 그대로 전달한다. */
    fun <B> map(transform: (A) -> B): Either<E, B> =
        when (this) {
            is Left -> this
            is Right -> Right(transform(value))
        }

    /** Left의 오류만 변환하며 Right의 성공 값은 그대로 전달한다. */
    fun <F> mapLeft(transform: (E) -> F): Either<F, A> =
        when (this) {
            is Left -> Left(transform(value))
            is Right -> this
        }

    /** 성공 값의 존재 여부와 관계없이 실패 분기인지 판별한다. */
    fun isLeft(): Boolean = this is Left

    /** Left의 오류를 꺼내며 Right이면 null을 반환한다. null 오류와 Right를 구분하려면 isLeft를 사용한다. */
    fun leftOrNull(): E? = fold({ it }, { null })

    companion object {
        /** 선택한 타입의 복구 가능한 예외만 Left로 바꾼다. 다른 예외와 인터럽트·취소·치명적 오류는 다시 던진다. */
        inline fun <reified E : Throwable, A> catchOrThrow(block: () -> A): Either<E, A> =
            runCatching(block).fold(
                onSuccess = { Right(it) },
                onFailure = { error ->
                    if (error is E && isRecoverable(error)) Left(error) else throw error
                },
            )
    }
}

/** Right의 값으로 다음 계산을 연결한다. Left이면 다음 계산을 실행하지 않고 기존 오류를 유지한다. */
fun <E, A, B> Either<E, A>.flatMap(next: (A) -> Either<E, B>): Either<E, B> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> next(value)
    }

/** 값을 실패 분기로 감싼다. Throwable이어도 여기서는 던지지 않는다. */
fun <E> E.left(): Either<E, Nothing> = Either.Left(this)

/** 이미 계산된 값을 성공 분기로 감싼다. */
fun <A> A.right(): Either<Nothing, A> = Either.Right(this)
