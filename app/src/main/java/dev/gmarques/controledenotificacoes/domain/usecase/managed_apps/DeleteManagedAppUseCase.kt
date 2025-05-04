package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.repository.ManagedAppRepository
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 02 de maio de 2025 as 22:34.
 */
class DeleteManagedAppUseCase @Inject constructor(
    private val repository: ManagedAppRepository,
) {

    suspend operator fun invoke(packageId: String) {
        repository.deleteManagedAppByPackageId(packageId)
    }
}