package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppsByRuleIdUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 19 de maio de 2025 as 13:14.
 */
class RescheduleAlarmsOnRuleEditUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val getManagedAppsByRuleIdUseCase: GetManagedAppsByRuleIdUseCase,
    private val scheduleAlarmForAppUseCase: ScheduleAlarmForAppUseCase,
) {

    /**
     * Reagenda os alarmes para todos os aplicativos gerenciados pela regra editada.
     */
    suspend operator fun invoke(rule: Rule) = withContext(IO) {

        getManagedAppsByRuleIdUseCase(rule.id)
            .map { app ->
                async {
                    app?.let { reschedule(app, rule) }
                }
            }.awaitAll()
    }

    /**
     * Reagenda um alarme para um aplicativo específico com base na regra fornecida.
     * Se já existir um alarme ativo para o aplicativo, ele será cancelado e um novo será agendado.
     */
    private fun reschedule(
        app: ManagedApp,
        rule: Rule,
    ) {
        val isThereAnyActiveAlarm = alarmScheduler.isThereAnyAlarmSetForPackage(app.packageId)

        if (isThereAnyActiveAlarm) {
            alarmScheduler.cancelAlarm(app.packageId)
            scheduleAlarmForAppUseCase(app, rule)
        }
    }


}