package dev.gmarques.controledenotificacoes.domain.model

import TimeRangeValidator
import dev.gmarques.controledenotificacoes.domain.OperationResult
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.RuleValidatorException.BlankIdException
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.RuleValidatorException.ConditionValidationException
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.RuleValidatorException.NameOutOfRangeException
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.validateDays
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.validateId
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.validateName
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator.validateTimeRanges
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import java.util.Locale

/**
 * Criado por Gilian Marques
 * Em domingo, 30 de março de 2025 as 13:30.
 */
object RuleValidator {

    const val MIN_NAME_LENGTH = 3
    const val MAX_NAME_LENGTH = 50


    /**
     * Valida um objeto [Rule].
     *
     * Essa função realiza uma série de validações em um objeto [Rule], incluindo:
     * - **Nome**: Verifica a validade do nome usando [validateName].
     * - **Dias**: Verifica a validade dos dias usando [validateDays].
     * - **Intervalos de Tempo**: Verifica a validade dos intervalos de tempo usando [validateTimeRanges].
     * - **Id**: Verifica se a Id está vazia [validateId].
     *
     * Se alguma dessas validações falhar, uma exceção será lançada. A exceção lançada
     * corresponde à falha específica da validação.
     *
     * @param rule O objeto [Rule] que será validado.
     * @throws Exception Lançada quando uma das validações falha. O tipo de exceção depende da falha específica.
     * - se a função de validação ([validateName], [validateDays], [validateTimeRanges]) retornar um [OperationResult.failure] e conter uma exceção, essa exceção será lançada.
     * - Se não tiver exceção, o sistema lançará uma exceção padrão.
     */
    fun validate(rule: Rule) {

        validateName(rule.name).getOrThrow()

        validateDays(rule.days).getOrThrow()

        validateTimeRanges(rule.timeRanges).getOrThrow()

        validateId(rule.id).getOrThrow()

        validateCondition(rule.condition)

    }


