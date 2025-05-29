package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import dev.gmarques.controledenotificacoes.domain.framework.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        removeNotificationCallback: (AppNotification, Rule) -> Any,
    ) = withContext(IO) {


    val managedApp = getManagedAppByPackageIdUseCase(notification.packageId)
        if (managedApp == null) {
            return@withContext
        }
        val rule = getRuleByIdUseCase(managedApp.ruleId)
            ?: error("Um app gerenciado deve ter uma regra. Isso é um Bug $managedApp")


        if (rule.isAppInBlockPeriod()) {
            removeNotificationCallback(notification, rule)
            saveNotificationOnHistory(notification)
        }


    }

    private fun saveNotificationOnHistory(notification: AppNotification) {

        if (notification.title.isEmpty() && notification.content.isEmpty()) return

        launch {
            insertAppNotificationUseCase(
                AppNotification(
                    notification.packageId, notification.title, notification.content, System.currentTimeMillis()
                )
            )
        }
    }
}
