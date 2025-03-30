package dev.gmarques.controledenotificacoes.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao
import dev.gmarques.controledenotificacoes.data.local.room.entities.RuleEntity
import dev.gmarques.controledenotificacoes.domain.model.Rule

@Database(entities = [RuleEntity::class], version = 1)

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
abstract class RoomDatabase : RoomDatabase() {

    abstract fun ruleDao(): RuleDao

}

