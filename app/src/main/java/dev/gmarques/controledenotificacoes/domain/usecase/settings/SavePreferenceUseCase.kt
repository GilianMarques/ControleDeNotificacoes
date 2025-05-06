package dev.gmarques.controledenotificacoes.domain.usecase.settings

import dev.gmarques.controledenotificacoes.domain.repository.PreferencesRepository
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 06 de maio de 2025 as 13:26.
 */
class SavePreferenceUseCase @Inject constructor(private val repository: PreferencesRepository) {
    suspend operator fun <T> invoke(key: String, value: T) = repository.save(key, value)
}