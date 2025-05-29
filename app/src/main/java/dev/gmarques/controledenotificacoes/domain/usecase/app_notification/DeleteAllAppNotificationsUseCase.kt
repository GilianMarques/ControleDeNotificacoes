package dev.gmarques.controledenotificacoes.domain.usecase.app_notification

import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import javax.inject.Inject

class DeleteAllAppNotificationsUseCase @Inject constructor(private val repository: AppNotificationRepository) {
    suspend operator fun invoke(packageId: String) = repository.deleteAll(packageId)
}