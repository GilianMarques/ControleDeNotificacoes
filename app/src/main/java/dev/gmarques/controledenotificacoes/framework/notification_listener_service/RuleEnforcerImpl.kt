package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.service.notification.StatusBarNotification
import com.bumptech.glide.Glide
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.framework.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextAppUnlockPeriodFromNow
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
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

    override suspend fun enforceOnNotification(
        sbn: StatusBarNotification,
        removeNotificationCallback: (AppNotification, Rule, ManagedApp) -> Any,
    ) = withContext(IO) {

        val pkg = sbn.packageName
        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val content = sbn.notification.extras.getString(Notification.EXTRA_TEXT).orEmpty()

        val notification = AppNotification(pkg, title, content, System.currentTimeMillis())

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


    private suspend fun saveNotificationOnHistory(sbn: StatusBarNotification, notification: AppNotification) {

        if (notification.title.isEmpty() && notification.content.isEmpty()) return

        saveLargeIcon(sbn)
// TODO: salvar a intent https://chatgpt.com/c/683b7135-46b4-8001-a2e5-6f1668739493 
        insertAppNotificationUseCase(
            AppNotification(
                notification.packageId,
                notification.title,
                notification.content,
                sbn.postTime
            )
        )

    }

    suspend fun saveLargeIcon(sbn: StatusBarNotification) = withContext(IO) {
// TODO: Quando a notificação for removida o ícone também deve ser movido de cachê

        val icon = sbn.notification.getLargeIcon() ?: return@withContext
        val drawable = icon.loadDrawable(context) ?: return@withContext

        val bitmap = Glide.with(context)
            .asBitmap()
            .load(drawable)
            .submit()
            .get()


        val filename = "${sbn.packageName}_${sbn.postTime}.png"

        val file = File(context.cacheDir, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

    }


}
