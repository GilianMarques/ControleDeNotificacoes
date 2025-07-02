package dev.gmarques.controledenotificacoes.domain.data.repository

import dev.gmarques.controledenotificacoes.presentation.model.ActiveStatusBarNotification

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:32.
 */
interface ActiveNotificationRepository {
    fun getActiveNotifications(): List<ActiveStatusBarNotification>
}