package dev.gmarques.controledenotificacoes.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.gmarques.controledenotificacoes.data.local.room.entities.ManagedAppEntity

@Dao
/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 16:45.
 */
interface ManagedAppDao {

    @Insert
    suspend fun insertManagedApp(managedAppEntity: ManagedAppEntity)

    @Update
    suspend fun updateManagedApp(managedAppEntity: ManagedAppEntity)

    @Delete
    suspend fun deleteManagedApp(managedAppEntity: ManagedAppEntity)

    @Query("SELECT * FROM managed_apps WHERE packageId = :id")
    suspend fun getManagedAppByPackageId(id: String): ManagedAppEntity?

    @Query("SELECT * FROM managed_apps")
    suspend fun getAllManagedApps(): List<ManagedAppEntity>
}
