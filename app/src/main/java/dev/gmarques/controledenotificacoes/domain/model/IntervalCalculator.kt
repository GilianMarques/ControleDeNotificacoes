package dev.gmarques.controledenotificacoes.domain.model

import android.icu.util.Calendar
import android.util.Log
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startIntervalFormatted
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

    /**
     * Objeto criado com único propósito de facilitar os testes. Quando esse objeto é inicializado, ele é usado nos cálculos ao invés
     * da data/hora atuais, isso permite escrever testes previsíveis.
     */
    private var baseDate: LocalDateTime? = null

    fun nextUnlockTime(baseDate: LocalDateTime? = null, rule: Rule): Long {

        this.baseDate = baseDate

        return when (rule.ruleType) {
            RuleType.RESTRICTIVE -> nextUnlockTimeFromNowRestrictive(rule)
            RuleType.PERMISSIVE -> nextUnlockTimeFromNowPermissive(rule)
        }?.withSecondsAndMillisSetToZero() /*Zero os segundos e milissegundos porque além de irrelevantes eles atrapalham os testes*/
            ?.toDate()?.time
            ?: INFINITE

    }

    private fun nextUnlockTimeFromNowRestrictive(rule: Rule): LocalDateTime? {

        val originalDateTime = (baseDate ?: LocalDateTime.now()).withSecondsAndMillisSetToZero()

        var todayLocalDateTime = LocalDateTime(originalDateTime)
        val blockDays = rule.days.map { it.dayNumber }

        val incrementDay = {
            todayLocalDateTime = todayLocalDateTime.plusDays(1).withMillisOfDay(0)// retorna o proximo dia às 00:00
        }

        repeat(WeekDay.entries.size) {

            val weekDayInt = Calendar.getInstance()
                .apply { timeInMillis = todayLocalDateTime.toDate().time }
                .get(Calendar.DAY_OF_WEEK)

            /* Se o dia não está na lista de bloqueio da regra significa que o app está desbloqueado, porém, essa função busca
             o próximo período de desbloqueio sem considerar o atual por esse motivo, ao invés de retornar aqui, busco o fim
             do próximo período de bloqueio para retornar.
             */
            if (it == 0 && weekDayInt !in blockDays) {
                incrementDay()
                return@repeat
            }

            val nextUnlockTime = iterateOverTimeRanges(todayLocalDateTime, rule)
            if (nextUnlockTime != null) return nextUnlockTime

            incrementDay()
            Log.d("USUK", "IntervalCalculator.nextUnlockTimeFromNowRestricted: checking tomorrow: $todayLocalDateTime")
        }
        Log.w(
            "USUK",
            "IntervalCalculator.nextUnlockTimeFromNowRestrictive: Wasn't possible to find the next unlock period for rule: ${rule}.\nIf this wasn't caused by a blocking rule of 7 days + 24 hours, it may indicate a bug."
        )
        return null
    }

    private fun nextUnlockTimeFromNowPermissive(rule: Rule): LocalDateTime? {
        return LocalDateTime.now()
    }

    private fun iterateOverTimeRanges(
        currentTime: LocalDateTime,
        rule: Rule,
    ): LocalDateTime? {

        val sortedTimeRanges = rule.timeRanges.sortedBy { it.startInMinutes() }

        sortedTimeRanges.forEach { timeRange ->

            Log.d(
                "USUK",
                "IntervalCalculator.iterateOverTimeRanges: checking ${timeRange.startIntervalFormatted()}:${timeRange.endIntervalFormatted()}"
            )

            if (rule.ruleType == RuleType.RESTRICTIVE) {

                val timeRangeRelative = LocalDateTime(currentTime)
                    .withHourOfDay(timeRange.endHour)
                    .withMinuteOfHour(timeRange.endMinute)

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

        Log.d("USUK", "IntervalCalculator.iterateOverTimeRanges: no timerange found for next unlock: currentTime: $currentTime")
        return null
    }

    private fun LocalDateTime.withSecondsAndMillisSetToZero(): LocalDateTime {
        return this.withSecondOfMinute(0).withMillisOfSecond(0)
    }

}