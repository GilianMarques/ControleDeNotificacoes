package dev.gmarques.controledenotificacoes.domain.usecase.app_notification

import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.repository.AppNotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotificationsByPkgIdUseCase @Inject constructor(private val repository: AppNotificationRepository) {
    operator fun invoke(pkg: String): Flow<List<AppNotification>> = repository.observeNotificationsByPkgId(pkg)
}