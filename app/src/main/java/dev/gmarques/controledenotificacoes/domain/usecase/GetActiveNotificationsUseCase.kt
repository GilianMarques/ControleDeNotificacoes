package dev.gmarques.controledenotificacoes.domain.usecase

import android.service.notification.StatusBarNotification
import android.util.Log
import dev.gmarques.controledenotificacoes.domain.framework.ActiveNotificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:36.
 */
class GetActiveNotificationsUseCase @Inject constructor(
    private val repository: ActiveNotificationRepository,
) {

    operator fun invoke(): Flow<List<StatusBarNotification>> = callbackFlow {

        val callback = object : ActiveNotificationRepository.Callback {
            override fun onActiveNotificationsReceived(notifications: List<StatusBarNotification>) {
                trySend(notifications).isSuccess
                close() // garante emissão única e encerramento do flow
            }
        }
        Log.d("USUK", "GetActiveNotificationsUseCase.invoke: ")
        repository.getActiveNotifications(callback)

        awaitClose {
            // nada a limpar, pois o receptor é removido internamente (TODO futuro)
        }
    }


}

