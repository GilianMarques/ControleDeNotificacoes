package dev.gmarques.controledenotificacoes.domain.repository

import dev.gmarques.controledenotificacoes.domain.model.Rule

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
interface RuleRepository {
    suspend fun addRule(rule: Rule)
    suspend fun updateRule(rule: Rule)
    suspend fun removeRule(rule: Rule)
    suspend fun getRuleById(id: String): Rule?
    suspend fun getAllRules(): List<Rule>
}
