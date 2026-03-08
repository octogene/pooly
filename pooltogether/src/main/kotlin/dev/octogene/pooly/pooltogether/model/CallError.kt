package dev.octogene.pooly.pooltogether.model

import kotlinx.io.IOException

sealed class CallError {
    data class IOError(val error: IOException) : CallError()
    data class HttpError<E>(val code: Int, val message: String, val body: E?) : CallError()
    data class UnexpectedCallError(val error: Throwable) : CallError()
}
