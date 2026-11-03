package acktsap.fp.instances

import acktsap.fp.ForResource
import acktsap.fp.Kind
import acktsap.fp.Resource
import acktsap.fp.asResource
import acktsap.fp.typeclass.Monad

object ResourceMonad : Monad<ForResource> {
    /** 획득·해제가 필요 없는 값을 Resource로 감싼다. */
    override fun <A> pure(value: A): Resource<A> = Resource.pure(value)

    /** 다음 자원의 수명을 현재 자원 안에 중첩하고, 실제 사용이 끝난 뒤 역순으로 해제한다. */
    override fun <A, B> flatMap(
        value: Kind<ForResource, A>,
        next: (A) -> Kind<ForResource, B>,
    ): Resource<B> = value.asResource().flatMap { next(it).asResource() }
}
