package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.repository.RuleRepository
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
class GetAllRulesUseCase @Inject constructor(private val repository: RuleRepository) {
    suspend fun execute(): List<Rule> {
        return repository.getAllRules()
    }
}
