package dev.gmarques.controledenotificacoes.framework


import android.content.Context
import android.service.notification.StatusBarNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.data.repository.ActiveNotificationRepository
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

    override fun getActiveNotifications(): List<ActiveStatusBarNotification> {

        val nots = NotificationListener.instance?.getFilteredActiveNotifications() ?: emptyList()
        return mapStatusBarNotificationsToActiveStatusBarNotifications(nots)
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

    companion object {
        const val INTENT_FILTER_FOR_BROADCAST = "NotificationListener.ActiveNotificationsResponse"
        const val EXTRA_NOTIFICATIONS = "extra_active_notifications"
    }
}

