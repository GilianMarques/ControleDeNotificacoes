package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.repository.AppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 21:05.
 */
class GetInstalledAppsUseCase @Inject constructor(private val repository: AppRepository) {

    suspend operator fun invoke(targetName: String, preSelectedPackages: HashSet<String>): List<InstalledApp> {
        return repository.getInstalledApps(targetName, preSelectedPackages)
    }
}