package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.model.ConditionExtensionFun.description
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType

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
    fun Rule.nextAppUnlockPeriodFromNow(): Long {
        val nextAppUnlockTimeUseCase = HiltEntryPoints.nextAppUnlockUseCase()
        return nextAppUnlockTimeUseCase(rule = this)
    }

    /**
     * Verifica se o aplicativo está dentro de um período de bloqueio com base na regra e horario (momento) da chamada.
     *
     * Um aplicativo é considerado "em bloqueio" se:
     * - A regra for [RuleType.RESTRICTIVE] e o horário atual estiver dentro de um dos [TimeRange]s especificados e o dia da semana atual estiver incluído na lista [dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay].
     * - A regra for [RuleType.PERMISSIVE] e o horário atual NÃO estiver dentro de NENHUM dos [TimeRange]s especificados e o dia da semana atual estiver incluído na lista [dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay].
     * - A regra for [RuleType.PERMISSIVE] e o dia da semana atual NÃO estiver incluído na lista [dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay]s.
     *
     * @return `true` se o aplicativo estiver em um período de bloqueio de acordo com esta regra, `false` caso contrário.
     *
     * Exemplo:
     * Seja uma regra para o app "ExemploApp" com as seguintes configurações:
     * - [RuleType.RESTRICTIVE]
     * - [dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay]: Segunda (2), Terça (3)
     * - [TimeRange]s: [10:00 - 12:00]
     *
     * Se for Segunda-feira às 11:00, `isAppInBlockPeriod()` retornará `true`.
     * Se for Quarta-feira às 11:00, `isAppInBlockPeriod()` retornará `false`.
     */
    fun Rule.isAppInBlockPeriod(): Boolean {

        val isAppInBlockPeriodUseCase = HiltEntryPoints.isAppInBlockPeriodUseCase()

        return isAppInBlockPeriodUseCase(rule = this)
    }

    /**
     * Retorna o nome da regra, se definido. Caso contrário, gera uma descrição  da regra.
     *
     * Esta função é útil para exibir informações sobre a regra ao usuário,
     * mesmo que um nome explícito não tenha sido fornecido.
     *
     * A geração do nome descritivo é delegada a um `UseCase` (`generateRuleNameUseCase`)
     * obtido através do `HiltEntryPoints`. Isso garante que a lógica de formatação
     * do nome seja centralizada e testável.
     *
     * @return O nome da regra se `name` não estiver em branco, caso contrário, uma descrição gerada da regra.
     *
     * @see HiltEntryPoints.generateRuleNameUseCase
     */
    fun Rule.nameOrDescription(): String {

        if (name.isNotBlank()) return name

        return HiltEntryPoints.generateRuleNameUseCase()(this) + " " +
                condition?.description(App.instance).orEmpty().trim()
    }

    /**
     * Verifica se a regra representa um dia de permissão/bloqueio integral, ou seja, 24 horas.
     *
     * @return `true` se a regra contiver ao menos um intervalo com flag `allDay = true`
     */
    fun Rule.isAllDayRule(): Boolean = timeRanges.any { it.allDay }

    /**
     * Retorna a lista de [TimeRange]s ordenada pelo horário de início.
     */
    fun Rule.sortedRanges() = timeRanges.sortedBy { it.startInMinutes() }
}