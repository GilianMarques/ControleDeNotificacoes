package dev.gmarques.controledenotificacoes.domain.model

import android.icu.util.Calendar
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
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

        /**
         * # Iteração de 8 dias
         * Em alguns casos, pode ser necessário iterar sobre 8 dias para obter o valor correto do proximo periodo de desbloqueio do app.
         * Ex: Tentar calcular o proximo periodo de desbloqueio passando uma regra Permissiva em Terça-feira, das 08:00-18:00 e uma data Terça-feira ás 12:20,
         * fará com que o loop tenha que iterar de uma terça, até a outra, incluindo ambas, ou seja, 8 dias. Isso acontce pq a função busca o proximo periodo
         * iterar até encontrar um periodo de bloqueio, nesse caso representado pelos dias ausentes na regra (qua-qui-sex-sab-dom-seg) para
         * retornar o primeiro periodo de desbloqueio após eles, que será a próxima terça ás 08:00.
         */
        const val REPEAT_COUNT = 8
    }

    private lateinit var baseDateTime: LocalDateTime

    /**@param date inicialize apenas para testes em cenario real deixe em branco*/
    fun nextUnlockTime(date: LocalDateTime = LocalDateTime.now(), rule: Rule): Long {

        baseDateTime = date.withSecondsAndMillisSetToZero()

        return when (rule.ruleType) {
            RuleType.RESTRICTIVE -> nextUnlockTimeRestrictive(rule)
            RuleType.PERMISSIVE -> nextUnlockTimePermissive(rule)
        }
            ?.withSecondsAndMillisSetToZero() /*Zero os segundos e milissegundos porque além de irrelevantes eles atrapalham os testes*/
            ?.toDate()?.time ?: INFINITE

    }

    //Executado quando o primeiro dia de bloqueio da regra é >= o dia de hoje
    private fun nextUnlockTimeRestrictive(rule: Rule): LocalDateTime? {

        var today = LocalDateTime(baseDateTime)
        val blockDays = rule.days.map { it.dayNumber }
        var goneTroughABlockDay = false

        repeat(REPEAT_COUNT) {
            if (it > 0) today = today.plusDays(1).withMillisOfDay(0)

            val weekDayInt = today.weekDayNumber()

            if (weekDayInt in blockDays) goneTroughABlockDay = true
            else {
                if (goneTroughABlockDay) return today
                return@repeat
            }

            val nextUnlockTime = getUnlockPeriodForRestrictiveDay(today, rule)
            if (nextUnlockTime != null) return nextUnlockTime

        }
        return null
    }

    private fun getUnlockPeriodForRestrictiveDay(
        day: LocalDateTime,
        rule: Rule,
    ): LocalDateTime? {

        val sortedTimeRanges = rule.timeRanges.sortedBy { it.startInMinutes() }

        val chainedRanges = { timeRange: TimeRange, index: Int ->
            timeRange.endInMinutes() + 1 == sortedTimeRanges.getOrNull(index + 1)?.startInMinutes()
        }

        sortedTimeRanges.forEachIndexed { index, timeRange ->

            if (rule.timeRanges.first().allDay) return null
            if (chainedRanges(timeRange, index)) return@forEachIndexed

            val timeRangeRelative = LocalDateTime(day)
                .withHourOfDay(timeRange.endHour)
                .withMinuteOfHour(timeRange.endMinute)

            if (timeRangeRelative.isAfter(day)) return timeRangeRelative.plusMinutes(1)

        }

        return null
    }


    private fun nextUnlockTimePermissive(rule: Rule): LocalDateTime? {

        var today = LocalDateTime(baseDateTime)
        val allowedDays = rule.days.map { it.dayNumber }
        var goneTroughABlockDay = false

        repeat(REPEAT_COUNT) {
            if (it > 0) today = today.plusDays(1).withMillisOfDay(0)

            val weekDayInt = today.weekDayNumber()

            if (weekDayInt in allowedDays) {
                if (!goneTroughABlockDay) return@repeat
            } else {
                goneTroughABlockDay = true
                return@repeat
            }


            val nextUnlockTime = getUnlockPeriodForPermissiveDay(today, rule)
            if (nextUnlockTime != null) return nextUnlockTime

        }
        return null
    }

    private fun getUnlockPeriodForPermissiveDay(
        day: LocalDateTime,
        rule: Rule,
    ): LocalDateTime? {

        val sortedTimeRanges = rule.timeRanges.sortedBy { it.startInMinutes() }

        val chainedRanges = { timeRange: TimeRange, index: Int ->
            timeRange.endInMinutes() + 1 == sortedTimeRanges.getOrNull(index + 1)?.startInMinutes()
        }

        sortedTimeRanges.forEachIndexed { index, timeRange ->

            if (rule.timeRanges.first().allDay) return day.withMillisOfDay(0)
            if (chainedRanges(timeRange, index)) return@forEachIndexed

            val timeRangeRelative = LocalDateTime(day)
                .withHourOfDay(timeRange.startHour)
                .withMinuteOfHour(timeRange.startMinute)

            if (timeRangeRelative.isAfter(day)) return timeRangeRelative
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
