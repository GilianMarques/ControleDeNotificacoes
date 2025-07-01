package dev.gmarques.controledenotificacoes.presentation.model

import android.graphics.drawable.Icon

/**
 * Criado por Gilian Marques
 * Em 01/07/2025 as 17:03
 *
 * Representa uma [android.service.notification.StatusBarNotification] apenas com os dados que o app precisa.
 * Serve para evitar dependencia do Domino com o Framework Android.
 */
data class ActiveStatusBarNotification(
    val title: String,
    val content: String,
    val packageId: String,
    val smallIcon: Icon?,
    val largeIcon: Icon?,
    val postTime: Long,
)