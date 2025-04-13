package dev.gmarques.controledenotificacoes.domain.repository

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp

/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 16:55.
 */
interface ManagedAppRepository {
    suspend fun addManagedApp(rule: ManagedApp)
    suspend fun updateManagedApp(rule: ManagedApp)
    suspend fun removeManagedApp(rule: ManagedApp)
    suspend fun getManagedAppById(id: String): ManagedApp?
    suspend fun getAllManagedApps(): List<ManagedApp>
}
