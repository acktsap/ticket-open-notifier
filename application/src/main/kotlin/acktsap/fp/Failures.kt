package acktsap.fp

import java.util.concurrent.CancellationException

/** 인터럽트·취소·JVM 치명적 오류는 복구 대상에서 제외한다. 이런 오류에서도 자원 해제는 수행해야 한다. */
@PublishedApi
@Suppress("DEPRECATION")
internal fun isRecoverable(error: Throwable): Boolean =
    when (error) {
        is InterruptedException,
        is CancellationException,
        is VirtualMachineError,
        is ThreadDeath,
        is LinkageError,
        -> false
        else -> true
    }
