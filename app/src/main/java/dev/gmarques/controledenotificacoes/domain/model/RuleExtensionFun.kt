package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import org.joda.time.DateTime
import java.util.Calendar

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 31 de março de 2025 as 23:20.
 *
 * Classe utilitária para adicionar funcionalidades ao [TimeRange]
 *
 */
object RuleExtensionFun {


    /**
     * Calcula o próximo período de desbloqueio para um app gerenciado com base nesta regra e a partir do momento atual.
     *
     * Não considera caso o momento atual seja de desbloqueio. Essa função sempre buscará o proximo periodo.
     *
     * Usa a lib JodaTime pra garantir cmpatibilidade com apis <26
     *
     * Esta função itera sobre os [TimeRange]s associados à regra.
     * - Para regras [RuleType.PERMISSIVE], ela busca o próximo início de período de permissão.
     * - Para regras [RuleType.RESTRICTIVE], ela busca o próximo fim de período de restrição.
     *
     */
    fun Rule.nextUnlockPeriodFromNow(): DateTime? {
        val now = DateTime.now()

        for (range in this.timeRanges) {

            if (this.ruleType == RuleType.PERMISSIVE) {
                val rangeStartPeriod = now.withTime(range.startHour, range.startMinute, 0, 0)
                if (rangeStartPeriod.isAfter(now)) return rangeStartPeriod
            }

            if (this.ruleType == RuleType.RESTRICTIVE) {
                val rangeEndPeriod = now.withTime(range.endHour, range.endMinute, 0, 0)
                if (rangeEndPeriod.isAfter(now)) return rangeEndPeriod
            }
        }

        return null
    }

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
    fun Rule.isAppInBlockPeriod(): Boolean {

        val now = Calendar.getInstance()
        val currentDay =
            now.get(Calendar.DAY_OF_WEEK).let { if (it == Calendar.SUNDAY) 7 else it - 1 } // TODO: essa linha deve ser testada
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val isDayMatched = days.any { it.dayNumber == currentDay }

        if (!isDayMatched) {
            return ruleType == RuleType.PERMISSIVE
        }

        val isTimeMatched = timeRanges.any { range ->
            currentMinutes in range.startInMinutes()..range.endInMinutes()
        }

        return when (ruleType) {
            RuleType.RESTRICTIVE -> isTimeMatched
            RuleType.PERMISSIVE -> !isTimeMatched
        }
    }
}