package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.framework.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.bitmapId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.pendingIntentId
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextAppUnlockPeriodFromNow
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import dev.gmarques.controledenotificacoes.framework.PendingIntentCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val scheduleManager: ScheduleManager,
    private val updateManagedAppUseCase: dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase,
    private val insertAppNotificationUseCase: InsertAppNotificationUseCase,
    @ApplicationContext private val context: Context,
) : RuleEnforcer, CoroutineScope by CoroutineScope(IO) {

    private lateinit var notification: AppNotification

    override suspend fun enforceOnNotification(
        sbn: StatusBarNotification,
        removeNotificationCallback: (AppNotification, Rule, ManagedApp) -> Any,
    ) = withContext(IO) {


        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val content = sbn.notification.extras.getString(Notification.EXTRA_TEXT).orEmpty()

        notification = AppNotification(sbn.packageName, title, content, sbn.postTime)

        val managedApp = getManagedAppByPackageIdUseCase(notification.packageId)

        if (managedApp == null) {
            return@withContext
        }
        val rule = getRuleByIdUseCase(managedApp.ruleId)
            ?: error("Um app gerenciado deve ter uma regra. Isso é um Bug $managedApp")


        if (rule.isAppInBlockPeriod()) {
            removeNotificationCallback(notification, rule, managedApp)

            launch {
                scheduleManager.scheduleAlarm(notification.packageId, rule.nextAppUnlockPeriodFromNow())
                updateManagedAppUseCase(managedApp.copy(hasPendingNotifications = true))
                saveNotificationOnHistory(sbn, notification)
            }
        }

    }

    override suspend fun saveNotificationOnHistory(sbn: StatusBarNotification, notification: AppNotification) {

        if (notification.title.isEmpty() && notification.content.isEmpty()) return

        insertAppNotificationUseCase(notification)

        PendingIntentCache.add(notification.pendingIntentId(), sbn.notification.contentIntent)
        saveLargeIcon(sbn)
    }

    override suspend fun saveLargeIcon(sbn: StatusBarNotification) = withContext(IO) {

        try {
            val icon = sbn.notification.getLargeIcon() ?: return@withContext
            val drawable = icon.loadDrawable(context) ?: return@withContext

            val bitmap = (drawable as BitmapDrawable).bitmap

            val file = File(context.cacheDir, notification.bitmapId())

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.d("USUK", "RuleEnforcerImpl.saveLargeIcon: largeIcon for ${sbn.packageName} saved")
        } catch (e: Exception) {
            Log.e("USUK", "RuleEnforcerImpl.saveLargeIcon: error while saving notification's large icon from ${sbn.packageName}")
            e.stackTrace
        }

    }


}

