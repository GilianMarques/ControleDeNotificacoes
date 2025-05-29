package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 26 de abril de 2025 as 18:46.
 */
class ObserveAllManagedApps @Inject constructor(
    private val repository: ManagedAppRepository,
) {
    operator fun invoke(): Flow<List<ManagedApp>> {
        return repository.observeAllManagedApps()
    }
}