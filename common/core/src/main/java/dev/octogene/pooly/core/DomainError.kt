package dev.octogene.pooly.core

sealed interface DomainError

sealed interface ValidationError : DomainError

data class InvalidField(val fieldName: String, val message: String) : ValidationError
