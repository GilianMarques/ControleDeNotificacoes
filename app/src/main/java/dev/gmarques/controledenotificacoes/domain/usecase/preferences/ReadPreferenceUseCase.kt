package dev.gmarques.controledenotificacoes.domain.usecase.preferences

import dev.gmarques.controledenotificacoes.domain.data.repository.PreferencesRepository
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 06 de maio de 2025 as 13:27.
 */
class ReadPreferenceUseCase @Inject constructor(private val repository: PreferencesRepository) {
    operator fun <T : Any> invoke(key: String, default: T): T = runBlocking { repository.read(key, default) }
}