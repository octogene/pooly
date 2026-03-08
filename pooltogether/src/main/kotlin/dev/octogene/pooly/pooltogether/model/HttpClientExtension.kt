package dev.octogene.pooly.pooltogether.model

import arrow.core.Either
import arrow.core.None.toEither
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import java.io.IOException

suspend inline fun <reified T> HttpClient.postOrEither(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): Either<CallError, T> = try {
    val response: HttpResponse = post(urlString) {
        block()
    }
    response.toEither<T>()
} catch (error: Throwable) {
    when (error) {
        is CancellationException -> throw error
        is IOException -> Either.Left(CallError.IOError(error))
        else -> Either.Left(CallError.UnexpectedCallError(error))
    }
}

suspend inline fun <reified T> HttpClient.getOrEither(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): Either<CallError, T> = try {
    val response: HttpResponse = get(urlString) {
        block()
    }
    response.toEither<T>()
} catch (e: Throwable) {
    when (e) {
        is CancellationException -> throw e
        is IOException -> Either.Left(CallError.IOError(e))
        else -> Either.Left(CallError.UnexpectedCallError(e))
    }
}

suspend inline fun <reified T> HttpResponse.toEither(): Either<CallError.HttpError<String>, T> =
    if (status.isSuccess()) {
        Either.Right(body<T>())
    } else {
        val errorBody = try {
            body<String>()
        } catch (e: Exception) {
            "Could not read error body: ${e.message}"
        }
        Either.Left(
            CallError.HttpError(
                status.value,
                status.description,
                errorBody,
            ),
        )
    }

suspend inline fun <reified T, reified E> HttpResponse.toErrorEither(): Either<CallError.HttpError<E?>, T> =
    if (status.isSuccess()) {
        Either.Right(body<T>())
    } else {
        val errorBody = try {
            body<E>()
        } catch (e: Exception) {
            "Could not read error body: ${e.message}"
            null
        }
        Either.Left(
            CallError.HttpError(
                status.value,
                status.description,
                errorBody,
            ),
        )
    }

suspend inline fun <reified T> HttpClient.deleteOrEither(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): Either<CallError, T> = try {
    val response: HttpResponse = delete(urlString) {
        block()
    }
    response.toEither<T>()
} catch (e: Throwable) {
    when (e) {
        is CancellationException -> throw e
        is IOException -> Either.Left(CallError.IOError(e))
        else -> Either.Left(CallError.UnexpectedCallError(e))
    }
}
