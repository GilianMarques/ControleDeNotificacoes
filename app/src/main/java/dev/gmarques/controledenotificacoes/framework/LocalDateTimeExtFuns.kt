package dev.gmarques.controledenotificacoes.framework

import android.icu.util.Calendar
import org.joda.time.LocalDateTime

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 28 de maio de 2025 as 16:10.
 */
object LocalDateTimeExtFuns {

    /**
     * Retorna um novo [LocalDateTime] com os valores de hora e minuto definidos conforme os parâmetros.
     *
     * @param hour Hora desejada (0–23)
     * @param minute Minuto desejado (0–59)
     */
    fun LocalDateTime.at(hour: Int, minute: Int): LocalDateTime = this.withHourOfDay(hour).withMinuteOfHour(minute)

    /**
     * Zera segundos e milissegundos de um [LocalDateTime].
     * Útil para garantir consistência em comparações e testes.
     */
    fun LocalDateTime.withSecondsAndMillisSetToZero(): LocalDateTime {
        return this.withSecondOfMinute(0).withMillisOfSecond(0)
    }

    /**
     * Retorna o número do dia da semana correspondente ao [LocalDateTime],
     * utilizando a enumeração do Android `Calendar.DAY_OF_WEEK` (1 = Domingo, 7 = Sábado).
     */
    fun LocalDateTime.weekDayNumber(): Int {
        return Calendar.getInstance().apply { timeInMillis = this@weekDayNumber.toDate().time }
            .get(Calendar.DAY_OF_WEEK)
    }
}