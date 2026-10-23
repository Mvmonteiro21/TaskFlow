package com.example.taskflow.domain.common

import kotlin.coroutines.cancellation.CancellationException

/** Resultado de operações de negócio: a UI nunca precisa de try/catch. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(
        val message: String,
        val isValidation: Boolean = false
    ) : AppResult<Nothing>
}

/** Executa [block] convertendo qualquer exceção em [AppResult.Failure]. */
inline fun <T> safeCall(errorMessage: String, block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Failure(errorMessage)
    }
