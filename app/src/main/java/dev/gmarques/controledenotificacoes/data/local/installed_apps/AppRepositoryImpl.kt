package dev.gmarques.controledenotificacoes.data.local.installed_apps


import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.repository.AppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
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

        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val deferredList = apps.map { appInfo ->
            async {
                val appName = packageManager.getApplicationLabel(appInfo).toString()

                if (!isAppValid(appName, appInfo, lowerTarget)) return@async null

                val icon = packageManager.getApplicationIcon(appInfo.packageName)

                InstalledApp(
                    packageId = appInfo.packageName,
                    name = appName,
                    icon = icon
                )
            }
        }

        val resultList = deferredList.awaitAll().filterNotNull()

        return@withContext resultList.sortedBy { it.name }
    }

    private fun isAppValid(appName: String, appInfo: ApplicationInfo, target: String): Boolean {
        val isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
        if (isSystemApp) return false
        return target.isEmpty() || appName.lowercase().contains(target)
    }

}
