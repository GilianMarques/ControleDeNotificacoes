import TimeRangeValidator.TimeRangeValidatorException.AllDayWithNoZeroedValuesException
import TimeRangeValidator.TimeRangeValidatorException.DuplicateTimeRangeException
import TimeRangeValidator.TimeRangeValidatorException.HourOutOfRangeException
import TimeRangeValidator.TimeRangeValidatorException.IntersectedRangeException
import TimeRangeValidator.TimeRangeValidatorException.InversedRangeException
import TimeRangeValidator.TimeRangeValidatorException.MinuteOutOfRangeException
import TimeRangeValidator.TimeRangeValidatorException.RangesOutOfRangeException
import dev.gmarques.controledenotificacoes.domain.OperationResult
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
     *         ou [OperationResult.failure] com a exceção correspondente em caso de falha:
     *         - [AllDayWithNoZeroedValuesException] Se `timeRange.allDay` for verdadeiro e os valores não estiverem zerados.
     *         - [HourOutOfRangeException] Se a hora de início ou fim estiver fora do intervalo permitido.
     *         - [MinuteOutOfRangeException] Se o minuto de início ou fim estiver fora do intervalo permitido.
     *         - [InversedRangeException] Se o horário de início for posterior ao horário de término.
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
     * @return [OperationResult.success] com o próprio objeto [TimeRange] se for válido,
     *         ou [OperationResult.failure] com [AllDayWithNoZeroedValuesException] se `timeRange.allDay` for verdadeiro e os valores não estiverem zerados.
     */
    private fun validateAllDay(timeRange: TimeRange): OperationResult<AllDayWithNoZeroedValuesException, TimeRange> {
        return if (timeRange.startHour == 0 && timeRange.startMinute == 0 && timeRange.endHour == 0 && timeRange.endMinute == 0) {
            OperationResult.success(timeRange)
        } else {
            OperationResult.failure(
                AllDayWithNoZeroedValuesException(timeRange)
            )
        }
    }

    /**
     * Valida se os valores de hora e minuto estão dentro dos intervalos permitidos
     * e se o horário inicial é anterior ao final.
     *
     * @param timeRange O intervalo a ser validado.
     * @return [OperationResult.success] com o próprio objeto [TimeRange] se for válido,
     *         ou [OperationResult.failure] com a exceção correspondente em caso de falha:
     *         - [HourOutOfRangeException] Se a hora de início ou fim estiver fora do intervalo permitido.
     *         - [MinuteOutOfRangeException] Se o minuto de início ou fim estiver fora do intervalo permitido.
     *         - [InversedRangeException] Se o horário de início for posterior ao horário de término.
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
     * @return [OperationResult.failure] com [HourOutOfRangeException] se o valor da hora estiver fora do intervalo [0..23],
     *         ou `null` se válido.
     */
    private fun validateHour(label: String, hour: Int): OperationResult<HourOutOfRangeException, TimeRange>? {
        return if (hour !in HOUR_RANGE) {
            //   Log.d("USUK", "TimeRangeValidator.validateHour:fail: $label")
            OperationResult.failure(
                HourOutOfRangeException(HOUR_RANGE.first, HOUR_RANGE.last, hour)
            )
        } else null
    }

    /**
     * Verifica se o valor de minuto está dentro do intervalo [0..59].
     *
     * @param label Nome do campo para fins de depuração.
     * @param minute Valor do minuto a ser validado.
     * @return [OperationResult.failure] com [MinuteOutOfRangeException] se o valor do minuto estiver fora do intervalo [0..59],
     *         ou `null` se válido.
     */
    private fun validateMinute(label: String, minute: Int): OperationResult<MinuteOutOfRangeException, TimeRange>? {
        return if (minute !in MINUTE_RANGE) {
            //Log.d("USUK", "TimeRangeValidator.validateMinute: fail: $label")
            OperationResult.failure(
                MinuteOutOfRangeException(HOUR_RANGE.first, HOUR_RANGE.last, minute)
            )
        } else null
    }

    /**
     * Verifica se o horário inicial é anterior ao final, convertendo ambos os
     * tempos para minutos desde a meia-noite.
     *
     * @param timeRange O intervalo a ser validado.
     * @return [OperationResult.failure] com [InversedRangeException] se o horário de início for posterior ou igual ao horário de término,
     *         ou [OperationResult.success] se válida.
     */
    private fun validateTimeOrder(timeRange: TimeRange): OperationResult<InversedRangeException, TimeRange> {
        val startInMinutes = timeRange.startInMinutes()
        val endInMinutes = timeRange.endInMinutes()

        return if (startInMinutes >= endInMinutes) {
            OperationResult.failure(InversedRangeException(startInMinutes, endInMinutes))
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
     *         - [OperationResult.failure] contendo uma exceção específica se alguma validação falhar:
     *           - [RangesOutOfRangeException] se o número de intervalos estiver fora dos limites permitidos.
     *           - [DuplicateTimeRangeException] se existirem intervalos duplicados.
     *           - [IntersectedRangeException] se existirem intervalos sobrepostos.
     *           - Qualquer exceção de [TimeRangeValidatorException] (como [AllDayWithNoZeroedValuesException],
     *             [HourOutOfRangeException], [MinuteOutOfRangeException], [InversedRangeException]) se algum intervalo individual for inválido.
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
     * @param ranges A lista de [TimeRange] a ser verificada.
     * @return [DuplicateTimeRangeException] se forem encontrados intervalos de tempo duplicados, ou `null` caso contrário.
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
     * @param r A lista de [TimeRange] a ser verificada.
     * @return [IntersectedRangeException] se houver interseção, ou `null` caso contrário.
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
     * @param ranges A lista de [TimeRange] a ser validada.
     * @return A primeira [TimeRangeValidatorException] encontrada durante a validação de um intervalo,
     *         ou `null` se todos os intervalos forem válidos.
     */
    private fun validateEachTimeRange(ranges: List<TimeRange>): TimeRangeValidatorException? {
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
        class HourOutOfRangeException(val minHour: Int, val maxHour: Int, val actualHour: Int) :
            TimeRangeValidatorException("A hora deve estar entre $minHour e $maxHour. Valor atual: $actualHour")

        /**
         * Criado por Gilian Marques
         * Em 20/06/2025 as 17:49
         */
        class MinuteOutOfRangeException(val minMin: Int, val maxMin: Int, val actualMinute: Int) :
            TimeRangeValidatorException("Os minutos devem estar entre $minMin e $maxMin. Valor atual: $actualMinute")

        /**
         * Criado por Gilian Marques
         * Em 20/06/2025 as 17:49
         */
        class InversedRangeException(
            private val startIntervalMinutes: Int,
            private val endIntervalMinutes: Int,
        ) : TimeRangeValidatorException("O inicio do intervalo nao pode acontecer após o fim do mesmo startIntervalMinutes: $startIntervalMinutes endIntervalMinutes: $endIntervalMinutes")

        /**
         * Criado por Gilian Marques
         * Em terça-feira, 08 de abril de 2025 as 22:24.
         */
        class DuplicateTimeRangeException(
            private val interval: TimeRange,
            private val otherInterval: TimeRange,
        ) : TimeRangeValidatorException("Existem dois ou mais intervalos de tempo iguais na lista:\n$interval\n$otherInterval")

        /**
         * Criado por Gilian Marques
         * Em domingo, 06 de março de 2025 as 20:54.
         */
        class IntersectedRangeException(
            private val range1: TimeRange,
            private val range2: TimeRange,
        ) : TimeRangeValidatorException(
            "Os intervalos fazem interseção entre si, de forma que um intervalo se inicia e/ou se encerra dentro de outro intervalo." +
                    "\n$range1 e \n$range2"
        )
    }
}
