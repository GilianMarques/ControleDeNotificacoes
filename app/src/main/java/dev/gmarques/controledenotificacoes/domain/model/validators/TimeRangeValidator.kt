import dev.gmarques.controledenotificacoes.domain.exceptions.InvalidTimeRangeValueException
import dev.gmarques.controledenotificacoes.domain.exceptions.InversedRangeException
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
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

    /**
     * Valida um [TimeRange], verificando se os valores de hora e minuto estão dentro
     * dos limites aceitáveis e se o horário de início é anterior ao de término.
     *
     * @param timeRange O intervalo de tempo a ser validado.
     * @return [Result.success] com o próprio objeto [TimeRange] se for válido,
     *         ou [Result.failure] com a exceção correspondente em caso de falha.
     */
    fun validate(timeRange: TimeRange): Result<TimeRange> {
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
     * @return Resultado do processo de validação.
     */
    private fun validateAllDay(timeRange: TimeRange): Result<TimeRange> {
        return if (timeRange.startHour == 0 &&
            timeRange.startMinute == 0 &&
            timeRange.endHour == 0 &&
            timeRange.endMinute == 0
        ) {
            Result.success(timeRange)
        } else {
            Result.failure(
                IllegalStateException("Um TimeRange definido como allDay deve ter valores zerados: $timeRange")
            )
        }
    }

    /**
     * Valida se os valores de hora e minuto estão dentro dos intervalos permitidos
     * e se o horário inicial é anterior ao final.
     *
     * @param timeRange O intervalo a ser validado.
     * @return Resultado do processo de validação.
     */
    private fun validateInterval(timeRange: TimeRange): Result<TimeRange> {
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
     * @return [Result.failure] se inválido, ou `null` se válido.
     */
    private fun validateHour(label: String, hour: Int): Result<TimeRange>? {
        return if (hour !in HOUR_RANGE) {
            //   Log.d("USUK", "TimeRangeValidator.validateHour:fail: $label")
            Result.failure(
                InvalidTimeRangeValueException(HOUR_RANGE.first, HOUR_RANGE.last, hour)
            )
        } else null
    }

    /**
     * Verifica se o valor de minuto está dentro do intervalo [0..59].
     *
     * @param label Nome do campo para fins de depuração.
     * @param minute Valor do minuto a ser validado.
     * @return [Result.failure] se inválido, ou `null` se válido.
     */
    private fun validateMinute(label: String, minute: Int): Result<TimeRange>? {
        return if (minute !in MINUTE_RANGE) {
            //Log.d("USUK", "TimeRangeValidator.validateMinute: fail: $label")
            Result.failure(
                InvalidTimeRangeValueException(MINUTE_RANGE.first, MINUTE_RANGE.last, minute)
            )
        } else null
    }

    /**
     * Verifica se o horário inicial é anterior ao final, convertendo ambos os
     * tempos para minutos desde a meia-noite.
     *
     * @param timeRange O intervalo a ser validado.
     * @return [Result.failure] se a ordem estiver incorreta, ou [Result.success] se válida.
     */
    private fun validateTimeOrder(timeRange: TimeRange): Result<TimeRange> {
        val startInMinutes = timeRange.startInMinutes()
        val endInMinutes = timeRange.endInMinutes()

        return if (startInMinutes >= endInMinutes) {
            Result.failure(InversedRangeException(startInMinutes, endInMinutes))
        } else {
            Result.success(timeRange)
        }
    }
}
