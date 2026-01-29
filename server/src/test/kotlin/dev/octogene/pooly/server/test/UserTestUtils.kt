package dev.octogene.pooly.server.test

import dev.octogene.pooly.server.user.LoginUserRequest
import dev.octogene.pooly.server.user.RegisterUserRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun HttpClient.registerUser(
    username: String,
    password: String,
    email: String
): HttpResponse {
    val newUserJson = RegisterUserRequest(
        username,
        password,
        email
    )

    val response = post("/api/v1/register") {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        setBody(newUserJson)
    }
    return response
}

suspend fun HttpClient.loginUser(
    username: String,
    password: String
): HttpResponse {
    val credentialsJson = LoginUserRequest(username, password)

    return post("/api/v1/login") {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        setBody(credentialsJson)
    }
}