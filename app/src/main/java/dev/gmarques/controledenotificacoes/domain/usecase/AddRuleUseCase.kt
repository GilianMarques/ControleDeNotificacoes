package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.repository.RuleRepository
import javax.inject.Inject
/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
class AddRuleUseCase @Inject constructor(private val repository: RuleRepository) {

    suspend operator fun invoke(rule: Rule) {
        repository.addRule(rule)
    }
}
