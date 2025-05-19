package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 19 de maio de 2025 as 15:13.
 */
class RescheduleAlarmOnAppsRuleChangeUseCase @Inject constructor(
    private val scheduleManager: ScheduleManager,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val scheduleAlarmForAppUseCase: ScheduleAlarmForAppUseCase,
) {

    /**
     * Executa o caso de uso para reagendar o alarme de um aplicativo.
     *
     * @param app O aplicativo gerenciado cuja regra foi alterada.
     */
    suspend operator fun invoke(app: ManagedApp) = withContext(IO) {

        if (!scheduleManager.isThereAnyAlarmSetForPackage(app.packageId)) return@withContext

        val rule = getRuleByIdUseCase(app.ruleId) ?: error("A regra ${app.ruleId} não foi encontrada. Isso é um Bug.")
        scheduleAlarmForAppUseCase(app, rule)

    }


}