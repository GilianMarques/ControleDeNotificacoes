package dev.gmarques.controledenotificacoes.domain.repository

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import kotlinx.coroutines.flow.Flow

/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 16:55.
 */
interface ManagedAppRepository {
    suspend fun addOrUpdateManagedAppOrThrow(rule: ManagedApp)
    suspend fun updateManagedAppOrThrow(rule: ManagedApp)
    suspend fun removeManagedApp(rule: ManagedApp)
    suspend fun getManagedAppById(id: String): ManagedApp?
    fun observeAllManagedApps(): Flow<List<ManagedApp>>
}
