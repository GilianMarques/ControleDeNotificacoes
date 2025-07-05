package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import android.util.Log
import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextAppUnlockPeriodFromNow
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.NextAppUnlockTimeUseCase
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 19 de maio de 2025 as 15:35.
 * Criada pra fazer valer o DRY na hora de reagendar os alarmes
 */
class ScheduleAlarmForAppUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
) {

    /**
     * Agenda um alarme para um aplicativo específico com base em sua regra.
     * Se o aplicativo estiver em período de bloqueio, o alarme é agendado para o próximo período de desbloqueio.
     * Caso contrário, o alarme é agendado para 2 segundos a partir do momento atual.
     * @param app O aplicativo para o qual o alarme será agendado.
     * @param rule A regra associada ao aplicativo.
     */
    operator fun invoke(app: ManagedApp, rule: Rule) {
        Log.d("USUK", "ScheduleAlarmForAppUseCase.invoke: rescheduled ${app.packageId}")

        val scheduleTimeMillis =
            if (rule.isAppInBlockPeriod()) rule.nextAppUnlockPeriodFromNow()
            else System.currentTimeMillis() + 2_000L

        if (scheduleTimeMillis == NextAppUnlockTimeUseCase.INFINITE) return.also {
            Log.d(
                "USUK",
                "ScheduleAlarmForAppUseCase.invoke: wont schedule notification for package ${app.packageId} 'cause the app is always block"
            )
        }

        alarmScheduler.scheduleAlarm(app.packageId, scheduleTimeMillis)


    }
}