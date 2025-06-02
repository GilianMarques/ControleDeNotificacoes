package dev.gmarques.controledenotificacoes.domain.usecase.app_notification

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import dev.gmarques.controledenotificacoes.framework.PendingIntentCache
import javax.inject.Inject

/**
 * Remove todas as notificações de um determinado aplicativo  assim como as PendingIntents e Imagens em cache referentes à essas
 * notificações
 * */
class DeleteAllAppNotificationsUseCase @Inject constructor(
    private val repository: AppNotificationRepository,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(packageId: String) {
        repository.deleteAll(packageId)
        removePendingIntentsFromCache(packageId)
        removeBitmapsFromCache(packageId)
    }

    private fun removeBitmapsFromCache(packageId: String) {
        context.cacheDir.listFiles()?.forEach {
            if (it.name.contains(packageId)) {
                Log.d("USUK", "DeleteAllAppNotificationsUseCase.removeBitmapsFromCache: removing bitmap:  ${it.name}")
                it.delete()
            }
        }
    }

    private fun removePendingIntentsFromCache(packageId: String) {
        PendingIntentCache.cache.keys
            .filter { it.contains(packageId) }
            .forEach {
                PendingIntentCache.cache.remove(it)
                Log.d(
                    "USUK",
                    "DeleteAllAppNotificationsUseCase.removePendingIntentsFromCache: removing pendingIntent with key: ${it}"
                )
            }

    }
}