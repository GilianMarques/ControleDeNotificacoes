package dev.gmarques.controledenotificacoes.domain.model

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
        validateValues(condition.values).getOrThrow()
    }

    fun validateValues(values: List<String>): Result<List<String>> {

        if (values.isEmpty()) {
            return Result.failure(EmptyValuesException())
        }

        if (values.size > MAX_VALUES) {
            return Result.failure(MaxValuesExceededException(MAX_VALUES, values.size))
        }

        val invalidBlank = values.find { it.isBlank() }
        if (invalidBlank != null) {
            return Result.failure(BlankValueException(invalidBlank))
        }

        val invalidValueLength = values.find { it.length > MAX_VALUE_LENGTH }
        if (invalidValueLength != null) {
            return Result.failure(InvalidValueLengthException(invalidValueLength, MAX_VALUE_LENGTH))
        }

        return Result.success(values)
    }

    sealed class ConditionValidationException(message: String) : Exception(message)

    class EmptyValuesException :
        ConditionValidationException("A lista de valores não pode estar vazia.")

    class MaxValuesExceededException(max: Int, found: Int) :
        ConditionValidationException("Número máximo de valores excedido: $found/$max")

    class BlankValueException(value: String) :
        ConditionValidationException("A lista contém valor em branco ou inválido: \"$value\"")

    class InvalidValueLengthException(value: String, max: Int) :
        ConditionValidationException("A lista contém valor com comprimento inválido (máximo $max): \"$value\"")
}
