@file:Suppress("ClassName")

package dev.gmarques.controledenotificacoes.domain

/**
 * Representa o resultado de uma operação que pode falhar com um erro [Exception]
 * ou ter sucesso com um valor [Value].
 * Criado por Gilian Marques
 * Em 20/06/2025 as 17:34
 */
sealed class OperationResult<out Exception, out Value> {

    /**
     * Representa um resultado de sucesso, contendo o valor da operação.
     * @param value O valor resultante da operação bem-sucedida.
     */
    data class success<Value>(val value: Value) : OperationResult<Nothing, Value>()

    /**
     * Representa um resultado de falha, contendo o erro ocorrido durante a operação.
     * @param error A exceção que causou a falha da operação.
     */
    data class failure<Exception>(val error: Exception) : OperationResult<Exception, Nothing>()

    /**
     * Verifica se o resultado da operação foi um sucesso.
     * @return `true` se a operação foi bem-sucedida, `false` caso contrário.
     */
    val isSuccess: Boolean get() = this is success

    /**
     * Verifica se o resultado da operação foi uma falha.
     * @return `true` se a operação falhou, `false` caso contrário.
     */
    val isFailure: Boolean get() = this is failure

    /**
     * Retorna o valor do resultado se a operação foi bem-sucedida, ou `null` se falhou.
     * @return O valor de sucesso ou `null`.
     */
    fun getOrNull(): Value? = when (this) {
        is success -> value
        is failure -> null
    }

    /**
     * Retorna a exceção se a operação falhou, ou `null` se foi bem-sucedida.
     * @return A exceção da falha ou `null`.
     */
    fun exceptionOrNull(): Exception? = when (this) {
        is failure -> error
        is success -> null
    }

    /**
     * Retorna o valor do resultado se a operação foi bem-sucedida, ou lança uma exceção se falhou.
     * @param mapError Uma função opcional para transformar a exceção original em outra `Throwable`.
     *                 Por padrão, lança uma `RuntimeException` com a mensagem "Erro: [erro]".
     * @return O valor de sucesso.
     * @throws Throwable A exceção mapeada (ou a original se `mapError` não for fornecida) se a operação falhou.
     */
    fun getOrThrow(mapError: (Exception) -> Throwable = { RuntimeException("Erro: $it") }): Value = when (this) {
        is success -> value
        is failure -> throw mapError(error)
    }

    /**
     * Aplica a função `onSuccess` ao valor se a operação foi bem-sucedida, ou a função `onError` à exceção se falhou.
     * @param onSuccess A função a ser aplicada em caso de sucesso.
     * @param onError A função a ser aplicada em caso de falha.
     * @return O resultado da aplicação da função correspondente.
     */
    inline fun <R> fold(
        onSuccess: (Value) -> R,
        onError: (Exception) -> R,
    ): R = when (this) {
        is success -> onSuccess(value)
        is failure -> onError(error)
    }

    /**
     * Transforma o valor de um resultado de sucesso usando a função `transform`.
     * Se o resultado for uma falha, retorna a falha original.
     * @param transform A função para transformar o valor de sucesso.
     * @return Um novo `OperationResult` com o valor transformado (se sucesso) ou a falha original.
     */
    inline fun <V2> map(transform: (Value) -> V2): OperationResult<Exception, V2> = when (this) {
        is success -> success(transform(value))
        is failure -> this
    }

    /**
     * Transforma a exceção de um resultado de falha usando a função `transform`.
     * Se o resultado for um sucesso, retorna o sucesso original.
     * @param transform A função para transformar a exceção de falha.
     * @return Um novo `OperationResult` com a exceção transformada (se falha) ou o sucesso original.
     */
    inline fun <E2> mapError(transform: (Exception) -> E2): OperationResult<E2, Value> = when (this) {
        is success -> this
        is failure -> failure(transform(error))
    }
}
