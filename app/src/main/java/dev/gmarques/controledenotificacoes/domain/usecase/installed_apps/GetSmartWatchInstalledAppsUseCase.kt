package dev.gmarques.controledenotificacoes.domain.usecase.installed_apps

import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 12/06/2025 as 17:37
 *
 * Use case responsável por obter a lista de aplicativos de smartwatch instalados no dispositivo.
 *
 * Este use case utiliza uma lista predefinida de nomes de pacotes de aplicativos de smartwatch
 * e verifica quais deles estão efetivamente instalados no dispositivo do usuário.
 *
 * @property getInstalledAppByPackageOrDefaultUseCase Use case para obter um aplicativo instalado pelo nome do pacote ou um valor padrão.
 */
class GetSmartWatchInstalledAppsUseCase @Inject constructor(
    private val getInstalledAppByPackageOrDefaultUseCase: GetInstalledAppByPackageOrDefaultUseCase,
    private val a: GetAllInstalledAppsUseCase,
) {

    private val smartwatchAppPackages = listOf(
        "com.samsung.android.app.watchmanager",
        "com.yingsheng.hayloufun",
        "com.crrepa.band.dafit",
        "com.watch.life",
        "com.veryfit.multi",
        "com.xiaomi.wearable",
        "com.zepp.mgrowth"
    )

    /**
     * Executa o use case.
     *
     * @return Uma lista de objetos [InstalledApp] representando os aplicativos de smartwatch
     *         instalados no dispositivo. Retorna uma lista vazia se nenhum dos aplicativos da lista predefinida estiver instalado.
     */
    suspend operator fun invoke(): List<InstalledApp> {

        val apps = mutableListOf<InstalledApp>()

        // return apps
        // apps.addAll(a.invoke())

        smartwatchAppPackages.forEach {
            val app = getInstalledAppByPackageOrDefaultUseCase(it)
            if (!app.uninstalled) apps.add(app)
        }

        return apps
    }
}