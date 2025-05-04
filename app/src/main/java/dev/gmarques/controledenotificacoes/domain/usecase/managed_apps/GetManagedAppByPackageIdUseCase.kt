package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.repository.ManagedAppRepository
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 04 de maio de 2025 às 14:39.
 */
class GetManagedAppByPackageIdUseCase @Inject constructor(private val repository: ManagedAppRepository) {
    suspend operator fun invoke(id: String): ManagedApp? {
        return repository.getManagedAppByPackageId(id)
    }
}
