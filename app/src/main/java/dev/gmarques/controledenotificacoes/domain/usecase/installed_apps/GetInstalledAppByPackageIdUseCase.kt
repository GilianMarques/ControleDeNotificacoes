package dev.gmarques.controledenotificacoes.domain.usecase.installed_apps

import dev.gmarques.controledenotificacoes.domain.data.repository.InstalledAppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 21:05.
 */
class GetInstalledAppByPackageUseCase @Inject constructor(private val repository: InstalledAppRepository) {

    suspend operator fun invoke(targetPackage: String): InstalledApp? {
        return repository.getInstalledApp(targetPackage)
    }
}