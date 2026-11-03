package acktsap.fp

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

/** 독립적인 검증의 오류를 누적한다. 앞 결과에 의존하는 계산의 중단은 Either.flatMap으로 표현한다. */
sealed class Validated<out E, out A> : Kind<ForValidated<E>, A> {
    data class Valid<A>(val value: A) : Validated<Nothing, A>()

    data class Invalid<E>(val errors: PersistentList<E>) : Validated<E, Nothing>() {
        init {
            require(errors.isNotEmpty())
        }
    }

    /** 성공은 Right로, 누적된 오류 목록은 순서를 유지한 Left로 바꾼다. */
    fun toEither(): Either<PersistentList<E>, A> =
        when (this) {
            is Valid -> Either.Right(value)
            is Invalid -> Either.Left(errors)
        }

    companion object {
        /** null이면 오류 하나를 가진 검증 실패를, 값이 있으면 검증 성공을 만든다. */
        fun <E, A : Any> required(
            value: A?,
            error: E,
        ): Validated<E, A> = value?.let { Valid(it) } ?: Invalid(persistentListOf(error))
    }
}

/** 두 검증이 모두 성공하면 값을 결합하고, 실패가 있으면 양쪽 오류를 왼쪽부터 모두 모은다. */
fun <E, A, B, C> Validated<E, A>.zip(
    other: Validated<E, B>,
    combine: (A, B) -> C,
): Validated<E, C> =
    when (this) {
        is Validated.Valid ->
            when (other) {
                is Validated.Valid -> Validated.Valid(combine(value, other.value))
                is Validated.Invalid -> other
            }
        is Validated.Invalid ->
            when (other) {
                is Validated.Valid -> this
                is Validated.Invalid -> Validated.Invalid(errors.addAll(other.errors))
            }
    }
