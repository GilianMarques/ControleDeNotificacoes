package dev.gmarques.controledenotificacoes.plataform.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 04 de abril de 2025 as 17:43.
 */
class GenerateRuleNameUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun execute(rule: Rule): String {
        val formattedDays = formatDays(rule.days)
        val interval = formatTimeIntervals(rule)
        val ruleType = formatRuleType(rule.ruleType)

        return context.getString(R.string.Nome_padrao_de_regra, ruleType,formattedDays, interval).trim()
    }

    private fun formatDays(days: List<WeekDay>): String {
        return days.sortedBy { it.dayNumber }
            .joinToString("/") { abbreviatedDay(it) }
    }

    private fun abbreviatedDay(day: WeekDay): String {
        return when (day) {
            WeekDay.MONDAY -> context.getString(R.string.segunda_abrev)
            WeekDay.TUESDAY -> context.getString(R.string.terca_abrev)
            WeekDay.WEDNESDAY -> context.getString(R.string.quarta_abrev)
            WeekDay.THURSDAY -> context.getString(R.string.quinta_abrev)
            WeekDay.FRIDAY -> context.getString(R.string.sexta_abrev)
            WeekDay.SATURDAY -> context.getString(R.string.sabado_abrev)
            WeekDay.SUNDAY -> context.getString(R.string.domingo_abrev)
        }
    }

    private fun formatTimeIntervals(rule: Rule): String {
        if (rule.timeIntervals.isEmpty()) return ""

        val start = rule.timeIntervals.minByOrNull { it.startHour * 60 + it.startMinute }!!
        val end = rule.timeIntervals.maxByOrNull { it.endHour * 60 + it.endMinute }!!

        val startTime = formatTime(start.startHour, start.startMinute)
        val endTime = formatTime(end.endHour, end.endMinute)

        return context.getString(R.string.Intervalo_inicio_fim, startTime, endTime)
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return "%02d:%02d".format(hour, minute)
    }

    private fun formatRuleType(type: RuleType): String {
        return when (type) {
            RuleType.PERMISSIVE -> context.getString(R.string.regra_tipo_permissiva)
            RuleType.RESTRICTIVE -> context.getString(R.string.regra_tipo_restritiva)
        }
    }
}
