package dev.gmarques.controledenotificacoes.domain.model

import android.util.Log
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startIntervalFormatted
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
     * Não considera caso o momento atual seja de desbloqueio (para saber se o app esta desbloqueado nesse momento, use [isAppInBlockPeriod]). Essa função sempre buscará o proximo periodo.
     *
     * Usa a lib JodaTime pra garantir cmpatibilidade com apis <26
     *
     * Esta função itera sobre os [TimeRange]s associados à regra.
     * - Para regras [RuleType.PERMISSIVE], ela busca o próximo início de período de permissão.
     * - Para regras [RuleType.RESTRICTIVE], ela busca o próximo fim de período de restrição.
     *
     */
    // TODO: função esta errada
    fun Rule.nextUnlockPeriodFromNow(): DateTime {
        Log.d(
            "USUK", "RuleExtensionFun.nextUnlockPeriodFromNow:$id\n ${
                this.timeRanges.joinToString("\n") {
                    "${it.startIntervalFormatted()}:${it.endIntervalFormatted()}"
                }
            }")

        val now = DateTime.now()

        for (range in this.timeRanges) {

            if (this.ruleType == RuleType.PERMISSIVE) {
                val rangeStartPeriod = now.withTime(range.startHour, range.startMinute, 0, 0)
                Log.d("USUK", "RuleExtensionFun.nextUnlockPeriodFromNow: permissive: $rangeStartPeriod : $now")
                if (rangeStartPeriod.isAfter(now)) return rangeStartPeriod
            }

            if (this.ruleType == RuleType.RESTRICTIVE) {
                val rangeEndPeriod = now.withTime(range.endHour, range.endMinute, 0, 0)
                Log.d("USUK", "RuleExtensionFun.nextUnlockPeriodFromNow: restrictive: $rangeEndPeriod : $now")
                if (rangeEndPeriod.isAfter(now)) return rangeEndPeriod
            }
        }
        Log.d("USUK", "RuleExtensionFun.".plus("nextUnlockPeriodFromNow() nao foi encontado prox range"))
        return now + 1_000L
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
    // TODO: talvez esteja errada tambem
    fun Rule.isAppInBlockPeriod(): Boolean {

        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
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