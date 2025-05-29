package dev.gmarques.controledenotificacoes.domain.data.repository

import dev.gmarques.controledenotificacoes.domain.model.Rule
import kotlinx.coroutines.flow.Flow

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
interface RuleRepository {
    suspend fun addRuleOrThrow(rule: Rule)
    suspend fun updateRuleOrThrow(rule: Rule)
    suspend fun deleteRule(rule: Rule)
    suspend fun getRuleById(id: String): Rule?
    suspend fun getAllRules(): List<Rule>
    fun observeAllRules(): Flow<List<Rule>>
    fun observeRule(id: String): Flow<Rule?>
}
