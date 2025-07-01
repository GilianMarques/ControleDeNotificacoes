package dev.gmarques.controledenotificacoes.framework


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.notification.StatusBarNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.framework.ActiveNotificationRepository
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import dev.gmarques.controledenotificacoes.presentation.model.ActiveStatusBarNotification
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:34.
 */
class ActiveNotificationRepositoryImpl @Inject constructor(@ApplicationContext private val appContext: Context) :
    ActiveNotificationRepository {
    var receiver: BroadcastReceiver? = null

    override fun getActiveNotifications(callback: ActiveNotificationRepository.Callback) {

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                val activeNotifications = extractNotificationsFromIntent(intent)
                callback.done(mapStatusBarNotificationsToActiveStatusBarNotifications(activeNotifications))
                appContext.unregisterReceiver(receiver)
            }
        }

        registerThisReceiver(receiver!!)

        /** faz o [NotificationListener] disparar um broadcast com "todas" as notificações ativas */
        NotificationListener.sendBroadcastToPostActiveNotifications()
    }

    /**
     * Extrai a lista de [StatusBarNotification] de um [Intent].
     *
     * Esta função é responsável por obter a lista de notificações ativas que foram
     * enviadas através de um Intent. Ela lida com as diferenças de API entre as versões
     * do Android para garantir a compatibilidade.
     */
    private fun extractNotificationsFromIntent(intent: Intent?): List<StatusBarNotification> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableArrayListExtra(EXTRA_NOTIFICATIONS, StatusBarNotification::class.java)
        } else {
            @Suppress("DEPRECATION") intent?.getParcelableArrayListExtra(EXTRA_NOTIFICATIONS)
        }.orEmpty()
    }

    /**
     * Mapeia uma lista de [StatusBarNotification] para uma lista de [ActiveStatusBarNotification].
     *
     * Esta função transforma a representação de notificações do sistema em um modelo
     * mais adequado para a camada de apresentação da aplicação.
     */
    private fun mapStatusBarNotificationsToActiveStatusBarNotifications(
        statusBarNotifications: List<StatusBarNotification>,
    ): List<ActiveStatusBarNotification> {
        return statusBarNotifications.map { sbn ->
            sbn.notification.extras

            val appNot = AppNotificationExtensionFun.createFromStatusBarNotification(sbn)

            ActiveStatusBarNotification(
                title = appNot.title,
                content = appNot.content,
                packageId = appNot.packageId,
                postTime = appNot.timestamp,
                smallIcon = sbn.notification.smallIcon,
                largeIcon = sbn.notification.getLargeIcon(),
            )
        }
    }

    private fun registerThisReceiver(receiver: BroadcastReceiver) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, IntentFilter(INTENT_FILTER_FOR_BROADCAST), RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION") @SuppressLint("UnspecifiedRegisterReceiverFlag") appContext.registerReceiver(
                receiver,
                IntentFilter(INTENT_FILTER_FOR_BROADCAST)
            )
        }
    }

    companion object {
        const val INTENT_FILTER_FOR_BROADCAST = "NotificationListener.ActiveNotificationsResponse"
        const val EXTRA_NOTIFICATIONS = "extra_active_notifications"
    }
}

