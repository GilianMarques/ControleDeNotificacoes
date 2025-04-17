package dev.gmarques.controledenotificacoes.data.local.installed_apps


import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.repository.AppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 14:57.
 * Implementação da camada de dados para obtenção de apps instalados.
 */
class AppRepositoryImpl @Inject constructor(@ApplicationContext context: Context) : AppRepository {

    private val packageManager: PackageManager = context.packageManager

    /**
     * Retorna uma lista de aplicativos instalados no dispositivo que correspondem ao nome de destino
     * e indica se cada aplicativo foi pré-selecionado.
     *
     * @param targetName O nome (ou parte do nome) do aplicativo a ser filtrado. A busca é
     *  case-insensitive. Se vazio, retorna todos os aplicativos instalados (não de sistema).
     * @param preSelectedPackages Um conjunto de IDs de pacotes de aplicativos que devem ser
     *  marcados como pré-selecionados na lista resultante.
     * @return Uma lista de [InstalledApp]s, ordenada por pré-seleção (decrescente) e, em seguida,
     *  por nome (crescente).
     */
    override suspend fun getInstalledApps(targetName: String, preSelectedPackages: HashSet<String>): List<InstalledApp> =
        withContext(IO) {

            val lowerTarget = targetName.lowercase()

            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            val deferredList = apps.map { appInfo ->
                async {
                    val appName = packageManager.getApplicationLabel(appInfo).toString()

                    if (!isAppValid(appName, appInfo, lowerTarget)) return@async null

                    val icon = packageManager.getApplicationIcon(appInfo.packageName)

                    InstalledApp(
                        packageId = appInfo.packageName,
                        name = appName,
                        icon = icon,
                        preSelected = preSelectedPackages.contains(appInfo.packageName)
                    )
                }
            }

            val resultList = deferredList.awaitAll().filterNotNull()

            return@withContext resultList.sortedWith(
                compareByDescending<InstalledApp> { it.preSelected }
                    .thenBy { it.name }
            )
        }

    /**
     * Verifica se um aplicativo é válido para ser exibido na lista.
     *
     * Um aplicativo é considerado válido se não for um aplicativo de sistema
     * e se o nome do aplicativo contiver o termo de busca (ignorando maiúsculas/minúsculas),
     * a menos que o termo de busca seja vazio, indicando que todos os aplicativos
     * não de sistema devem ser retornados.
     *
     * @param appName O nome do aplicativo.
     * @param appInfo As informações do aplicativo, incluindo suas flags.
     * @param target O termo de busca para filtrar os aplicativos pelo nome.
     * @return `true` se o aplicativo for válido, `false` caso contrário.
     */
    private fun isAppValid(appName: String, appInfo: ApplicationInfo, target: String): Boolean {
        val isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
        if (isSystemApp) return false
        return target.isEmpty() || appName.lowercase().contains(target)
    }

}
