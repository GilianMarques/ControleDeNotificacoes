package dev.gmarques.controledenotificacoes.data.local.room.dao

import androidx.room.*
import dev.gmarques.controledenotificacoes.data.local.room.entities.AppNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: AppNotificationEntity)

    @Delete
    suspend fun delete(notification: AppNotificationEntity)

    @Query("SELECT * FROM app_notifications WHERE packageId = :pkg")
    suspend fun getByPkg(pkg: String): AppNotificationEntity?

    @Query("SELECT * FROM app_notifications")
    suspend fun getAll(): List<AppNotificationEntity>

    @Query("SELECT * FROM app_notifications WHERE packageId = :pkg")
    fun observeNotificationsByPkgId(pkg: String): Flow<List<AppNotificationEntity>>
}