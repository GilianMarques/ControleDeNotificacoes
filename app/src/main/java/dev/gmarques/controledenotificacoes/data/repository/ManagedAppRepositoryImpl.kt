package dev.gmarques.controledenotificacoes.data.repository

import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.ManagedAppMapper
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.validators.ManagedAppValidator
import dev.gmarques.controledenotificacoes.domain.repository.ManagedAppRepository
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 16:57.
 */
class ManagedAppRepositoryImpl @Inject constructor(private val managedAppDao: ManagedAppDao) : ManagedAppRepository {

    override suspend fun addManagedAppOrThrow(managedApp: ManagedApp) {
        ManagedAppValidator.validate(managedApp)
        managedAppDao.insertManagedAppOrThrow(ManagedAppMapper.mapToEntity(managedApp))
    }

    override suspend fun updateManagedAppOrThrow(managedApp: ManagedApp) {
        ManagedAppValidator.validate(managedApp)
        managedAppDao.updateManagedAppOrThrow(ManagedAppMapper.mapToEntity(managedApp))
    }

    override suspend fun removeManagedApp(managedApp: ManagedApp) {
        managedAppDao.deleteManagedApp(ManagedAppMapper.mapToEntity(managedApp))
    }

    override suspend fun getManagedAppById(id: String): ManagedApp? {
        return managedAppDao.getManagedAppByPackageId(id)?.let { ManagedAppMapper.mapToModel(it) }
    }

    override suspend fun getAllManagedApps(): List<ManagedApp> {
        return managedAppDao.getAllManagedApps().map { ManagedAppMapper.mapToModel(it) }
    }
}
