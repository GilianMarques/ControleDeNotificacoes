package dev.gmarques.controledenotificacoes.domain.usecase.installed_apps

import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.domain.data.repository.InstalledAppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 21:05.
 */
class GetAllInstalledAppsUseCase @Inject constructor(private val repository: InstalledAppRepository) {

    /**
     * Retorna uma lista de aplicativos instalados no dispositivo.
     *
     * @param targetName Filtra os aplicativos por nome (parcial). O padrão é uma string vazia,
     *                   que retorna todos os aplicativos.
     * @param excludePackages Um conjunto de nomes de pacotes a serem excluídos da lista. O padrão é
     *                        um conjunto vazio.
     * @param includeSystemApps Se `true`, inclui aplicativos do sistema na lista. O padrão é o valor
     *                          de `PreferencesImpl.prefIncludeSystemApps.value`. **Este parâmetro é
     *                          destinado apenas para testes. Em produção, não é necessário inicializá-lo.**
     * @param includeManagedApps Se `true`, inclui aplicativos gerenciados na lista. O padrão é o valor
     *                           de `PreferencesImpl.prefIncludeManagedApps.value`. **Este parâmetro é
     *                           destinado apenas para testes. Em produção, não é necessário inicializá-lo.**
     * @return Uma lista de objetos `InstalledApp` que correspondem aos critérios de filtro.
     */
    suspend operator fun invoke(
        targetName: String = "",
        excludePackages: HashSet<String> = hashSetOf<String>(),
        includeSystemApps: Boolean = PreferencesImpl.prefIncludeSystemApps.value,
        includeManagedApps: Boolean = PreferencesImpl.prefIncludeManagedApps.value,
    ): List<InstalledApp> {
        return repository.getInstalledApps(
            targetName,
            includeSystemApps,
            includeManagedApps,
            excludePackages
        )
    }
}