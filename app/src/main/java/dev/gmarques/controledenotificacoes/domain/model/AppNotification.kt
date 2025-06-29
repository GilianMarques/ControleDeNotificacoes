package dev.gmarques.controledenotificacoes.domain.model

import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 14:21.
 *
 * Representa a notificação bloqueada de um app
 * Voce pode usar  [AppNotificationExtensionFun.createFromStatusBarNotification]
 */
data class AppNotification(
    val packageId: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
) : Serializable