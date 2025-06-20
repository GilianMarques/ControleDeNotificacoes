import TimeRangeValidator.TimeRangeValidatorException.RangesOutOfRangeException
import dev.gmarques.controledenotificacoes.domain.OperationResult
import dev.gmarques.controledenotificacoes.domain.exceptions.DuplicateTimeRangeException
import dev.gmarques.controledenotificacoes.domain.exceptions.IntersectedRangeException
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.asRange
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes

/**
 *
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 as 21:49.
 *
 * Objeto responsável por validar instâncias de [TimeRange], garantindo que os valores
 * de hora e minuto estejam dentro dos limites válidos e que o intervalo de tempo seja coerente.
 */
object TimeRangeValidator {

    private val HOUR_RANGE = 0..23
    private val MINUTE_RANGE = 0..59

    /** max [TimeRange]s for [dev.gmarques.controledenotificacoes.domain.model.Rule] */
    const val MAX_RANGES = 10

    /** min [TimeRange]s for [dev.gmarques.controledenotificacoes.domain.model.Rule] */
    const val MIN_RANGES = 1

    /**
     * Valida um [TimeRange], verificando se os valores de hora e minuto estão dentro
     * dos limites aceitáveis e se o horário de início é anterior ao de término.
     *
     * @param timeRange O intervalo de tempo a ser validado.
     * @return [OperationResult.success] com o próprio objeto [TimeRange] se for válido,
     *         ou [OperationResult.failure] com a exceção correspondente em caso de falha.
     */
    fun validate(timeRange: TimeRange): OperationResult<TimeRangeValidatorException, TimeRange> {
        return when {
            timeRange.allDay -> validateAllDay(timeRange)
            else -> validateInterval(timeRange)
        }
    }

    /**
     * Valida se um [TimeRange] definido como "dia inteiro" (allDay) está com todos
     * os valores zerados, como esperado.
     *
     * @param timeRange O intervalo a ser validado.
     * @return OperationResultado do processo de validação.
     */
    private fun validateAllDay(timeRange: TimeRange): OperationResult<TimeRangeValidatorException, TimeRange> {
        return if (timeRange.startHour == 0 && timeRange.startMinute == 0 && timeRange.endHour == 0 && timeRange.endMinute == 0) {
            OperationResult.success(timeRange)
        } else {
            OperationResult.failure(
                TimeRangeValidatorException.AllDayWithNoZeroedValuesException(timeRange)
            )
        }
    }

    /**
     * Valida se os valores de hora e minuto estão dentro dos intervalos permitidos
     * e se o horário inicial é anterior ao final.
     *
     * @param timeRange O intervalo a ser validado.
     * @return OperationResultado do processo de validação.
     */
    private fun validateInterval(timeRange: TimeRange): OperationResult<TimeRangeValidatorException, TimeRange> {
        validateHour("startHour", timeRange.startHour)?.let { return it }
        validateHour("endHour", timeRange.endHour)?.let { return it }
        validateMinute("startMinute", timeRange.startMinute)?.let { return it }
        validateMinute("endMinute", timeRange.endMinute)?.let { return it }

        return validateTimeOrder(timeRange)
    }

    /**
     * Verifica se o valor de hora está dentro do intervalo [0..23].
     *
     * @param label Nome do campo para fins de depuração.
     * @param hour Valor da hora a ser validado.
     * @return [OperationResult.failure] se inválido, ou `null` se válido.
     */
    private fun validateHour(label: String, hour: Int): OperationResult<TimeRangeValidatorException, TimeRange>? {
        return if (hour !in HOUR_RANGE) {
            //   Log.d("USUK", "TimeRangeValidator.validateHour:fail: $label")
            OperationResult.failure(
                TimeRangeValidatorException.HourOutOfRangeException(HOUR_RANGE.first, HOUR_RANGE.last, hour)
            )
        } else null
    }

    /**
     * Verifica se o valor de minuto está dentro do intervalo [0..59].
     *
     * @param label Nome do campo para fins de depuração.
     * @param minute Valor do minuto a ser validado.
     * @return [OperationResult.failure] se inválido, ou `null` se válido.
     */
    private fun validateMinute(label: String, minute: Int): OperationResult<TimeRangeValidatorException, TimeRange>? {
        return if (minute !in MINUTE_RANGE) {
            //Log.d("USUK", "TimeRangeValidator.validateMinute: fail: $label")
            OperationResult.failure(
                TimeRangeValidatorException.MinuteOutOfRangeException(HOUR_RANGE.first, HOUR_RANGE.last, minute)
            )
        } else null
    }

    /**
     * Verifica se o horário inicial é anterior ao final, convertendo ambos os
     * tempos para minutos desde a meia-noite.
     *
     * @param timeRange O intervalo a ser validado.
     * @return [OperationResult.failure] se a ordem estiver incorreta, ou [OperationResult.success] se válida.
     */
    private fun validateTimeOrder(timeRange: TimeRange): OperationResult<TimeRangeValidatorException, TimeRange> {
        val startInMinutes = timeRange.startInMinutes()
        val endInMinutes = timeRange.endInMinutes()

        return if (startInMinutes >= endInMinutes) {
            OperationResult.failure(TimeRangeValidatorException.InversedRangeException(startInMinutes, endInMinutes))
        } else {
            OperationResult.success(timeRange)
        }
    }

