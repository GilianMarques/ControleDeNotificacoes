package dev.gmarques.controledenotificacoes.domain.usecase.rules

import dev.gmarques.controledenotificacoes.domain.framework.RuleStringsProvider
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 04 de abril de 2025 as 17:43.
 */
class GenerateRuleDescriptionUseCase @Inject constructor(
    private val ruleStringsProvider: RuleStringsProvider,
) {

    operator fun invoke(rule: Rule): String {
        val formattedDays = formatCondensedDays(rule.days)
        val range = formatTimeRanges(rule)
        val ruleType = formatRuleType(rule.ruleType)

        return "$ruleType $formattedDays $range"
    }

    private fun formatCondensedDays(days: List<WeekDay>): String {
        if (days.isEmpty()) return ""
        if (days.size == 7) return ruleStringsProvider.everyDay()

        val sortedDays = days.sortedBy { it.dayNumber }

        val sequences = mutableListOf<List<WeekDay>>()
        var currentSequence = mutableListOf<WeekDay>()

        for ((index, day) in sortedDays.withIndex()) {
            if (currentSequence.isEmpty()) {
                currentSequence.add(day)
            } else {
                val lastDay = currentSequence.last()
                if (day.dayNumber == lastDay.dayNumber + 1) {
                    currentSequence.add(day)
                } else {
                    sequences.add(currentSequence)
                    currentSequence = mutableListOf(day)
                }
            }

            if (index == sortedDays.lastIndex) {
                sequences.add(currentSequence)
            }
        }

        return sequences.joinToString("/") { sequence ->
            when {
                sequence.size == 1 -> abbreviatedDay(sequence.first())
                else -> "${abbreviatedDay(sequence.first())}-${abbreviatedDay(sequence.last())}"
            }
        }
    }

    private fun abbreviatedDay(day: WeekDay): String {
        return when (day) {
            WeekDay.MONDAY -> ruleStringsProvider.monday()
            WeekDay.TUESDAY -> ruleStringsProvider.tuesday()
            WeekDay.WEDNESDAY -> ruleStringsProvider.wednesday()
            WeekDay.THURSDAY -> ruleStringsProvider.thursday()
            WeekDay.FRIDAY -> ruleStringsProvider.friday()
            WeekDay.SATURDAY -> ruleStringsProvider.saturday()
            WeekDay.SUNDAY -> ruleStringsProvider.sunday()
        }
    }

    private fun formatTimeRanges(rule: Rule): String {
        if (rule.timeRanges.isEmpty()) return ""

        if (rule.timeRanges.any { it.allDay }) return ruleStringsProvider.wholeDay()


        val start = rule.timeRanges.minByOrNull { it.startHour * 60 + it.startMinute }!!
        val end = rule.timeRanges.maxByOrNull { it.endHour * 60 + it.endMinute }!!

        val startTime = formatTime(start.startHour, start.startMinute)
        val endTime = formatTime(end.endHour, end.endMinute)

        return "%s-%s".format(startTime, endTime)

    }

    private fun formatTime(hour: Int, minute: Int): String {
        return "%02d:%02d".format(hour, minute)
    }

    private fun formatRuleType(type: RuleType): String {
        return when (type) {
            RuleType.PERMISSIVE -> ruleStringsProvider.permissive()
            RuleType.RESTRICTIVE -> ruleStringsProvider.restrictive()
        }
    }
}