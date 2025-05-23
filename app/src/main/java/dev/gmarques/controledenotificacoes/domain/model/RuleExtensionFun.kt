package dev.gmarques.controledenotificacoes.domain.model

import dagger.hilt.android.EntryPointAccessors
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.di.entry_points.UseCasesEntryPoint
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
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
        val nextAppUnlockTimeUseCase = EntryPointAccessors.fromApplication(
            App.context,
            UseCasesEntryPoint::class.java
        ).nextAppUnlockUseCase()
        return nextAppUnlockTimeUseCase(rule = this)
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

        val checkAppInBlockPeriodUseCase = EntryPointAccessors.fromApplication(
            App.context,
            UseCasesEntryPoint::class.java
        ).checkAppInBlockPeriodUseCase()

        return checkAppInBlockPeriodUseCase(rule = this)
    }
}