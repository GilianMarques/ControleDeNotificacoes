package dev.gmarques.controledenotificacoes.framework.notification_service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.di.entry_points.RuleEnforcerEntryPoint
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
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

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val pkg = sbn.packageName
        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE) ?: ""
        val content = sbn.notification.extras.getString(Notification.EXTRA_TEXT) ?: ""

        val not = AppNotification(pkg, title, content)
        val x = System.currentTimeMillis()
        launch {
            ruleEnforcer.enforceOnNotification(not) {
            /*    Log.d(
                    "USUK",
                    "NotificationListener.onNotificationPosted: ${it.title} cancelada processTime: ${System.currentTimeMillis() - x}mls"
                )*/
                cancelNotification(sbn.key)
            }
        }

    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Conectado — serviço pronto
        Log.d("USUK", "NotificationListener.".plus("onListenerConnected() "))
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("USUK", "NotificationListener.".plus("onCreate() "))
    }

    override fun onListenerDisconnected() {
        cancel() //cancela corrotinas em execução quando o serviço é desconectado
        super.onListenerDisconnected()
        Log.d("USUK", "NotificationListener.".plus("onListenerDisconnected() "))
    }


}

