package dev.gmarques.controledenotificacoes.data.repository

import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.ManagedAppMapper
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.validators.ManagedAppValidator
import dev.gmarques.controledenotificacoes.domain.repository.ManagedAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 16:57.
 */
class ManagedAppRepositoryImpl @Inject constructor(private val managedAppDao: ManagedAppDao) : ManagedAppRepository {

    /**
     * Adiciona ou atualiza um aplicativo gerenciado no repositório.
     * Se o objeto já existir no DB ele será atualizado.
     * Lança exceção se o objeto falhar na validação, impedidno que um objeto invalido seja salvo.
     *
     * @param managedApp O aplicativo gerenciado a ser adicionado ou atualizado.
     */
    override suspend fun addOrUpdateManagedAppOrThrow(managedApp: ManagedApp) {
        ManagedAppValidator.validate(managedApp)
        managedAppDao.insertOrUpdateManagedApp(ManagedAppMapper.mapToEntity(managedApp))
    }

    override suspend fun updateManagedAppOrThrow(managedApp: ManagedApp) {
        ManagedAppValidator.validate(managedApp)
        managedAppDao.updateManagedApp(ManagedAppMapper.mapToEntity(managedApp))
    }

    override suspend fun deleteManagedAppById(packageId: String) {
        managedAppDao.deleteById(packageId)
    }

    override suspend fun getManagedAppById(id: String): ManagedApp? {
        return managedAppDao.getManagedAppByPackageId(id)?.let { ManagedAppMapper.mapToModel(it) }
    }

    override suspend fun deleteManagedAppsByRuleId(ruleId: String): Int {
        return managedAppDao.deleteManagedAppsByRuleId(ruleId)
    }

    override fun observeAllManagedApps(): Flow<List<ManagedApp>> {
        return managedAppDao.observeAllManagedApps().map { apps ->
            apps.map { ManagedAppMapper.mapToModel(it) }
        }
    }
}
