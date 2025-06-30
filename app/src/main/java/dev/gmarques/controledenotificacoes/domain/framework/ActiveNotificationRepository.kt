package dev.gmarques.controledenotificacoes.domain.framework

import android.service.notification.StatusBarNotification

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:32.
 */
interface ActiveNotificationRepository {
    // TODO: isso viola a ordem das dependencias na clean arch. resolva criando um objeto de dominio.
    fun getActiveNotifications(callback: Callback)

    interface Callback {
        fun onActiveNotificationsReceived(notifications: List<StatusBarNotification>)
    }
}
