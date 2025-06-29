package dev.gmarques.controledenotificacoes.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.gmarques.controledenotificacoes.data.local.room.dao.AppNotificationDao
import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao
import dev.gmarques.controledenotificacoes.data.local.room.entities.AppNotificationEntity
import dev.gmarques.controledenotificacoes.data.local.room.entities.ManagedAppEntity
import dev.gmarques.controledenotificacoes.data.local.room.entities.RuleEntity


/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */

@Database(entities = [RuleEntity::class, ManagedAppEntity::class, AppNotificationEntity::class], version = 3)
abstract class RoomDatabase : RoomDatabase() {

    abstract fun ruleDao(): RuleDao
    abstract fun managedAppDao(): ManagedAppDao
    abstract fun appNotificationDao(): AppNotificationDao

}

