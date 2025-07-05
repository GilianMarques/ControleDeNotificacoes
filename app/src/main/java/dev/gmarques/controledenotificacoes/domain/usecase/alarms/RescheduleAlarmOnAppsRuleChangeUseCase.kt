package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 19 de maio de 2025 as 15:13.
 */
class RescheduleAlarmOnAppsRuleChangeUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val scheduleAlarmForAppUseCase: ScheduleAlarmForAppUseCase,
) {

    /**
     * Executa o caso de uso para reagendar o alarme de um aplicativo.
     * Verifica se existe algum alarme ativo para o aplicativo em questão se houver, solicita o
     * reagendamento desse alarme com base na nova regra.
     *
     * @param app O aplicativo gerenciado cuja regra foi alterada.
     */
    suspend operator fun invoke(app: ManagedApp, rule: Rule) = withContext(IO) {

        if (alarmScheduler.isThereAnyAlarmSetForPackage(app.packageId)) scheduleAlarmForAppUseCase(app, rule)

    }


}