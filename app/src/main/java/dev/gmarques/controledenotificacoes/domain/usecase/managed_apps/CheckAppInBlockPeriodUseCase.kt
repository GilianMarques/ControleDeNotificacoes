package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType.PERMISSIVE
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType.RESTRICTIVE
import java.util.Calendar
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 23 de maio de 2025 as 17:46.
 */
class CheckAppInBlockPeriodUseCase @Inject constructor() {
    /**
     * Verifica se o aplicativo está dentro de um período de bloqueio com base na regra e horario (momento) da chamada.
     *
     * Um aplicativo é considerado "em bloqueio" se:
     * - A regra for [RuleType.RESTRICTIVE] e o horário atual estiver dentro de um dos [timeRanges] especificados e o dia da semana atual estiver incluído na lista [days].
     * - A regra for [RuleType.PERMISSIVE] e o horário atual NÃO estiver dentro de NENHUM dos [timeRanges] especificados e o dia da semana atual estiver incluído na lista [days].
     * - A regra for [RuleType.PERMISSIVE] e o dia da semana atual NÃO estiver incluído na lista [days].
     *
     * @return `true` se o aplicativo estiver em um período de bloqueio de acordo com esta regra, `false` caso contrário.
     *
     * Exemplo:
     * Seja uma regra para o app "ExemploApp" com as seguintes configurações:
     * - [RuleType.RESTRICTIVE]
     * - [days]: Segunda (2), Terça (3)
     * - [timeRanges]: [10:00 - 12:00]
     *
     * Se for Segunda-feira às 11:00, `isAppInBlockPeriod()` retornará `true`.
     * Se for Quarta-feira às 11:00, `isAppInBlockPeriod()` retornará `false`.
     */
    operator fun invoke(rule: Rule): Boolean {

        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val isDayMatched = rule.days.any { it.dayNumber == currentDay }

        if (!isDayMatched) {
            return rule.ruleType == PERMISSIVE
        }

        val isTimeMatched = rule.timeRanges.any { range ->
            currentMinutes in range.startInMinutes()..range.endInMinutes()
        }

        return when (rule.ruleType) {
            RESTRICTIVE -> isTimeMatched
            PERMISSIVE -> !isTimeMatched
        }
    }
}