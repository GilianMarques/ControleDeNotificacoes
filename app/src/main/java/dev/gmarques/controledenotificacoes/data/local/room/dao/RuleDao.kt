package dev.gmarques.controledenotificacoes.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.gmarques.controledenotificacoes.data.local.room.entities.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
interface RuleDao {

    @Insert
    suspend fun insertRule(ruleEntity: RuleEntity)

    @Update
    suspend fun updateRule(ruleEntity: RuleEntity)

    @Delete
    suspend fun deleteRule(ruleEntity: RuleEntity)

    @Query("SELECT * FROM rules WHERE id = :id")
    suspend fun getRuleById(id: String): RuleEntity?

    @Query("SELECT * FROM rules")
    suspend fun getAllRules(): List<RuleEntity>

    @Query("SELECT * FROM rules")
    fun observeAllRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE id = :id")
    fun observeRule(id: String): Flow<RuleEntity?>
}
