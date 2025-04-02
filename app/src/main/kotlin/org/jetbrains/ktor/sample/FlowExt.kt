package org.jetbrains.ktor.sample

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Wraps the [Flow] with builder methods like `trackAiQuestion { }, or `measureTimeMillis { }`
 */
fun <A> Flow<A>.track(tracker: suspend (suspend () -> Unit) -> Unit): Flow<A> =
    flow {
        tracker {
            collect { emit(it) }
        }
    }
