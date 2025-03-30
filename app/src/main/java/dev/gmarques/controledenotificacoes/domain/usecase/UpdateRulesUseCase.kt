package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.repository.RuleRepository
/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
class UpdateRuleUseCase(private val repository: RuleRepository) {
    suspend fun execute(ruleEntity: Rule) {
        repository.updateRule(ruleEntity)
    }
}
