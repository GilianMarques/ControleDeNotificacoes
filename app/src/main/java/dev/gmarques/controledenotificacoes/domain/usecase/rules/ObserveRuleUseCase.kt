package dev.gmarques.controledenotificacoes.domain.usecase.rules

import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.model.Rule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 02/05/2024
 * Observa a tabela de regras e emite atualizações sempre que houver mudanças em uma regra específica.
 *
 */
class ObserveRuleUseCase @Inject constructor(
    private val repository: RuleRepository,
) {

    /**
     * Observa a regra com o ID especificado e emite atualizações sempre que houver alterações no banco.
     */
    operator fun invoke(id: String): Flow<Rule?> {
        return repository.observeRule(id)
    }
}