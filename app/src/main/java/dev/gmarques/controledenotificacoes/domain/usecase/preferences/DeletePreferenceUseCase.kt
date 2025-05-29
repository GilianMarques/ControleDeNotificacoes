package dev.gmarques.controledenotificacoes.domain.usecase.preferences

import dev.gmarques.controledenotificacoes.domain.data.repository.PreferencesRepository
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 16 de maio de 2025 as 11:35.
 *
 * Remove a preferencia do arquivo, valor e chave.
 */
class DeletePreferenceUseCase @Inject constructor(private val repository: PreferencesRepository) {
    operator fun invoke(key: String) = runBlocking { repository.deletePreferenceByName(key) }
}