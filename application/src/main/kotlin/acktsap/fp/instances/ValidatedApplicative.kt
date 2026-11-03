package acktsap.fp.instances

import acktsap.fp.ForValidated
import acktsap.fp.Kind
import acktsap.fp.Validated
import acktsap.fp.asValidated
import acktsap.fp.typeclass.Applicative
import acktsap.fp.zip

class ValidatedApplicative<E> : Applicative<ForValidated<E>> {
    /** 값을 오류 없는 검증 성공으로 감싼다. */
    override fun <A> pure(value: A): Validated<E, A> = Validated.Valid(value)

    /** 양쪽이 성공하면 함수를 적용한다. 실패하면 함수 쪽 오류와 인자 쪽 오류를 그 순서대로 모두 모은다. */
    override fun <A, B> ap(
        function: Kind<ForValidated<E>, (A) -> B>,
        argument: Kind<ForValidated<E>, A>,
    ): Validated<E, B> = function.asValidated().zip(argument.asValidated()) { transform, value -> transform(value) }
}
