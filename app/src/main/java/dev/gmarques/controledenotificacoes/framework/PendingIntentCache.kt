package dev.gmarques.controledenotificacoes.framework

import android.app.PendingIntent

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 02 de junho de 2025 as 09:21.
 */
object PendingIntentCache {
    // TODO: gerenciar melhor esse cache
    val cache = HashMap<String, PendingIntent>()
}