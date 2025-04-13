package dev.gmarques.controledenotificacoes.domain.model.validators

import dev.gmarques.controledenotificacoes.domain.exceptions.DuplicateTimeRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.IntersectedRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.OutOfRangeException
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.utils.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.utils.TimeRangeExtensionFun.asRange
import dev.gmarques.controledenotificacoes.domain.utils.TimeRangeExtensionFun.startInMinutes
import java.util.Locale

/**
 * Criado por Gilian Marques
 * Em domingo, 30 de março de 2025 as 13:30.
 */
object RuleValidator {

    const val MIN_NAME_LENGTH = 3
    const val MAX_NAME_LENGTH = 50

    const val MAX_RANGES = 10
    private const val MIN_RANGES = 1


    /**
     * Valida um objeto [Rule] verificando seu nome, dias e intervalos de tempo.
     *
     * Esta função executa uma série de validações no objeto [Rule] fornecido:
     * 1. **Validação de Nome:** Verifica se o nome da regra é válido usando [validateName].
     * 2. **Validação de Dias:** Verifica se os dias da regra são válidos usando [validateDays].
     * 3. **Validação de Intervalos de Tempo:** Verifica se os intervalos de tempo da regra são válidos usando [validateTimeRanges].
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

        validateTimeRanges(rule.timeRanges).getOrThrow()

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
        if (name.isEmpty()) return Result.success(name)

        val trimmedName = name.trim().replace("\\s+".toRegex(), " ")
        val capitalizedName = trimmedName.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault())
                else char.toString()
            }
        }

        if (capitalizedName.length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            return Result.failure(
                OutOfRangeException(MIN_NAME_LENGTH, MAX_NAME_LENGTH, capitalizedName.length)
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
        return if (days.size !in minDays..maxDays) {
            Result.failure(OutOfRangeException(minDays, maxDays, days.size))
        } else Result.success(days)
    }

    /**
     * Valida uma lista de objetos [TimeRange], garantindo que eles atendam aos seguintes critérios:
     * - O número de intervalos de tempo está dentro dos limites permitidos (definidos por `MIN_RANGES` e `MAX_RANGES`).
     * - Não há intervalos de tempo duplicados.
     * - Nenhum intervalo de tempo se sobrepõe (intercepta) a outro.
     * - Cada intervalo de tempo individual na lista é válido (por exemplo, o horário de início é anterior ao horário de término).
     *
     * Se todos os critérios forem atendidos, a função retorna um [Result.success] contendo a lista de intervalos de tempo, ordenados por seu horário de início.
     * Se algum critério não for atendido, a função retorna um [Result.failure] contendo uma exceção que descreve o problema específico.
     *
     * @param ranges A lista de objetos [TimeRange] a serem validados.
     * @return Um objeto [Result]:
     *         - [Result.success] contendo a lista ordenada de [TimeRange] se todos os intervalos forem válidos.
     *         - [Result.failure] contendo uma exceção se alguma validação falhar:
     *           - [OutOfRangeException] se o número de intervalos estiver fora dos limites permitidos.
     *           - Uma [DuplicateTimeRangeException] retornada por [findDuplicateRanges] se existirem intervalos duplicados.
     *           - Uma  [IntersectedRangeException] por [findIntersectedRanges] se existirem intervalos sobrepostos.
     *           - Uma exceção retornada por [validateEachTimeRange] se algum intervalo individual for inválido.
     *           - [OutOfRangeException] se o número de intervalos estiver fora dos limites permitidos.
     *
     */
    fun validateTimeRanges(ranges: List<TimeRange>): Result<List<TimeRange>> {
        if (!isTimeRangeCountValid(ranges)) {
            return Result.failure(OutOfRangeException(MIN_RANGES, MAX_RANGES, ranges.size))
        }

        findDuplicateRanges(ranges)?.let { return Result.failure(it) }

        findIntersectedRanges(ranges)?.let { return Result.failure(it) }

        validateEachTimeRange(ranges)?.let { return Result.failure(it) }

        return Result.success(ranges)
    }

    /**
     * Verifica se há intervalos de tempo duplicados (com os mesmos valores de hora e minuto).
     *
     * @return [DuplicateTimeRangeException] se houver duplicatas, ou null caso contrário.
     */
    private fun findDuplicateRanges(ranges: List<TimeRange>): DuplicateTimeRangeException? {
        // uso pares aninhados pra gerar uma chave, a estrutura é assim:  (((startHour, startMinute), endHour), endMinute)
        val duplicates =
            ranges.groupBy { it.startHour to it.startMinute to it.endHour to it.endMinute }.filter { it.value.size > 1 }
        return if (duplicates.isNotEmpty()) DuplicateTimeRangeException(
            duplicates.values.first()[0], duplicates.values.first()[1]
        ) else null
    }

    /**
     * Verifica se a quantidade de intervalos está dentro dos limites permitidos.
     */
    private fun isTimeRangeCountValid(ranges: List<TimeRange>): Boolean {
        return ranges.size in MIN_RANGES..MAX_RANGES
    }

    /**
     * Verifica se há interseções entre os intervalos.
     */
    private fun findIntersectedRanges(r: List<TimeRange>): IntersectedRangeException? {

        // necessario colocar do maior pro menor pra verificar as interceçoes em apenas um de dois intervalos
        val sortedRanges = r.sortedByDescending { it.startInMinutes() }

        for (i in 0 until sortedRanges.size - 1) {
            val range = sortedRanges[i]
            for (j in i + 1 until sortedRanges.size) {
                val other = sortedRanges[j]
                if (range.startInMinutes() in other.asRange() || range.endInMinutes() in other.asRange()) {
                    return IntersectedRangeException(range, other)
                }
            }
        }
        return null
    }

    /**
     * Valida cada intervalo individualmente e retorna uma exceção se algum for inválido.
     */
    private fun validateEachTimeRange(ranges: List<TimeRange>): Throwable? {
        ranges.forEach { range ->
            val result = TimeRangeValidator.validate(range)
            if (result.isFailure) return result.exceptionOrNull()!!
        }
        return null
    }

}