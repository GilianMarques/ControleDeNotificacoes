package dev.gmarques.controledenotificacoes.domain.model

import android.util.Log
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import org.jetbrains.annotations.TestOnly
import org.joda.time.DateTimeFieldType
import org.joda.time.LocalDateTime
import java.util.Calendar

/**
 * Criado por Gilian Marques
 * Em terça-feira, 20 de maio de 2025 as 13:21.
 */
// TODO: arrumar um nome melhor
// TODO: colocar no pacote adequado
class IntervalCalculator {

    private val now = LocalDateTime.now()
    val actualDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    fun nextUnlockTime(rule: Rule) {
        if (rule.ruleType == RuleType.RESTRICTIVE) nextUnlockTimeFromNowRestricted(rule)
    }

    private fun nextUnlockTimeFromNowRestricted(rule: Rule): Long {

        val days = getDaysRelativelySorted(rule.days, actualDayOfWeek)
        return iterateOverDays(days, rule)

    }

    private fun iterateOverDays(blockDays: List<Int>, rule: Rule): Long {

        var todayLocalDateTime = LocalDateTime.now()

        repeat(7) {

            val weekDayInt = todayLocalDateTime.get(DateTimeFieldType.dayOfWeek())
            // TODO: deve retornar o dia em questao as 00:00
            if (weekDayInt !in blockDays) return todayLocalDateTime.toDate().time.also {
                Log.d(
                    "USUK",
                    "IntervalCalculator.nextUnlockTimeFromNowRestricted:" + "data: ${todayLocalDateTime} de numero dia: $weekDayInt nao esta presente em ${blockDays}"
                )
            }

            val nextUnlockTime = iterateOverTimeRanges(todayLocalDateTime, rule)
            if (nextUnlockTime != null) return nextUnlockTime
            // TODO: continuar aqui

            /* dia de hoje consta na lista de dias da regra:
               avalio os horarios e vejo se de agora até as 00:00 tem algum fim de intervalo de bloqueio
                retorno hoje depois do fim do intervalo, se houver. Se nao o loop segue pelos proximos dias
                 dia de hoje nao consta na lista de dias bloqueados  retorno o dia de hoje as 00:00 */

            todayLocalDateTime = todayLocalDateTime.plusDays(1)

        }
    }

    private fun iterateOverTimeRanges(
        currentTime: LocalDateTime,
        rule: Rule,
    ): Long? {

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

                if (timeRangeRelative.isAfter(currentTime)) return timeRangeRelative.plusMinutes(1).toDate().time.also {
                    Log.d(
                        "USUK",
                        "IntervalCalculator.iterateOverTimeRanges: RESTRICTIVE next unlock period found: timeRangeRelative $timeRangeRelative currentTime $currentTime"
                    )
                }
            }

            if (rule.ruleType == RuleType.PERMISSIVE) {

                val timeRangeRelative = LocalDateTime(currentTime)
                    .withHourOfDay(timeRange.startHour)
                    .withMinuteOfHour(timeRange.startMinute)

                if (timeRangeRelative.isAfter(currentTime)) return timeRangeRelative.toDate().time.also {
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


    /**
     * Essa função reordena os dias selecionados de uma regra de acordo com o dia da semana atual,
     * garantindo que o primeiro dia da lista retornada seja o dia da semana atual.
     * Exemplo: Se [actualDayOfWeek] = TERÇA e [ruleDays] = [ SEGUNDA, TERÇA, QUARTA, SEXTA],
     * a função deve retornar [TERÇA, QUARTA, SEXTA, SEGUNDA].
     * A função parte a lista no meio, movendo os dias atrás do dia atual para o fim da lista sem altrar sua ordem.
     */
    @TestOnly
    fun getDaysRelativelySorted(ruleDays: List<WeekDay>, actualDayOfWeek: Int): List<Int> {

        if (ruleDays.isEmpty()) error("Uma regra deve ter pelo menos um dia selecionado")
        if (ruleDays.size == 1) return ruleDays.map { it.dayNumber }

        val sortedRuleDaysInt = ruleDays.map { it.dayNumber }.sortedBy { it }
        if (actualDayOfWeek <= sortedRuleDaysInt.first()) return sortedRuleDaysInt

        val splitIndex = sortedRuleDaysInt.indexOfFirst { it >= actualDayOfWeek }

        return sortedRuleDaysInt.drop(splitIndex) + sortedRuleDaysInt.take(splitIndex)
    }


}