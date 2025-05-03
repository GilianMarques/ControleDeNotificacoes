package dev.gmarques.controledenotificacoes.domain.repository

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import kotlinx.coroutines.flow.Flow

/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 16:55.
 */
interface ManagedAppRepository {
    suspend fun addOrUpdateManagedAppOrThrow(app: ManagedApp)
    suspend fun updateManagedAppOrThrow(app: ManagedApp)
    suspend fun deleteManagedAppById(packageId: String)
    suspend fun getManagedAppById(id: String): ManagedApp?
    suspend fun deleteManagedAppsByRuleId(ruleId: String): Int
    fun observeAllManagedApps(): Flow<List<ManagedApp>>
}
