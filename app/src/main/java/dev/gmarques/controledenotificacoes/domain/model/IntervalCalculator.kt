package dev.gmarques.controledenotificacoes.domain.model

import android.icu.util.Calendar
import android.util.Log
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
            RuleType.RESTRICTIVE -> evaluateBlockDaysRestrictive(rule)
            RuleType.PERMISSIVE -> nextUnlockTimeFromNowPermissive(rule)
        }?.withSecondsAndMillisSetToZero() /*Zero os segundos e milissegundos porque além de irrelevantes eles atrapalham os testes*/
            ?.toDate()?.time ?: INFINITE

    }

    private fun evaluateBlockDaysRestrictive(rule: Rule): LocalDateTime? {

        val weekDayInt = Calendar.getInstance().apply { timeInMillis = baseDateTime.toDate().time }.get(Calendar.DAY_OF_WEEK)

        val blockDays = rule.days.map { it.dayNumber }.sortedBy { it }

        // se o primeiro dia de bloqueio é >= terça
        return if (blockDays.first() >= weekDayInt) nextUnlockTimeAfterNowRestrictive(rule)
        else nextUnlockTimeBeforeNowRestrictive(rule)

    }

    //Executado quando o primeiro dia de bloqueio da regra é < terça
    private fun nextUnlockTimeBeforeNowRestrictive(rule: Rule): LocalDateTime? {

        var todayLocalDateTime = LocalDateTime(baseDateTime)
        val blockDays = rule.days.map { it.dayNumber }

        val nextDay = {
            todayLocalDateTime = todayLocalDateTime.plusDays(1).withMillisOfDay(0)// retorna o proximo dia às 00:00
        }

        repeat(WeekDay.entries.size) {

            val weekDayInt = todayLocalDateTime.weekDayInt()

            /* Se o dia não está na lista de bloqueio da regra significa que o app está desbloqueado, porém, essa função busca
             o próximo período de desbloqueio sem considerar o atual por esse motivo, ao invés de retornar aqui, busco o fim
             do próximo período de bloqueio para retornar.
             */
            if (weekDayInt !in blockDays) {
                nextDay()
                return@repeat
            }

            val nextUnlockTime = getUnlockPeriodForDate(todayLocalDateTime, rule)
            if (nextUnlockTime != null) return nextUnlockTime

            nextDay()
        }

        return null
    }

    //Executado quando o primeiro dia de bloqueio da regra é >= o dia de hoje
    private fun nextUnlockTimeAfterNowRestrictive(rule: Rule): LocalDateTime? {
        Log.d("USUK", "IntervalCalculator.".plus("nextUnlockTimeAfterNowRestrictive() rule = $rule"))

        var todayLocalDateTime = LocalDateTime(baseDateTime)
        val blockDays = rule.days.map { it.dayNumber }
        var goneTroughABlockDay = false
        val nextDay = {
            todayLocalDateTime = todayLocalDateTime.plusDays(1).withMillisOfDay(0)// retorna o proximo dia às 00:00
        }

        repeat(WeekDay.entries.size) {

            val weekDayInt = todayLocalDateTime.weekDayInt()

            if (weekDayInt in blockDays) goneTroughABlockDay = true
            else {
                if (goneTroughABlockDay) return todayLocalDateTime
                else nextDay(); return@repeat
            }

            val nextUnlockTime = getUnlockPeriodForDate(todayLocalDateTime, rule)
            if (nextUnlockTime != null) return nextUnlockTime

            nextDay()
        }
        return null
    }

    private fun nextUnlockTimeFromNowPermissive(rule: Rule): LocalDateTime? {
        return LocalDateTime.now()
    }

    private fun getUnlockPeriodForDate(
        currentTime: LocalDateTime,
        rule: Rule,
    ): LocalDateTime? {


        if (rule.timeRanges.size == 1 && rule.timeRanges.first().allDay) return null.also {
            Log.d(
                "USUK", "IntervalCalculator.iterateOverTimeRanges: all day blocked for day: $currentTime and rule: $rule"
            )
        }

        val sortedTimeRanges = rule.timeRanges.sortedBy { it.startInMinutes() }

        sortedTimeRanges.forEach { timeRange ->

            if (rule.ruleType == RuleType.RESTRICTIVE) {

                val timeRangeRelative =
                    LocalDateTime(currentTime).withHourOfDay(timeRange.endHour).withMinuteOfHour(timeRange.endMinute)

                if (timeRangeRelative.isAfter(currentTime)) return timeRangeRelative.plusMinutes(1).also {
                    Log.d(
                        "USUK",
                        "IntervalCalculator.iterateOverTimeRanges: RESTRICTIVE next unlock period found: timeRangeRelative $timeRangeRelative currentTime $currentTime"
                    )
                }
            }

            if (rule.ruleType == RuleType.PERMISSIVE) {

                val timeRangeRelative =
                    LocalDateTime(currentTime).withHourOfDay(timeRange.startHour).withMinuteOfHour(timeRange.startMinute)

                if (timeRangeRelative.isAfter(currentTime)) return timeRangeRelative.also {
                    Log.d(
                        "USUK",
                        "IntervalCalculator.iterateOverTimeRanges: PERMISSIVE next unlock period found: timeRangeRelative $timeRangeRelative currentTime $currentTime"
                    )
                }
            }


        }

        return null
    }


}

private fun LocalDateTime.withSecondsAndMillisSetToZero(): LocalDateTime {
    return this.withSecondOfMinute(0).withMillisOfSecond(0)
}

private fun LocalDateTime.weekDayInt(): Int {
    return Calendar.getInstance()
        .apply { timeInMillis = this@weekDayInt.toDate().time }
        .get(Calendar.DAY_OF_WEEK)
}
