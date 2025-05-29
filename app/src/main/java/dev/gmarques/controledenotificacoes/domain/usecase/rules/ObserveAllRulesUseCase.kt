package dev.gmarques.controledenotificacoes.domain.usecase.rules

import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.model.Rule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em domingo, 20 de abril de 2025 as 19:24.
 * Observa a tabela de regras e emite atualizações sempre que houver mudanças.
 *
 */
class ObserveAllRulesUseCase @Inject constructor(
    private val repository: RuleRepository,
) {

    /**
     * Retorna um Flow que emite a lista de regras sempre que houver alterações no banco.
     */
    operator fun invoke(): Flow<List<Rule>> {
        return repository.observeAllRules()
    }
}