    /**
     * Valida uma lista de objetos [TimeRange], garantindo que eles atendam aos seguintes critérios:
     * - O número de intervalos de tempo está dentro dos limites permitidos (definidos por `MIN_RANGES` e `MAX_RANGES`).
     * - Não há intervalos de tempo duplicados.
     * - Nenhum intervalo de tempo se sobrepõe (intercepta) a outro.
     * - Cada intervalo de tempo individual na lista é válido (por exemplo, o horário de início é anterior ao horário de término).
     *
     * Se todos os critérios forem atendidos, a função retorna um [OperationResult.success] contendo a lista de intervalos de tempo, ordenados por seu horário de início.
     * Se algum critério não for atendido, a função retorna um [OperationResult.failure] contendo uma exceção que descreve o problema específico.
     *
     * @param ranges A lista de objetos [TimeRange] a serem validados.
     * @return Um objeto [OperationResult]:
     *         - [OperationResult.success] contendo a lista ordenada de [TimeRange] se todos os intervalos forem válidos.
     *         - [OperationResult.failure] contendo uma exceção se alguma validação falhar:
     *           - [OutOfRangeException] se o número de intervalos estiver fora dos limites permitidos.
     *           - [InvalidTimeRangeValueException] se algum intervalo individual for inválido.
     *           - Uma [DuplicateTimeRangeException] retornada por [findDuplicateRanges] se existirem intervalos duplicados.
     *           - Uma  [IntersectedRangeException] por [findIntersectedRanges] se existirem intervalos sobrepostos.
     *           - Uma exceção retornada por [validateEachTimeRange] se algum intervalo individual for inválido.
     *           - [OutOfRangeException] se o número de intervalos estiver fora dos limites permitidos.
     *
     */
    fun validateTimeRanges(ranges: List<TimeRange>): OperationResult<TimeRangeValidatorException, List<TimeRange>> {

        if (!isTimeRangeCountValid(ranges)) {
            return OperationResult.failure(RangesOutOfRangeException(MIN_RANGES, MAX_RANGES, ranges.size))
        }
// TODO: erros retornados nao herdam da classe certa
        findDuplicateRanges(ranges)?.let { return OperationResult.failure(it) }

        findIntersectedRanges(ranges)?.let { return OperationResult.failure(it) }

        validateEachTimeRange(ranges)?.let { return OperationResult.failure(it) }

        return OperationResult.success(ranges)
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

        if (r.size > 1 && r.any { it.allDay }) return IntersectedRangeException(r[0], r[1])

        // necessario colocar do maior pro menor pra verificar as interceçoes em apenas um de dois intervalos
        val sortedRanges = r.sortedByDescending { it.startInMinutes() }

        for (i in 0..sortedRanges.size) {

            val range = sortedRanges[i]
            val nextRange = sortedRanges.getOrNull(i + 1)
            if (nextRange == null) return null

            if (range.startInMinutes() in nextRange.asRange() || range.endInMinutes() in nextRange.asRange()) {
                return IntersectedRangeException(range, nextRange)
            }
        }
        return null
    }

    /**
     * Valida cada intervalo individualmente e retorna uma exceção se algum for inválido.
     */
    private fun validateEachTimeRange(ranges: List<TimeRange>): Throwable? {
        ranges.forEach { range ->
            val result = validate(range)
            if (result.isFailure) return result.exceptionOrNull()!!
        }
        return null
    }

    sealed class TimeRangeValidatorException(msg: String) : Exception(msg) {

        /**
         * Criado por Gilian Marques
         * Em 20/06/2025 as 17:49
         */
        class RangesOutOfRangeException(
            val minLength: Int,
            val maxLength: Int,
            val actual: Int,
        ) : TimeRangeValidatorException("O range valido é de $minLength a $maxLength. valor atual: $actual")

        /**
         * Criado por Gilian Marques
         * Em 20/06/2025 as 17:49
         */
        class AllDayWithNoZeroedValuesException(timeRange: TimeRange) :
            TimeRangeValidatorException("Um TimeRange definido como allDay deve ter valores zerados: $timeRange")

        /**
         * Criado por Gilian Marques
         * Em 20/06/2025 as 17:49
         */
        class HourOutOfRangeException(minHour: Int, maxHour: Int, actualHour: Int) :
            TimeRangeValidatorException("A hora deve estar entre $minHour e $maxHour. Valor atual: $actualHour")

        /**
         * Criado por Gilian Marques
         * Em 20/06/2025 as 17:49
         */
        class MinuteOutOfRangeException(minMin: Int, maxMin: Int, actualMinute: Int) :
            TimeRangeValidatorException("Os minutos devem estar entre $minMin e $maxMin. Valor atual: $actualMinute")

        /**
         * Criado por Gilian Marques
         * Em 20/06/2025 as 17:49
         */
        class InversedRangeException(
            private val startIntervalMinutes: Int,
            private val endIntervalMinutes: Int,
        ) : TimeRangeValidatorException("O inicio do intervalo nao pode acontecer após o fim do mesmo startIntervalMinutes: $startIntervalMinutes endIntervalMinutes: $endIntervalMinutes")

    }
}
