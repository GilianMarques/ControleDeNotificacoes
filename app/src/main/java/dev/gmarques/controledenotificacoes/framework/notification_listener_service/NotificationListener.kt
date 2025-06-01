package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

/**
 * Criado por Gilian Marques
 * Em sábado, 03 de maio de 2025 as 16:18.
 */
class NotificationListener : NotificationListenerService(), CoroutineScope by MainScope() {

    private val ruleEnforcer = HiltEntryPoints.ruleEnforcer()


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
    private fun manageNotification(sbn: StatusBarNotification) {

        runBlocking {
            ruleEnforcer.enforceOnNotification(sbn) { not, rule, managedApp ->
                Log.d("USUK", "NotificationListener.manageNotification: cancelling: ${not.title} - ${not.packageId}")
                cancelNotification(not.packageId)
            }
        }
    }


    override fun onListenerDisconnected() {
        cancel()
        super.onListenerDisconnected()
        Log.d("USUK", "NotificationListener.".plus("onListenerDisconnected() "))
    }

}

