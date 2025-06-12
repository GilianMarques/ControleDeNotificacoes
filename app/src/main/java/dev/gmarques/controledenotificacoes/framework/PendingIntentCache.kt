package dev.gmarques.controledenotificacoes.framework

import android.app.PendingIntent
import android.util.Log

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 02 de junho de 2025 as 09:21.
 */

object PendingIntentCache {

    private val cache = HashMap<String, PendingIntent>()

    operator fun invoke(key: String): PendingIntent? = cache[key]

    fun add(key: String, pendingIntent: PendingIntent) {
        cache[key] = pendingIntent
    }

    /**
     * Removes from cache all pendingIntents related to a specific package
     */
    fun removeAllFrom(packageId: String) {
        cache.keys.filter { it.contains(packageId) }
            .forEach {
                cache.remove(it)
                Log.d("USUK", "PendingIntentCache.clearAllFrom: removing pendingIntent with key: ${it}")
            }
    }

    fun remove(pendingIntentId: String) {
        cache.remove(pendingIntentId)
    }

}