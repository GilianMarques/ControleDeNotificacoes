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
 *
 * Este usecase é usando em uma Transação do Room. Não crie corrotinas ou mude o escopo do contexto para que a
 * transação não perca o efeito.
 * */
class DeleteAllAppNotificationsUseCase @Inject constructor(
    private val repository: AppNotificationRepository,
    @ApplicationContext private val context: Context,
) {
    /**
     * Limpa o cache de intents e de bitmaps relacionado ao app e por fim apaga as notificaçoes do db
     * de forma que o cache bao fique orfao se houver algum erro entre as operações
     */
    suspend operator fun invoke(packageId: String) {
        removeBitmapsFromCache(packageId)
        removePendingIntentsFromCache(packageId)
        repository.deleteAll(packageId)
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
        PendingIntentCache.removeAllFrom(packageId)
    }
}