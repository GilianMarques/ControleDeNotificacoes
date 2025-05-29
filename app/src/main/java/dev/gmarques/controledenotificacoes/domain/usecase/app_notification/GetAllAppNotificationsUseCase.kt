package dev.gmarques.controledenotificacoes.domain.usecase.app_notification

import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import javax.inject.Inject

class GetAllAppNotificationsUseCase @Inject constructor(private val repository: AppNotificationRepository) {
    suspend operator fun invoke() = repository.getAll()
}