package dev.gmarques.controledenotificacoes.domain.data.repository

import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface AppNotificationRepository {
    suspend fun insert(notification: AppNotification)
    suspend fun deleteAll(packageId: String)
    suspend fun getByPkg(pkg: String): AppNotification?
    suspend fun getAll(): List<AppNotification>
    fun observeNotificationsByPkgId(pkg: String): Flow<List<AppNotification>>
}