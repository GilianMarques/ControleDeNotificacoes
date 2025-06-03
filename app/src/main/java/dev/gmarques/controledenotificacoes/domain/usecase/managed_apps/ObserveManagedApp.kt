package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 03 de junho de 2025 as 13:06.
 */
class ObserveManagedApp @Inject constructor(
    private val repository: ManagedAppRepository,
) {
    operator fun invoke(pkg: String): Flow<ManagedApp?> {
        return repository.observeManagedApp(pkg)
    }
}