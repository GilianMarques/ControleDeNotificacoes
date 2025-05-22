package dev.gmarques.controledenotificacoes.domain.model

import android.icu.util.Calendar
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import org.joda.time.LocalDateTime

/**
 * Criado por Gilian Marques
 * Em terça-feira, 20 de maio de 2025 as 13:21.
 */
// TODO: arrumar um nome melhor
// TODO: colocar no pacote adequado
class IntervalCalculator {

    companion object {
        /**
         * Usado para indicar que não foi possível  calcular um período de desbloqueio de um aplicativo porque sua regra
         * implementa um bloqueio de 7 dias de 24 horas por dia 7 dias por semana
         */
        const val INFINITE = -1L
    }

    private lateinit var baseDateTime: LocalDateTime

    /**@param date inicialize apenas para testes em cenario real deixe em branco*/
    fun nextUnlockTime(date: LocalDateTime = LocalDateTime.now(), rule: Rule): Long {

        baseDateTime = date.withSecondsAndMillisSetToZero()

        return when (rule.ruleType) {
            RuleType.RESTRICTIVE -> nextUnlockTimeAfterNowRestrictive(rule)
            RuleType.PERMISSIVE -> nextUnlockTimeFromNowPermissive(rule)
        }
            ?.withSecondsAndMillisSetToZero() /*Zero os segundos e milissegundos porque além de irrelevantes eles atrapalham os testes*/
            ?.toDate()?.time ?: INFINITE

    }

    //Executado quando o primeiro dia de bloqueio da regra é >= o dia de hoje
    private fun nextUnlockTimeAfterNowRestrictive(rule: Rule): LocalDateTime? {

        var todayLocalDateTime = LocalDateTime(baseDateTime)
        val blockDays = rule.days.map { it.dayNumber }
        var goneTroughABlockDay = false
        val nextDay = {
            todayLocalDateTime = todayLocalDateTime.plusDays(1).withMillisOfDay(0)// retorna o proximo dia às 00:00
        }

        repeat(WeekDay.entries.size) {

            val weekDayInt = todayLocalDateTime.weekDayNumber()

            if (weekDayInt in blockDays) goneTroughABlockDay = true
            else {
                if (goneTroughABlockDay) return todayLocalDateTime
                else nextDay(); return@repeat
            }

            val nextUnlockTime = getUnlockPeriodForDay(todayLocalDateTime, rule)
            if (nextUnlockTime != null) return nextUnlockTime

            nextDay()
        }
        return null
    }

    private fun nextUnlockTimeFromNowPermissive(rule: Rule): LocalDateTime? {
        return LocalDateTime.now()
    }

    private fun getUnlockPeriodForDay(
        day: LocalDateTime,
        rule: Rule,
    ): LocalDateTime? {

        if (rule.timeRanges.size == 1 && rule.timeRanges.first().allDay) return null

        val sortedTimeRanges = rule.timeRanges.sortedBy { it.startInMinutes() }

        val chainedRanges = { timeRange: TimeRange, index: Int ->
            timeRange.endInMinutes() + 1 == sortedTimeRanges.getOrNull(index + 1)?.startInMinutes()
        }

        sortedTimeRanges.forEachIndexed { index, timeRange ->

            if (chainedRanges(timeRange, index)) return@forEachIndexed

            if (rule.ruleType == RuleType.RESTRICTIVE) {

                val timeRangeRelative = LocalDateTime(day)
                    .withHourOfDay(timeRange.endHour)
                    .withMinuteOfHour(timeRange.endMinute)

                if (timeRangeRelative.isAfter(day)) return timeRangeRelative.plusMinutes(1)
            }

            if (rule.ruleType == RuleType.PERMISSIVE) {

                val timeRangeRelative = LocalDateTime(day)
                    .withHourOfDay(timeRange.startHour)
                    .withMinuteOfHour(timeRange.startMinute)

                if (timeRangeRelative.isAfter(day)) return timeRangeRelative
            }

        }

        return null
    }

}

private fun LocalDateTime.withSecondsAndMillisSetToZero(): LocalDateTime {
    return this.withSecondOfMinute(0).withMillisOfSecond(0)
}

private fun LocalDateTime.weekDayNumber(): Int {
    return Calendar.getInstance()
        .apply { timeInMillis = this@weekDayNumber.toDate().time }
        .get(Calendar.DAY_OF_WEEK)
}
