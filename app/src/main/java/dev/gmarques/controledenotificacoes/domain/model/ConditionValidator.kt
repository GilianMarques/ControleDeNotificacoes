package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.CantBeNullException
import dev.gmarques.controledenotificacoes.domain.OperationResult
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.KEYWORD_MAX_LENGTH
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.KeywordsValidationException.EmptyKeywordsException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.KeywordsValidationException.MaxKeywordsExceededException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.SingleKeywordValidationException.BlankKeywordException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.SingleKeywordValidationException.InvalidKeywordLengthException
import dev.gmarques.controledenotificacoes.domain.model.ConditionValidator.validateKeyword

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
    const val KEYWORD_MAX_LENGTH = 30

    fun validate(condition: Condition) {
        validateKeywords(condition.keywords).getOrThrow()
    }

    /**
     * Valida uma lista de palavras-chave.
     *
     * Esta função realiza as seguintes verificações:
     * 1. **Lista Vazia:** Garante que a lista de palavras-chave não esteja vazia.
     *    Se estiver vazia, retorna uma falha com [EmptyKeywordsException].
     * 2. **Limite de Palavras-Chave:** Verifica se o número de palavras-chave na lista
     *    não excede o máximo permitido ([MAX_VALUES]). Se exceder, retorna uma falha
     *    com [MaxKeywordsExceededException].
     * 3. **Validação Individual:** Itera sobre cada palavra-chave na lista e a valida
     *    individualmente usando a função [validateKeyword].
     *    - Se a validação de uma palavra-chave individual falhar (retornar `isFailure`),
     *      a exceção correspondente ([SingleKeywordValidationException]) é lançada.
     *      Isso interrompe o processo de validação da lista.
     *      - É importante notar que, neste caso, a exceção lançada será uma
     *        [SingleKeywordValidationException] e não uma [KeywordsValidationException]
     *        diretamente, pois o objetivo é sinalizar o problema específico com a
     *        palavra-chave individual. O chamador pode precisar tratar
     *        [SingleKeywordValidationException] separadamente ou encapsulá-la, se necessário.
     *
     * @param keywords A lista de strings representando as palavras-chave a serem validadas.
     * @return Um [OperationResult] que:
     *   - Em caso de sucesso ([OperationResult.success]), contém a lista de palavras-chave original (se todas forem válidas).
     *   - Em caso de falha ([OperationResult.failure]) devido à lista estar vazia ou exceder o limite,
     *     contém a [KeywordsValidationException] apropriada ([EmptyKeywordsException] ou [MaxKeywordsExceededException]).
     * @throws SingleKeywordValidationException Se a validação de uma palavra-chave individual falhar. Valide a palavra chave individualmente antes de adiciona-la a lista. use [validateKeyword].
     * @throws CantBeNullException Se `keywordResult.exceptionOrNull()` retornar nulo inesperadamente (salvaguarda).
     */
    fun validateKeywords(keywords: List<String>): OperationResult<KeywordsValidationException, List<String>> {

        if (keywords.isEmpty()) {
            return OperationResult.failure(EmptyKeywordsException())
        }

        if (keywords.size > MAX_VALUES) { // Verifica se o número de palavras-chave excede o máximo permitido.
            return OperationResult.failure(MaxKeywordsExceededException(MAX_VALUES, keywords.size)) // Retorna falha se exceder.
        }

        for (keyword in keywords) { // Itera sobre cada palavra-chave na lista.
            val keywordResult = validateKeyword(keyword) // Valida a palavra-chave individualmente.
            if (keywordResult.isFailure) throw keywordResult.exceptionOrNull()
                ?: CantBeNullException() // Se a validação individual falhar, lança a exceção correspondente.
        }
        return OperationResult.success(keywords)
    }


    /**
     * Valida uma única palavra-chave (keyword).
     *
     * Esta função verifica se a palavra-chave fornecida não está vazia após a remoção de espaços em branco
     * e se seu comprimento não excede o limite máximo permitido ([KEYWORD_MAX_LENGTH]).
     *
     * @param keyword A string da palavra-chave a ser validada.
     * @return Um [OperationResult] que:
     *   - Em caso de sucesso ([OperationResult.success]), contém a palavra-chave validada (após `trim()`).
     *   - Em caso de falha ([OperationResult.failure]), contém uma [KeywordsValidationException] apropriada:
     *     - [BlankKeywordException] se a palavra-chave estiver vazia após `trim()`.
     *     - [InvalidKeywordLengthException] se o comprimento da palavra-chave exceder [KEYWORD_MAX_LENGTH].
     */
    fun validateKeyword(keyword: String): OperationResult<SingleKeywordValidationException, String> {


        val sanitizedKeyword = keyword.trim()

        if (sanitizedKeyword.isEmpty()) {
            return OperationResult.failure(BlankKeywordException(sanitizedKeyword))
        }

        if (sanitizedKeyword.length > KEYWORD_MAX_LENGTH) {
            return OperationResult.failure(InvalidKeywordLengthException(sanitizedKeyword.length, MAX_VALUES))
        }

        return OperationResult.success(sanitizedKeyword)
    }


    sealed class KeywordsValidationException(message: String) : Exception(message) {

        class EmptyKeywordsException : KeywordsValidationException("A lista de valores não pode estar vazia.")

        class MaxKeywordsExceededException(val max: Int, val found: Int) :
            KeywordsValidationException("Número máximo de valores excedido: $found/$max")


    }

    sealed class SingleKeywordValidationException(message: String) : Exception(message) {

        class BlankKeywordException(val value: String) :
            SingleKeywordValidationException("A palavra-chave está em branco ou é inválida: \"$value\"")

        class InvalidKeywordLengthException(val length: Int, val max: Int) :
            SingleKeywordValidationException("A palavra-chave tem comprimento inválido (máximo $max): \"$length\"")
    }
}
