package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.repository.ManagedAppRepository
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 24 de abril de 2025 às 17:36.
 */
class AddManagedAppUseCase @Inject constructor(private val repository: ManagedAppRepository) {
    suspend operator fun invoke(app: ManagedApp) {
        repository.addManagedAppOrThrow(app)
    }
}
