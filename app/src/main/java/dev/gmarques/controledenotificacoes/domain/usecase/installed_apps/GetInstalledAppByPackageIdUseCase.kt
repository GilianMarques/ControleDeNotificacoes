package dev.gmarques.controledenotificacoes.domain.usecase.installed_apps

import dev.gmarques.controledenotificacoes.domain.data.repository.InstalledAppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 21:05.
 *
 * Caso de uso para obter um aplicativo instalado pelo nome do pacote.
 * Se o aplicativo não estiver instalado, retorna um objeto [InstalledApp] representando um aplicativo desinstalado.
 */
class GetInstalledAppByPackageOrDefaultUseCase @Inject constructor(private val repository: InstalledAppRepository) {

    /**
     * Executa o caso de uso.
     * @param targetPackage O nome do pacote do aplicativo a ser procurado.
     * @return Um objeto [InstalledApp] representando o aplicativo encontrado ou um aplicativo desinstalado.
     */
    suspend operator fun invoke(targetPackage: String): InstalledApp {
        return repository.getInstalledApp(targetPackage) ?: InstalledApp.uninstalledApp(targetPackage)
    }
}