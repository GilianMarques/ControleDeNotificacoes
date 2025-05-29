package dev.gmarques.controledenotificacoes.domain.usecase.app_notification

import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppNotificationsByPkgIdUseCase @Inject constructor(private val repository: AppNotificationRepository) {
    operator fun invoke(pkg: String): Flow<List<AppNotification>> = repository.observeNotificationsByPkgId(pkg)
}