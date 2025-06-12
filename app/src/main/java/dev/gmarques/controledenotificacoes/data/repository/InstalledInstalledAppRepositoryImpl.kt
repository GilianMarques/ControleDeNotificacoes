package dev.gmarques.controledenotificacoes.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.data.repository.InstalledAppRepository
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 14:57.
 * Implementação da camada de dados para obtenção de apps instalados.
 */
class InstalledInstalledAppRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase,
) : InstalledAppRepository {
    // TODO: nao to usando o cache ao ler todos os apps?
    private val packageManager: PackageManager = context.packageManager
    private val iconCache = object : LruCache<String, Drawable>(100) {}
    private val appCache = object : LruCache<String, InstalledApp>(100) {}

    /**
     * Busca e retorna uma lista de aplicativos instalados no dispositivo, filtrando-os opcionalmente
     * por um nome alvo e excluindo aplicativos com base em uma lista de pacotes.
     *
     * A função opera em background para evitar bloqueios na thread principal durante a busca e o
     * processamento dos dados.
     *
     * @param targetName O nome (ou parte do nome) a ser usado como filtro para os aplicativos.
     *                   A busca ignora maiúsculas e minúsculas. Se uma string vazia for fornecida,
     *                   todos os aplicativos (respeitando os outros filtros) serão retornados.
     * @param includeSystemApps Booleano indicando se aplicativos de sistema devem ser incluídos
     *                          nos resultados. Se `false`, aplicativos de sistema são excluídos.
     * @param includeManagedApps Booleano indicando se aplicativos gerenciados (aqueles que
     *                           já existem no banco de dados de aplicativos gerenciados) devem
     *                           ser incluídos nos resultados. Se `false`, aplicativos gerenciados
     *                           são excluídos.
     * @param excludePackages Um conjunto de IDs de pacotes. Aplicativos cujos IDs estejam neste conjunto
     *   serão excluídos da lista de resultados. Além disso, aplicativos considerados inválidos pela
     *   função `isAppValid` também serão excluídos.
     * @return Uma lista de [InstalledApp]s, contendo informações sobre os aplicativos que
     *   correspondem aos critérios de filtro e exclusão. A lista é ordenada alfabeticamente pelo
     *   nome do aplicativo. Retorna uma lista vazia se nenhum aplicativo corresponder aos critérios.
     */
    override suspend fun getInstalledApps(
        targetName: String,
        includeSystemApps: Boolean,
        includeManagedApps: Boolean,
        excludePackages: HashSet<String>,
    ): List<InstalledApp> =
        withContext(Dispatchers.IO) {

            val lowerTargetName = targetName.lowercase()

            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            return@withContext installedApps.map { appInfo ->
                async {

                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    var managedApp = false

                    if (excludePackages.contains(appInfo.packageName)) return@async null
                    if (!includeSystemApps && isSystemApp(appInfo)) return@async null
                    if (isManagedApp(appInfo).also { managedApp = it } && !includeManagedApps) return@async null
                    if (!isAppNameMatchingSearchQuery(lowerTargetName, appName)) return@async null

                    InstalledApp(
                        packageId = appInfo.packageName,
                        name = appName,
                        isBeingManaged = managedApp
                    )
                }
            }
                .awaitAll()
                .filterNotNull()
                .sortedBy { it.name }

        }


    /**
     * Verifica se o aplicativo é um aplicativo de sistema.
     *
     * @param appInfo As informações do aplicativo a serem verificadas.
     * @return `true` se o aplicativo for um aplicativo de sistema, `false` caso contrário.
     */
    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    /**
     * Verifica se o aplicativo já está gerenciado (existe no banco de dados de aplicativos gerenciados).
     *
     * @param appInfo As informações do aplicativo a serem verificadas.
     * @return `true` se o aplicativo for gerenciado, `false` caso contrário.
     */
    private suspend fun isManagedApp(appInfo: ApplicationInfo): Boolean {
        return getManagedAppByPackageIdUseCase(appInfo.packageName) != null
    }

    /**
     * Verifica se o nome do aplicativo corresponde à consulta de busca.
     *
     * A correspondência é feita de forma case-insensitive.
     *
     * @param lowerTargetName A consulta de busca.
     * @param appName O nome do aplicativo a ser verificado.
     * @return `true` se o nome do aplicativo corresponder à consulta de busca (ou se a consulta
     *         estiver vazia), `false` caso contrário.
     */
    private fun isAppNameMatchingSearchQuery(lowerTargetName: String, appName: String): Boolean {
        return lowerTargetName.isEmpty() || appName.lowercase().contains(lowerTargetName)
    }

    /**
     * Busca o ícone do aplicativo pelo seu ID de pacote. Utiliza um cache LRU para armazenar ícones
     * previamente carregados.
     */

    override suspend fun getDrawable(pkg: String): Drawable? = withContext(Dispatchers.IO) {
        iconCache.get(pkg) ?: runCatching {
            val drawable = packageManager.getApplicationIcon(pkg)
            iconCache.put(pkg, drawable)
            drawable
        }.getOrNull()
    }

    /**
     * Recupera um aplicativo instalado específico pelo seu ID de pacote.
     *
     * @param pkg O ID do pacote do aplicativo a ser buscado.
     * @return O [InstalledApp] correspondente ao ID de pacote fornecido, ou `null` se nenhum
     * aplicativo com o ID especificado for encontrado.
     */
    override suspend fun getInstalledApp(pkg: String): InstalledApp? = withContext(Dispatchers.IO) {
        appCache.get(pkg) ?: runCatching {

            val appInfo = packageManager.getApplicationInfo(pkg, PackageManager.GET_META_DATA)
            val appName = packageManager.getApplicationLabel(appInfo).toString()

            val installedApp = InstalledApp(
                packageId = pkg,
                name = appName,
                isBeingManaged = getManagedAppByPackageIdUseCase(pkg) != null
            )

            appCache.put(pkg, installedApp)

            installedApp

        }.getOrNull()
    }


}