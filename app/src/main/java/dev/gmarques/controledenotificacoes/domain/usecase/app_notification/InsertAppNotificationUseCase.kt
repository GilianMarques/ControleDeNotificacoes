package dev.gmarques.controledenotificacoes.domain.usecase.app_notification

import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import javax.inject.Inject

class InsertAppNotificationUseCase @Inject constructor(private val repository: AppNotificationRepository) {
    suspend operator fun invoke(notification: AppNotification) = repository.insert(notification)
}

