package dev.gmarques.controledenotificacoes.data.repository

import dev.gmarques.controledenotificacoes.data.local.room.dao.AppNotificationDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.AppNotificationMapper
import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppNotificationRepositoryImpl @Inject constructor(
    private val dao: AppNotificationDao,
) : AppNotificationRepository {

    override suspend fun insert(notification: AppNotification) {
        AppNotificationMapper.toEntity(notification).let { dao.insert(it) }
    }

    override suspend fun deleteAll(packageId: String) {
        dao.deleteAll(packageId)
    }

    override suspend fun getByPkg(pkg: String): AppNotification? {
        return dao.getByPkg(pkg)?.let { AppNotificationMapper.toModel(it) }
    }

    override suspend fun getAll(): List<AppNotification> {
        return dao.getAll().map { AppNotificationMapper.toModel(it) }
    }

    override fun observeNotificationsByPkgId(pkg: String): Flow<List<AppNotification>> {
        return dao.observeNotificationsByPkgId(pkg).map { list ->
            list.map { AppNotificationMapper.toModel(it) }
        }
    }
}