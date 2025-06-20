@file:Suppress("ClassName")

package dev.gmarques.controledenotificacoes.domain

/**
 * Representa o resultado de uma operação que pode falhar com um erro [E]
 * ou ter sucesso com um valor [V].
 * Criado por Gilian Marques
 * Em 20/06/2025 as 17:34
 */
sealed class OperationResult<out E, out V> {

    data class success<V>(val value: V) : OperationResult<Nothing, V>()
    data class failure<E>(val error: E) : OperationResult<E, Nothing>()

    val isSuccess: Boolean get() = this is success
    val isFailure: Boolean get() = this is failure

    fun getOrNull(): V? = when (this) {
        is success -> value
        is failure -> null
    }

    fun exceptionOrNull(): E? = when (this) {
        is failure -> error
        is success -> null
    }

    fun getOrThrow(mapError: (E) -> Throwable = { RuntimeException("Erro: $it") }): V = when (this) {
        is success -> value
        is failure -> throw mapError(error)
    }

    inline fun <R> fold(
        onSuccess: (V) -> R,
        onError: (E) -> R,
    ): R = when (this) {
        is success -> onSuccess(value)
        is failure -> onError(error)
    }

    inline fun <V2> map(transform: (V) -> V2): OperationResult<E, V2> = when (this) {
        is success -> success(transform(value))
        is failure -> this
    }

    inline fun <E2> mapError(transform: (E) -> E2): OperationResult<E2, V> = when (this) {
        is success -> this
        is failure -> failure(transform(error))
    }
}
