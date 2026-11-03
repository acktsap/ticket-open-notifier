package acktsap.fp

/** F<A>를 표현하는 타입 인코딩이다. F는 실제 의존 객체가 아닌 타입 생성자의 표식이다. */
sealed interface Kind<out F, out A>

class ForIO private constructor()

class ForEither<out E> private constructor()

class ForReaderIO<R> private constructor()

class ForResource private constructor()

class ForValidated<out E> private constructor()

/** IO 타입 표식을 가진 Kind를 구체적인 IO로 되돌린다. 실행하지는 않는다. */
@Suppress("UNCHECKED_CAST")
fun <A> Kind<ForIO, A>.asIO(): IO<A> = this as IO<A>

/** 오류 타입 E가 고정된 Kind를 Either로 되돌린다. */
@Suppress("UNCHECKED_CAST")
fun <E, A> Kind<ForEither<E>, A>.asEither(): Either<E, A> = this as Either<E, A>

/** 환경 타입 R이 고정된 Kind를 ReaderIO로 되돌린다. */
@Suppress("UNCHECKED_CAST")
fun <R, A> Kind<ForReaderIO<R>, A>.asReaderIO(): ReaderIO<R, A> = this as ReaderIO<R, A>

/** Resource 타입 표식을 가진 Kind를 되돌린다. 자원을 획득하지는 않는다. */
@Suppress("UNCHECKED_CAST")
fun <A> Kind<ForResource, A>.asResource(): Resource<A> = this as Resource<A>

/** 오류 타입 E가 고정된 Kind를 Validated로 되돌린다. */
@Suppress("UNCHECKED_CAST")
fun <E, A> Kind<ForValidated<E>, A>.asValidated(): Validated<E, A> = this as Validated<E, A>
