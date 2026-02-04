package dev.octogene.pooly.server.model

sealed interface ApplicationError {
    sealed interface AuthenticationError : ApplicationError {
        object InvalidCredentials : AuthenticationError
        data class UserNotFound(val username: String) : AuthenticationError
        data class UserAlreadyExists(val username: String) : AuthenticationError
        data class InternalError(val message: String) : AuthenticationError
    }
}
