package org.jetbrains.ktor.sample

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <A> Flow<A>.track(tracker: suspend (suspend () -> Unit) -> Unit): Flow<A> = flow {
    tracker { collect { emit(it) } }
}
