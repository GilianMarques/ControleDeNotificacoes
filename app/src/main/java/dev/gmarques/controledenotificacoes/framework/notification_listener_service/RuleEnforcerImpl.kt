package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.framework.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.bitmapId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.pendingIntentId
import dev.gmarques.controledenotificacoes.domain.model.ConditionExtensionFun.isSatisfiedBy
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextAppUnlockPeriodFromNow
import dev.gmarques.controledenotificacoes.domain.model.enums.ConditionType
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import dev.gmarques.controledenotificacoes.framework.PendingIntentCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/*
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 14:16.
 */
class RuleEnforcerImpl @Inject constructor(
    private val getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val alarmScheduler: AlarmScheduler,
    private val updateManagedAppUseCase: dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase,
    private val insertAppNotificationUseCase: InsertAppNotificationUseCase,
    @ApplicationContext private val context: Context,
) : RuleEnforcer, CoroutineScope by CoroutineScope(IO) {

    private lateinit var callback: RuleEnforcer.Callback

    override suspend fun enforceOnNotification(sbn: StatusBarNotification, callback: RuleEnforcer.Callback) = withContext(IO) {

        this@RuleEnforcerImpl.callback = callback

        val notification = AppNotificationExtensionFun.createFromStatusBarNotification(sbn)

        val managedApp = getManagedAppByPackageIdUseCase(notification.packageId)

        if (managedApp != null) {
            val rule = getRuleByIdUseCase(managedApp.ruleId)
                ?: error("Um app gerenciado deve ter uma regra. Isso é um Bug $managedApp")

            enforceRuleAndCondition(rule, managedApp, sbn, notification)

        } else callback.appNotManaged()

    }

    private fun enforceRuleAndCondition(
        rule: Rule,
        managedApp: ManagedApp,
        sbn: StatusBarNotification,
        notification: AppNotification,
    ) {

        val condition = rule.condition
        val appInBlockPeriod = rule.isAppInBlockPeriod()

        if (condition == null) {
            if (appInBlockPeriod) saveAndCancelNotification(rule, managedApp, sbn, notification)
            else callback.allowNotification()
            return
        }

        val shouldAllowNotification = shouldAllowNotification(
            ruleType = rule.ruleType,
            conditionType = condition.type,
            isConditionSatisfied = condition.isSatisfiedBy(notification),
            isAppInBlockPeriod = appInBlockPeriod
        )

        if (shouldAllowNotification) callback.allowNotification()
        else saveAndCancelNotification(rule, managedApp, sbn, notification)
    }

    @TestOnly
    fun shouldAllowNotification(
        ruleType: RuleType,
        conditionType: ConditionType,
        isConditionSatisfied: Boolean,
        isAppInBlockPeriod: Boolean,
    ): Boolean {

        if (ruleType == RuleType.RESTRICTIVE && isAppInBlockPeriod) {
            return when (conditionType) {
                ConditionType.ONLY_IF -> !isConditionSatisfied
                ConditionType.EXCEPT -> isConditionSatisfied
            }
        }

        if (ruleType == RuleType.PERMISSIVE && !isAppInBlockPeriod) {
            return when (conditionType) {
                ConditionType.ONLY_IF -> isConditionSatisfied
                ConditionType.EXCEPT -> !isConditionSatisfied
            }
        }

        Log.w(
            "USUK",
            "RuleEnforcerImpl.shouldAllowNotification: notificaçção permitida pq nao caiu em nenhuma pré-condição"
        )

        return true
    }

    private fun saveAndCancelNotification(
        rule: Rule,
        managedApp: ManagedApp,
        sbn: StatusBarNotification,
        notification: AppNotification,
    ) {
        callback.cancelNotification(notification, rule, managedApp)
        launch {
            alarmScheduler.scheduleAlarm(notification.packageId, rule.nextAppUnlockPeriodFromNow())
            updateManagedAppUseCase(managedApp.copy(hasPendingNotifications = true))
            saveNotificationOnHistory(sbn, notification)
        }
    }

    suspend fun saveNotificationOnHistory(sbn: StatusBarNotification, notification: AppNotification) {

        if (notification.title.isEmpty() && notification.content.isEmpty()) return

        insertAppNotificationUseCase(notification)

        sbn.notification.contentIntent?.let {
            PendingIntentCache.add(
                notification.pendingIntentId(), sbn.notification.contentIntent
            )
        }
        saveLargeIcon(sbn, notification)
    }

    suspend fun saveLargeIcon(sbn: StatusBarNotification, notification: AppNotification) = withContext(IO) {

        try {
            val icon = sbn.notification.getLargeIcon() ?: return@withContext
            val drawable = icon.loadDrawable(context) ?: return@withContext

            val bitmap = (drawable as BitmapDrawable).bitmap

            val file = File(context.cacheDir, notification.bitmapId())

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            // Log.d("USUK", "RuleEnforcerImpl.saveLargeIcon: largeIcon for ${sbn.packageName} saved")
        } catch (e: Exception) {
            // Log.e("USUK", "RuleEnforcerImpl.saveLargeIcon: failure while saving notification's large icon from ${sbn.packageName}")
            e.stackTrace
        }

    }


}

