package dev.gmarques.controledenotificacoes.domain.usecase.preferences

import dev.gmarques.controledenotificacoes.domain.data.repository.PreferencesRepository
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 06 de maio de 2025 as 13:27.
 */
class ClearPreferencesUseCase @Inject constructor(private val repository: PreferencesRepository) {
    suspend operator fun invoke() = repository.clearAll()
}