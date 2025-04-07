import dev.gmarques.controledenotificacoes.domain.exceptions.InversedIntervalException
import dev.gmarques.controledenotificacoes.domain.exceptions.OutOfRangeException
import dev.gmarques.controledenotificacoes.domain.model.TimeRange

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 as 21:49.
 */
object TimeRangeValidator {

    /**
     * Valida um TimeRange para garantir que ele representa um intervalo de tempo válido.
     *
     * Esta função verifica o seguinte:
     * 1. **Intervalo de Horas:** `startHour` e `endHour` devem estar dentro do intervalo de 0 a 23 (inclusive).
     * 2. **Intervalo de Minutos:** `startMinute` e `endMinute` devem estar dentro do intervalo de 0 a 59 (inclusive).
     * 3. **Ordem do Intervalo:** O horário de início deve ser anterior ao horário de término. Isso é determinado convertendo os horários de início e término para minutos desde a meia-noite e comparando-os.
     *
     * @param timeRange O TimeRange a ser validado.
     * @return Um objeto Result.
     *   - **Success:** Se o TimeRange for válido, um Result.success contendo o TimeRange original é retornado.
     *   - **Failure:** Se alguma das verificações de validação falhar, um Result.failure é retornado, contendo uma das seguintes exceções:
     *     - **OutOfRangeException:** Se algum dos valores de hora ou minuto estiver fora de seus intervalos válidos. A mensagem de exceção indica qual campo está fora do intervalo e o intervalo permitido.
     *     - **InversedIntervalException:** Se o horário de início for igual ou posterior ao horário de término. A mensagem de exceção fornece os horários de início e término em minutos.
     *
     * @throws OutOfRangeException Se a hora ou minuto não estiverem no intervalo especificado.
     * @throws InversedIntervalException Se o horário de início for igual ou posterior ao horário de término.
     */
    fun validate(timeRange: TimeRange): Result<TimeRange> {

        val hourRange = 0..23
        val minuteRange = 0..59


        if (timeRange.startHour !in hourRange) return Result.failure(
            OutOfRangeException(
                "startHour: ${timeRange.startHour}", hourRange.first, hourRange.last
            )
        )

        if (timeRange.endHour !in hourRange) return Result.failure(
            OutOfRangeException(
                "endHour: ${timeRange.endHour}", hourRange.first, hourRange.last
            )
        )

        if (timeRange.startMinute !in minuteRange) return Result.failure(
            OutOfRangeException(
                "startMinute: ${timeRange.startMinute}", minuteRange.first, minuteRange.last
            )
        )

        if (timeRange.endMinute !in minuteRange) return Result.failure(
            OutOfRangeException(
                "endMinute: ${timeRange.endMinute}", minuteRange.first, minuteRange.last
            )
        )

        val startPeriodMinutes = timeRange.startHour * 60 + timeRange.startMinute
        val endPeriodMinutes = timeRange.endHour * 60 + timeRange.endMinute

        if (startPeriodMinutes >= endPeriodMinutes) return Result.failure(
            InversedIntervalException(startPeriodMinutes, endPeriodMinutes)
        )

        return Result.success(timeRange)
    }

}