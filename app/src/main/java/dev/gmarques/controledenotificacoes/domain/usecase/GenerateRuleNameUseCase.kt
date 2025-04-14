package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.usecase.contracts.RuleStringsProvider
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 04 de abril de 2025 as 17:43.
 */
class GenerateRuleNameUseCase @Inject constructor(
    private val ruleStringsProvider: RuleStringsProvider,
) {
    operator fun invoke(rule: Rule): String {
        val formattedDays = formatDays(rule.days)
        val range = formatTimeRanges(rule)
        val ruleType = formatRuleType(rule.ruleType)

        return "$ruleType $formattedDays $range"
    }

    private fun formatDays(days: List<WeekDay>): String {
        return days.sortedBy { it.dayNumber }
            .joinToString("/") { abbreviatedDay(it) }
    }

    private fun abbreviatedDay(day: WeekDay): String {
        return when (day) {
            WeekDay.MONDAY -> ruleStringsProvider.monday()
            WeekDay.TUESDAY -> ruleStringsProvider.tuesday()
            WeekDay.WEDNESDAY -> ruleStringsProvider.wednesday()
            WeekDay.THURSDAY -> ruleStringsProvider.thursday()
            WeekDay.FRIDAY -> ruleStringsProvider.friday()
            WeekDay.SATURDAY -> ruleStringsProvider.saturday()
            WeekDay.SUNDAY -> ruleStringsProvider.saturday()
        }
    }

    private fun formatTimeRanges(rule: Rule): String {
        if (rule.timeRanges.isEmpty()) return ""

        val start = rule.timeRanges.minByOrNull { it.startHour * 60 + it.startMinute }!!
        val end = rule.timeRanges.maxByOrNull { it.endHour * 60 + it.endMinute }!!

        val startTime = formatTime(start.startHour, start.startMinute)
        val endTime = formatTime(end.endHour, end.endMinute)

        return "%02d-%02d".format(startTime, endTime)

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