package dev.gmarques.controledenotificacoes.framework.notification_service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.di.entry_points.RuleEnforcerEntryPoint
import dev.gmarques.controledenotificacoes.di.entry_points.ScheduleManagerEntryPoint
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextUnlockPeriodFromNow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Criado por Gilian Marques
 * Em sábado, 03 de maio de 2025 as 16:18.
 */
class NotificationListener : NotificationListenerService(), CoroutineScope by MainScope() {

    private val ruleEnforcer = EntryPointAccessors
        .fromApplication(App.context, RuleEnforcerEntryPoint::class.java)
        .getRuleEnforcer()

    private val scheduleManager = EntryPointAccessors
        .fromApplication<ScheduleManagerEntryPoint>(App.context)
        .getScheduleManager()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT //https://blog.stackademic.com/exploring-the-notification-listener-service-in-android-7db54d65eca7
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Conectado — serviço pronto
        //   Log.d("USUK", "NotificationListener.".plus("onListenerConnected() "))

        readActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        manageNotification(sbn)
    }

    /**
     * Lê todas as notificações ativas no momento em que o serviço é conectado.
     * Processa cada notificação ativa usando o método [manageNotification].
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

        launch {
            ruleEnforcer.enforceOnNotification(not) { not, rule ->
                Log.d("USUK", "NotificationListener.manageNotification: cancelling: ${not.title} - ${not.packageId}")
                cancelNotification(notification.key)
                scheduleManager.scheduleAlarm(not.packageId, rule.nextUnlockPeriodFromNow().millis)
            }
        }
    }


    override fun onListenerDisconnected() {
        cancel() //cancela corrotinas em execução quando o serviço é desconectado
        super.onListenerDisconnected()
        Log.d("USUK", "NotificationListener.".plus("onListenerDisconnected() "))
    }

}

