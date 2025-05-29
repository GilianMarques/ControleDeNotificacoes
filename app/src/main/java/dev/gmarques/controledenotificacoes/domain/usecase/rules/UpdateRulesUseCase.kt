package dev.gmarques.controledenotificacoes.domain.usecase.rules

import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.model.Rule
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
class UpdateRuleUseCase @Inject constructor(private val repository: RuleRepository) {
    suspend operator fun invoke(rule: Rule) {
        repository.updateRuleOrThrow(rule)
    }
}
