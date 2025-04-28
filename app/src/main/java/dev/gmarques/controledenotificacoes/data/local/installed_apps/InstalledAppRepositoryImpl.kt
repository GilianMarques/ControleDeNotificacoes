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
class InstalledAppRepositoryImpl @Inject constructor(@ApplicationContext context: Context) : AppRepository {

    private val packageManager: PackageManager = context.packageManager

    /**
     * Busca e retorna uma lista de aplicativos instalados no dispositivo, filtrando-os opcionalmente
     * por um nome alvo e excluindo aplicativos com base em uma lista de pacotes.
     *
     * A função opera em background (usando `withContext(IO)`) para evitar bloqueios na thread principal
     * durante a busca e o processamento dos dados.
     *
     * @param targetName O nome (ou parte do nome) a ser usado como filtro para os aplicativos. A busca
     *   ignora maiúsculas e minúsculas. Se uma string vazia for fornecida, todos os aplicativos
     *   (exceto os do sistema) serão retornados.
     * @param excludePackages Um conjunto de IDs de pacotes. Aplicativos cujos IDs estejam neste conjunto
     *   serão excluídos da lista de resultados. Além disso, aplicativos considerados inválidos pela
     *   função `isAppValid` também serão excluídos.
     * @return Uma lista de [InstalledApp]s, contendo informações sobre os aplicativos que correspondem
     *   aos critérios de filtro e exclusão. A lista é ordenada alfabeticamente pelo nome do aplicativo.
     */
    override suspend fun getInstalledApps(targetName: String, excludePackages: HashSet<String>): List<InstalledApp> =
        withContext(IO) {

            val lowerTarget = targetName.lowercase()

            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            return@withContext apps.map { appInfo ->
                async {

                    val appName = packageManager.getApplicationLabel(appInfo).toString()

                    if (excludePackages.contains(appInfo.packageName) ||
                        !isAppValid(
                            appName,
                            appInfo,
                            lowerTarget
                        )
                    ) return@async null

                    val icon = packageManager.getApplicationIcon(appInfo.packageName)

                    InstalledApp(
                        packageId = appInfo.packageName,
                        name = appName,
                        icon = icon,
                    )
                }
            }
                .awaitAll()
                .filterNotNull()
                .sortedBy { it.name }

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

    /**
     * Recupera um aplicativo instalado específico pelo seu ID de pacote.
     *
     * Esta função procura na lista de aplicativos instalados por um que corresponda ao ID
     * de pacote fornecido. Se encontrado, retorna um objeto [InstalledApp] que contém informações
     * sobre o aplicativo.
     *
     * @param packageId O ID do pacote do aplicativo a ser buscado.
     * @return O [InstalledApp] correspondente ao ID de pacote fornecido, ou `null` se nenhum
     * aplicativo com o ID especificado for encontrado.
     */
    override suspend fun getInstalledAppByPackage(packageId: String): InstalledApp? = withContext(IO) {

        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { appInfo ->
                if (packageId != appInfo.packageName) return@map
                val icon = packageManager.getApplicationIcon(appInfo.packageName)
                val appName = packageManager.getApplicationLabel(appInfo).toString()

                return@withContext InstalledApp(
                    packageId = appInfo.packageName,
                    name = appName,
                    icon = icon,
                )

            }

        return@withContext null
    }

}
