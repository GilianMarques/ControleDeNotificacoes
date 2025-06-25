package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.OperationResult
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.BlankKeywordException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.EmptyKeywordsException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.EmptyValidKeywordsException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.InvalidKeywordLengthException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.MaxKeywordsExceededException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.ConditionValidationException.PartialKeywordValidationException

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 20 de junho de 2025 às 15:20.
 *
 * Objeto responsável por validar instâncias de [Condition], garantindo que:
 * - O número de valores não ultrapasse o limite definido.
 * - Nenhum valor esteja vazio ou apenas com espaços.
 * - Nenhum valor ultrapasse o número máximo de caracteres permitidos.
 */
object ConditionValidator {

    const val MAX_VALUES = 50
    const val MAX_VALUE_LENGTH = 30

    fun validate(condition: Condition) {
        validateKeywords(condition.keywords).getOrThrow()
    }

    fun validateKeywords(keywords: List<String>): OperationResult<ConditionValidationException, List<String>> {

        if (keywords.isEmpty()) {
            return OperationResult.failure(EmptyKeywordsException())
        }

        if (keywords.size > MAX_VALUES) {
            return OperationResult.failure(MaxKeywordsExceededException(MAX_VALUES, keywords.size))
        }

        val validValues = mutableListOf<String>()
        var firstException: ConditionValidationException? = null

        for (keyword in keywords) when {

            keyword.isBlank() && firstException == null -> {
                firstException = BlankKeywordException(keyword)
            }

            keyword.length > MAX_VALUE_LENGTH && firstException == null -> {
                firstException = InvalidKeywordLengthException(keyword.length, MAX_VALUE_LENGTH)
            }

            !keyword.isBlank() && keyword.length <= MAX_VALUE_LENGTH -> {
                validValues.add(keyword)
            }
        }


        return when {
            validValues.isEmpty() -> OperationResult.failure(firstException ?: EmptyValidKeywordsException(keywords.size))
            firstException != null -> OperationResult.failure(PartialKeywordValidationException(validValues, firstException))
            else -> OperationResult.success(validValues)
        }
    }


    sealed class ConditionValidationException(message: String) : Exception(message) {

        class PartialKeywordValidationException(
            val validValues: MutableList<String>,
            val firstException: ConditionValidationException,
        ) :
            ConditionValidationException("A lista contem alguns valores invalidos. Valores válidos foram: $validValues.\nPrimeira Exceção: ${firstException.message}")

        class EmptyKeywordsException : ConditionValidationException("A lista de valores não pode estar vazia.")

        class EmptyValidKeywordsException(val size: Int) :
            ConditionValidationException("Nenhuma palavra-chave válida foi encontrada na lista. Tamanho da lista: $size")

        class MaxKeywordsExceededException(val max: Int, val found: Int) :
            ConditionValidationException("Número máximo de valores excedido: $found/$max")

        class BlankKeywordException(val value: String) :
            ConditionValidationException("A lista contém valor em branco ou inválido: \"$value\"")

        class InvalidKeywordLengthException(val length: Int, val max: Int) :
            ConditionValidationException("A lista contém valor com comprimento inválido (máximo $max): \"$length\"")
    }
}
