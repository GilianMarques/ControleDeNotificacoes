package dev.gmarques.controledenotificacoes.domain.usecase.installed_apps

import dev.gmarques.controledenotificacoes.domain.repository.AppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 21:05.
 */
class GetAllInstalledAppsUseCase @Inject constructor(private val repository: AppRepository) {

    suspend operator fun invoke(
        targetName: String = "",
        excludePackages: HashSet<String> = hashSetOf<String>(),
    ): List<InstalledApp> {
        return repository.getInstalledApps(targetName, excludePackages)
    }
}