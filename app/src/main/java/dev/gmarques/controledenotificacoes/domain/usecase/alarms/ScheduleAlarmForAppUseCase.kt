package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextUnlockPeriodFromNow
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 19 de maio de 2025 as 15:35.
 * Criada pra fazer valer o DRY na hora de reagendar os alarmes
 */
class ScheduleAlarmForAppUseCase @Inject constructor(
    private val scheduleManager: ScheduleManager,
) {

    /**
     * Agenda um alarme para um aplicativo específico com base em sua regra.
     * Se o aplicativo estiver em período de bloqueio, o alarme é agendado para o próximo período de desbloqueio.
     * Caso contrário, o alarme é agendado para 5 segundos a partir do momento atual.
     * @param app O aplicativo para o qual o alarme será agendado.
     * @param rule A regra associada ao aplicativo.
     */
    operator fun invoke(app: ManagedApp, rule: Rule) {

        val scheduleTimeMillis =
            if (rule.isAppInBlockPeriod()) rule.nextUnlockPeriodFromNow().millis
            else System.currentTimeMillis() + 5_000L

        scheduleManager.scheduleAlarm(app.packageId, scheduleTimeMillis)

    }
}