    /**
     * Valida uma string de nome fornecida de acordo com as seguintes regras:
     *
     * 2. **Tratamento de Espaços em Branco:** Espaços em branco iniciais e finais são removidos. Múltiplos espaços entre palavras são reduzidos a um único espaço.
     * 3. **Capitalização:** Cada palavra no nome é capitalizada (primeira letra maiúscula, o restante minúsculo).
     * 4. **Verificação de Comprimento:** O nome capitalizado resultante deve estar dentro do intervalo de comprimento especificado (inclusivo).
     *
     * @param name A string de nome a ser validada.
     * @return Um objeto [Result].
     *         - Se o nome for válido, retorna [OperationResult.success] contendo o nome validado (sem espaços extras, corretamente espaçado e capitalizado).
     *         - Se o nome for inválido, retorna [OperationResult.failure] contendo uma exceção:
     *           - [BlankNameException] se o nome estiver em branco.
     *           - [NameOutOfRangeException] se o comprimento do nome capitalizado estiver fora do intervalo permitido.
     */
    fun validateName(name: String): OperationResult<RuleValidatorException, String> {
        if (name.isEmpty()) return OperationResult.success(name)

        val trimmedName = name.trim().replace("\\s+".toRegex(), " ")
        val capitalizedName = trimmedName.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault())
                else char.toString()
            }
        }

        if (capitalizedName.length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            return OperationResult.failure(
                NameOutOfRangeException(MIN_NAME_LENGTH, MAX_NAME_LENGTH, capitalizedName.length)
            )
        }

        return OperationResult.success(capitalizedName)

    }

    /**
     * Valida uma lista de dias da semana.
     *
     * Garante que a quantidade de dias na lista esteja dentro de um intervalo específico.
     *
     * @param days A lista de dias da semana a ser validada.
     * @return Um objeto Result que contém:
     *         - Sucesso: A lista de dias da semana, se a validação for bem-sucedida.
     *         - Falha: Uma exceção `OutOfRangeException` se a quantidade de dias estiver fora do intervalo permitido.
     *
     * @throws NameOutOfRangeException Se a quantidade de dias na lista estiver fora do intervalo permitido (entre 1 e 7, inclusive).
     */
    fun validateDays(days: List<WeekDay>): OperationResult<RuleValidatorException, List<WeekDay>> {
        val minDays = 1
        val maxDays = 7
        return if (days.size !in minDays..maxDays) {
            OperationResult.failure(RuleValidatorException.DaysOutOfRangeException(minDays, maxDays, days.size))
        } else OperationResult.success(days)
    }

    private fun validateTimeRanges(ranges: List<TimeRange>): OperationResult<TimeRangeValidator.TimeRangeValidatorException, List<TimeRange>> {
        return TimeRangeValidator.validateTimeRanges(ranges)
    }

    /**
     * Valida o ID fornecido, verificando se ele está vazio.
     *
     * Esta função verifica se a string de ID fornecida está vazia. Se estiver, ela retorna
     * um [OperationResult.failure] contendo uma exceção [BlankIdException] com uma mensagem
     * detalhada explicando que a ID de um objeto não pode estar vazia. Se a ID não estiver
     * vazia, ela retorna um [OperationResult.success] contendo a própria ID.
     *
     * @param id A string de ID a ser validada.
     * @return Um objeto [Result]:
     *         - [OperationResult.success] contendo a string de ID se ela não estiver vazia.
     *         - [OperationResult.failure] contendo uma [BlankIdException] se a string de ID estiver vazia.
     */
    fun validateId(id: String): OperationResult<RuleValidatorException, String> {
        if (id.isEmpty()) {
            return OperationResult.failure(BlankIdException())
        }
        return OperationResult.success(id)
    }

    private fun validateCondition(condition: Condition?): OperationResult<RuleValidatorException, Condition?> {

        if (condition == null) return OperationResult.success(condition)

        try {
            ConditionValidator.validate(condition)
            return OperationResult.success(condition)
        } catch (ex: ConditionValidationException) {
            return OperationResult.failure(ConditionValidationException(ex))
        }

    }

    /**
     * Criado por Gilian Marques
     * Em 20/06/2025 as 17:18
     */
    sealed class RuleValidatorException(msg: String) : Exception(msg) {

        /**
         * Criado por Gilian Marques
         * Em domingo, 30 de março de 2025 as 14:21.
         */
        class NameOutOfRangeException(
            val minLength: Int,
            val maxLength: Int,
            val actual: Int,
        ) : RuleValidatorException("O range valido é de $minLength a $maxLength. valor atual: $actual")

        /**
         * Criado por Gilian Marques
         * Em 23/06/2025 as 17:31
         */
        class DaysOutOfRangeException(
            val minDays: Int,
            val maxDays: Int,
            val actual: Int,
        ) : RuleValidatorException("A quantidade de dias deve estar entre $minDays e $maxDays. Valor atual: $actual")

        /**
         * Criado por Gilian Marques
         * Em domingo, 30 de março de 2025 as 14:22.
         */
        class BlankIdException() :
            RuleValidatorException("Em hipótese alguma a id de um objeto pode ficar vazia. Ela é gerada automaticamente e imutavel, por tanto algo deu muito errado pra isso acontecer.")

        /**
         * Criado por Gilian Marques
         * Em 23/06/2025 as 15:46
         *
         * Serve para encapsular as exceções retornadas pelo [TimeRangeValidator.TimeRangeValidatorException]
         */
        class TimeRangeValidationException(val exception: TimeRangeValidator.TimeRangeValidatorException) :
            RuleValidatorException("O seguinte erro foi lançado durante a validação de timeranges: ${exception.message} ")

        /**
         * Criado por Gilian Marques
         * Em 29/06/2025 as 10:47
         * */
        class ConditionValidationException(val exception: ConditionValidationException) :
            RuleValidatorException("Erro durante a validação da condição: ${exception.message}")

    }
}