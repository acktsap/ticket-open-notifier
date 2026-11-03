package acktsap.fp

/** 자원의 수명을 사용 효과 전체에 연결하며, 중첩된 자원은 획득 역순으로 해제한다. */
sealed class Resource<out A> private constructor() : Kind<ForResource, A> {
    private data class Pure<A>(val value: A) : Resource<A>()

    private data class Allocate<A>(val acquire: IO<A>, val release: (A) -> IO<Unit>) : Resource<A>()

    private data class Bind<A, B>(val source: Resource<A>, val next: (A) -> Resource<B>) : Resource<B>()

    /** 획득한 값에 순수 변환을 적용하며 기존 자원의 해제 범위를 유지한다. */
    fun <B> map(transform: (A) -> B): Resource<B> = flatMap { pure(transform(it)) }

    /** 획득한 값으로 다음 자원을 선택한다. 중첩된 자원은 사용이 끝난 뒤 획득 역순으로 해제된다. */
    fun <B> flatMap(next: (A) -> Resource<B>): Resource<B> = Bind(this, next)

    /** 자원을 획득해 IO 전체에서 사용하고 성공·실패 모두에서 해제한다. 생성 시에는 자원을 획득하지 않는다. */
    @Suppress("UNCHECKED_CAST")
    fun <B> use(effect: (A) -> IO<B>): IO<B> =
        IO.defer {
            when (this) {
                is Pure -> IO.defer { effect(value) }
                is Allocate -> IO.bracket(acquire, release, effect)
                is Bind<*, *> -> {
                    val bind = this as Bind<Any?, A>
                    bind.source.use { value -> bind.next(value).use(effect) }
                }
            }
        }

    companion object {
        /** 획득·해제가 필요 없는 값을 Resource로 감싼다. */
        fun <A> pure(value: A): Resource<A> = Pure(value)

        /** 획득 효과와 해제 함수를 묶는다. 실제 획득과 해제는 use가 반환한 IO를 실행할 때 수행한다. */
        fun <A> make(
            acquire: IO<A>,
            release: (A) -> IO<Unit>,
        ): Resource<A> = Allocate(acquire, release)
    }
}
