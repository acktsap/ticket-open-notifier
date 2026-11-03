package acktsap.fp

import acktsap.fp.instances.IOMonad
import acktsap.fp.typeclass.traverse
import kotlinx.collections.immutable.PersistentList

/** 입력을 복사해 순서를 고정하고 IO들을 순차 실행한다. 첫 실패 뒤에는 남은 효과를 만드는 함수도 호출하지 않는다. */
fun <A, B> Iterable<A>.traverseIO(effect: (A) -> IO<B>): IO<PersistentList<B>> = traverse(IOMonad, effect).asIO()
