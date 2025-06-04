package dev.gmarques.controledenotificacoes.domain.data.repository

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import kotlinx.coroutines.flow.Flow

/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 16:55.
 */
interface ManagedAppRepository {
    suspend fun addOrUpdateManagedAppOrThrow(app: ManagedApp)
    suspend fun updateManagedAppOrThrow(app: ManagedApp)
    suspend fun deleteManagedAppByPackageId(packageId: String)
    suspend fun deleteManagedAppsByRuleId(ruleId: String): Int
    suspend fun getManagedAppByPackageId(id: String): ManagedApp?
    suspend fun getManagedAppsByRuleId(ruleId: String): List<ManagedApp?>
    fun observeAllManagedApps(): Flow<List<ManagedApp>>
    fun observeManagedApp(pkg: String): Flow<ManagedApp?>
}
