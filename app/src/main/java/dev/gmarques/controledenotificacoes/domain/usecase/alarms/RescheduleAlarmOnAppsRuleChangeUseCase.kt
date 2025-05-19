package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextUnlockPeriodFromNow
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
) {

    suspend operator fun invoke(app: ManagedApp) = withContext(IO) {

        if (!scheduleManager.isThereAnyAlarmSetForPackage(app.packageId)) return@withContext

        val rule = getRuleByIdUseCase(app.ruleId) ?: error("A regra ${app.ruleId} não foi encontrada. Isso é um Bug.")
        scheduleAlarmForApp(app, rule)

    }

    private fun scheduleAlarmForApp(app: ManagedApp, rule: Rule) {

        val scheduleTimeMillis =
            if (rule.isAppInBlockPeriod()) rule.nextUnlockPeriodFromNow().millis
            else System.currentTimeMillis() + 5_000L

        scheduleManager.scheduleAlarm(app.packageId, scheduleTimeMillis)

    }
}