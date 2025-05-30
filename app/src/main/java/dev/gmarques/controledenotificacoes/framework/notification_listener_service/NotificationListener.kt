package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextAppUnlockPeriodFromNow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Criado por Gilian Marques
 * Em sábado, 03 de maio de 2025 as 16:18.
 */
class NotificationListener : NotificationListenerService(), CoroutineScope by MainScope() {

    private val ruleEnforcer = HiltEntryPoints.ruleEnforcer()
    private val scheduleManager = HiltEntryPoints.scheduleManager()
    private val updateManagedAppUseCase = HiltEntryPoints.updateManagedAppUseCase()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT //https://blog.stackademic.com/exploring-the-notification-listener-service-in-android-7db54d65eca7
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("USUK", "NotificationListener.onListenerConnected: ")
        readActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        manageNotification(sbn)
    }

    /**
     * Lê todas as notificações ativas no momento em que o serviço é conectado.
     * Processa cada notificação ativa usando o mét.odo [manageNotification].
     */
    private fun readActiveNotifications() {
        val active = activeNotifications ?: return
        active.forEach { sbn ->
            manageNotification(sbn)
        }
    }

    /**
     * Processa uma notificação recebida.
     * Extrai informações relevantes e as encapsula em um objeto AppNotification.
     * Em seguida, utiliza o RuleEnforcer para aplicar as regras configuradas.
     *
     */
    private fun manageNotification(notification: StatusBarNotification) {

        val pkg = notification.packageName
        val title = notification.notification.extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val content = notification.notification.extras.getString(Notification.EXTRA_TEXT).orEmpty()

        val not = AppNotification(pkg, title, content, System.currentTimeMillis())

        runBlocking {
            ruleEnforcer.enforceOnNotification(not) { not, rule, managedApp ->
                Log.d("USUK", "NotificationListener.manageNotification: cancelling: ${not.title} - ${not.packageId}")
                cancelNotification(notification.key)
                scheduleManager.scheduleAlarm(not.packageId, rule.nextAppUnlockPeriodFromNow())
                launch { updateManagedAppUseCase(managedApp.copy(hasPendingNotifications = true)) }
            }
        }
    }

    override fun onListenerDisconnected() {
        cancel()
        super.onListenerDisconnected()
        Log.d("USUK", "NotificationListener.".plus("onListenerDisconnected() "))
    }

}

