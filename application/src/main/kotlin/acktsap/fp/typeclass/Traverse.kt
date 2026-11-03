package acktsap.fp.typeclass

import acktsap.fp.Kind
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

/** 입력 순서대로 계산을 연결하고 결과 목록을 모은다. 실제 평가 시점과 실패 처리 방식은 주어진 Monad를 따른다. */
fun <F, A, B> Iterable<A>.traverse(
    monad: Monad<F>,
    effect: (A) -> Kind<F, B>,
): Kind<F, PersistentList<B>> =
    toPersistentList().fold(monad.pure(persistentListOf<B>())) { accumulated, value ->
        monad.flatMap(accumulated) { results -> monad.map(effect(value)) { results.add(it) } }
    }
