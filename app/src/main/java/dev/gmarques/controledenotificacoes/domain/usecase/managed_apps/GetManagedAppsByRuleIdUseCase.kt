package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 19 de maio de 2025 as 13:18.
 *
 * Este usecase é usando em uma Transação do Room. Não crie corrotinas ou mude o escopo do contexto para que a
 * transação não perca o efeito.
 */
class GetManagedAppsByRuleIdUseCase @Inject constructor(private val repository: ManagedAppRepository) {
    suspend operator fun invoke(ruleId: String): List<ManagedApp?> {
        return repository.getManagedAppsByRuleId(ruleId)
    }
}
