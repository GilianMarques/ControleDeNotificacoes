package dev.gmarques.controledenotificacoes.data.local.installed_apps


import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.App.Companion.context
import dev.gmarques.controledenotificacoes.domain.repository.AppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 14:57.
 * Implementação da camada de dados para obtenção de apps instalados.
 */
class AppRepositoryImpl @Inject constructor(@ApplicationContext context: Context) : AppRepository {

    private val packageManager: PackageManager = context.packageManager

    override suspend fun getInstalledApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        val ownPackageName = context.packageName

        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 && it.packageName != ownPackageName }
            .map { appInfo ->
                InstalledApp(
                    packageId = appInfo.packageName,
                    name = packageManager.getApplicationLabel(appInfo).toString(),
                    icon = packageManager.getApplicationIcon(appInfo.packageName),
                )
            }
            .sortedBy { it.name }
    }

}
