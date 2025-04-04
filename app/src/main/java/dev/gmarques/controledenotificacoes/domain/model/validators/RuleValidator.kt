package dev.gmarques.controledenotificacoes.domain.model.validators

import dev.gmarques.controledenotificacoes.domain.exceptions.BlankNameException
import dev.gmarques.controledenotificacoes.domain.exceptions.OutOfRangeException
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import java.util.Locale

/**
 * Criado por Gilian Marques
 * Em domingo, 30 de março de 2025 as 13:30.
 */
object RuleValidator {

    const val MIN_NAME_LENGTH = 3
    const val MAX_NAME_LENGTH = 50

    const val MAX_INTERVALS = 10
    private const val MIN_INTERVALS = 1

    private val baseException = Exception("A validação falhou mas não retornou exceção para lançar, isso é um bug!")

    /**
     * Valida um objeto [Rule] verificando seu nome, dias e intervalos de tempo.
     *
     * Esta função executa uma série de validações no objeto [Rule] fornecido:
     * 1. **Validação de Nome:** Verifica se o nome da regra é válido usando [validateName].
     * 2. **Validação de Dias:** Verifica se os dias da regra são válidos usando [validateDays].
     * 3. **Validação de Intervalos de Tempo:** Verifica se os intervalos de tempo da regra são válidos usando [validateTimeIntervals].
     *
     * Se alguma dessas validações falhar, a função lança uma exceção. A exceção específica lançada
     * depende do resultado da validação individual.
     *
     * - Se a função de validação (por exemplo, [validateName]) retornar um `Result.failure` e tiver uma exceção
     *   associada, essa exceção será lançada.
     * - Se a função de validação retornar um `Result.failure`, mas não tiver uma exceção associada,
     *   uma `baseException` padrão será lançada.
     *
     * @param rule O objeto [Rule] a ser validado.
     * @throws Exception Uma exceção se alguma das verificações de validação falhar. O tipo de exceção depende
     *                   da falha de validação específica, mas será a exceção retornada pela função de validação
     *                   que falhou ou `baseException` se a validação que falhou não retornar uma exceção.
     */
    fun validate(rule: Rule) {

        validateName(rule.name).getOrThrow()

        validateDays(rule.days).getOrThrow()

        validateTimeIntervals(rule.timeIntervals).getOrThrow()

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
     *         - Se o nome for válido, retorna [Result.success] contendo o nome validado (sem espaços extras, corretamente espaçado e capitalizado).
     *         - Se o nome for inválido, retorna [Result.failure] contendo uma exceção:
     *           - [BlankNameException] se o nome estiver em branco.
     *           - [OutOfRangeException] se o comprimento do nome capitalizado estiver fora do intervalo permitido.
     *
     * @throws OutOfRangeException se o comprimento do nome capitalizado estiver fora do intervalo permitido.
     */
    fun validateName(name: String): Result<String> {



        val trimmedName = name.trim().replace("\\s+".toRegex(), " ")
        val capitalizedName = trimmedName.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault())
                else char.toString()
            }
        }

        if (capitalizedName.length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            return Result.failure(
                OutOfRangeException("capitalizedName: ${capitalizedName.length}", MIN_NAME_LENGTH, MAX_NAME_LENGTH)
            )
        }

        return Result.success(capitalizedName)

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
     * @throws OutOfRangeException Se a quantidade de dias na lista estiver fora do intervalo permitido (entre 1 e 7, inclusive).
     */
    fun validateDays(days: List<WeekDay>): Result<List<WeekDay>> {
        val minDays = 1
        val maxDays = 7
        return if (days.size !in minDays..maxDays) Result.failure(OutOfRangeException("days: ${days.size}", minDays, maxDays))
        else Result.success(days)
    }

    /**
     * Valida uma lista de intervalos de tempo.
     *
     * Esta função verifica se o número de intervalos de tempo está dentro de um intervalo válido (1 a 10, inclusive)
     * e se cada intervalo de tempo individual é válido de acordo com o `TimeIntervalValidator`.
     *
     * @param timeIntervals A lista de objetos [TimeInterval] a serem validados.
     * @return Um objeto [Result]:
     *   - Se a validação for bem-sucedida, retorna um [Result.success] contendo a lista original de objetos [TimeInterval].
     *   - Se a validação falhar, retorna um [Result.failure] contendo uma exceção:
     *     - [OutOfRangeException]: Se o número de intervalos de tempo estiver fora do intervalo permitido (1 a 10).
     *     - Uma exceção de [TimeIntervalValidator.validate]: Se algum dos intervalos de tempo individuais for inválido.
     *       A exceção pode ser qualquer uma que `TimeIntervalValidator.validate` possa retornar. Se o resultado do validador não contiver uma exceção, uma `baseException` genérica será retornada.
     * @throws OutOfRangeException se o número de intervalos de tempo não estiver dentro do intervalo válido.
     * @throws Exception se qualquer TimeInterval individual for inválido de acordo com TimeIntervalValidator.validate.
     */
    fun validateTimeIntervals(timeIntervals: List<TimeInterval>): Result<List<TimeInterval>> {

        if (timeIntervals.size !in MIN_INTERVALS..MAX_INTERVALS) return Result.failure(
            OutOfRangeException("hours: ${timeIntervals.size}", MIN_INTERVALS, MAX_INTERVALS)
        )

        timeIntervals.forEach { timeInterval ->
            with(TimeIntervalValidator.validate(timeInterval)) {
                if (isFailure) return Result.failure(exceptionOrNull() ?: baseException)
            }
        }

        return Result.success(timeIntervals)
    }
}