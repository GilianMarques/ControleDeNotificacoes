package dev.gmarques.controledenotificacoes.data.local.installed_apps


import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.repository.AppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 14:57.
 * Implementação da camada de dados para obtenção de apps instalados.
 */
class AppRepositoryImpl @Inject constructor(@ApplicationContext context: Context) : AppRepository {

    private val packageManager: PackageManager = context.packageManager


    override suspend fun getInstalledApps(targetName: String): List<InstalledApp> = withContext(IO) {

        val lowerTarget = targetName.lowercase()

        return@withContext packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .mapNotNull { appInfo ->

                val appName = packageManager.getApplicationLabel(appInfo).toString()

                if (!isAppValid(appName, appInfo, lowerTarget)) return@mapNotNull null

                InstalledApp(
                    packageId = appInfo.packageName,
                    name = appName,
                    icon = packageManager.getApplicationIcon(appInfo.packageName),
                )
            }
            .sortedBy { it.name }
    }

    private fun isAppValid(appName: String, appInfo: ApplicationInfo, target: String): Boolean {
        val isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
        if (isSystemApp) return false
        return target.isEmpty() || appName.lowercase().contains(target)
    }

}
