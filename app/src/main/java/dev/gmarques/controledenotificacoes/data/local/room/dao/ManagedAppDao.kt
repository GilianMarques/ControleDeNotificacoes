package dev.gmarques.controledenotificacoes.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.gmarques.controledenotificacoes.data.local.room.entities.ManagedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 16:45.
 */
interface ManagedAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateManagedApp(managedAppEntity: ManagedAppEntity)

    @Update
    suspend fun updateManagedApp(managedAppEntity: ManagedAppEntity)

    @Query("DELETE FROM managed_apps WHERE packageId = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM managed_apps WHERE packageId = :id")
    suspend fun getManagedAppByPackageId(id: String): ManagedAppEntity?

    @Query("SELECT * FROM managed_apps")
    fun observeAllManagedApps(): Flow<List<ManagedAppEntity>>

    @Query("DELETE FROM managed_apps WHERE ruleId = :ruleId")
    fun deleteManagedAppsByRuleId(ruleId: String): Int

    @Query("SELECT * FROM managed_apps WHERE ruleId = :ruleId")
    suspend fun getManagedAppsByRuleId(ruleId: String): List<ManagedAppEntity?>


    @Query("SELECT * FROM managed_apps WHERE packageId = :pkg")
    fun observeManagedApp(pkg: String): Flow<ManagedAppEntity?>

}
