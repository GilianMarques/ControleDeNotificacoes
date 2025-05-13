package dev.gmarques.controledenotificacoes.framework.notification_service

import dev.gmarques.controledenotificacoes.domain.framework.notification_service.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

/*
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 14:16.
 */
class RuleEnforcerImpl @Inject constructor(
    private val getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val insertAppNotificationUseCase: InsertAppNotificationUseCase,
) : RuleEnforcer, CoroutineScope by CoroutineScope(IO) {

    override suspend fun enforceOnNotification(
        notification: AppNotification,
        removeNotificationCallback: (AppNotification) -> Any,
    ) = withContext(IO) {

        val managedApp = getManagedAppByPackageIdUseCase(notification.packageId)
        if (managedApp == null) {
            return@withContext
        }

        val rule = getRuleByIdUseCase(managedApp.ruleId)
            ?: error("Um app gerenciado deve ter uma regra. Isso é um Bug $managedApp")

        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
            .let { if (it == Calendar.SUNDAY) 7 else it - 1 } // TODO: essa linha deve ser testada
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        if (shouldBlockNotification(rule, currentDay, currentMinutes)) {
            removeNotificationCallback(notification)
        }

        saveNotificationOnHistory(notification)

    }

    private fun shouldBlockNotification(rule: Rule, currentDay: Int, currentMinutes: Int): Boolean {
        val isDayMatched = rule.days.any { it.dayNumber == currentDay }

        if (!isDayMatched) {
            return rule.ruleType == RuleType.PERMISSIVE
        }

        val isTimeMatched = rule.timeRanges.any { range ->
            currentMinutes in range.startInMinutes() until range.endInMinutes()
        }

        return when (rule.ruleType) {
            RuleType.RESTRICTIVE -> isTimeMatched
            RuleType.PERMISSIVE -> !isTimeMatched
        }
    }

    private fun saveNotificationOnHistory(notification: AppNotification) {

        launch {
            insertAppNotificationUseCase(
                AppNotification(
                    notification.packageId,
                    notification.title,
                    notification.content,
                    System.currentTimeMillis()
                )
            )
        }
    }
}
