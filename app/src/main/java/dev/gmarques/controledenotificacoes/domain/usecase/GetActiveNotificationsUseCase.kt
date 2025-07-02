package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.data.repository.ActiveNotificationRepository
import dev.gmarques.controledenotificacoes.presentation.model.ActiveStatusBarNotification
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

    operator fun invoke(): Flow<List<ActiveStatusBarNotification>> = callbackFlow {
        trySend(repository.getActiveNotifications()).isSuccess
        close() // garante emissão única e encerramento do flow
        awaitClose { }
    }


}

