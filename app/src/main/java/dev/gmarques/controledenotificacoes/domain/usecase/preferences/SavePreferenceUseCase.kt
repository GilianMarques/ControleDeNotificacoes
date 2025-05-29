package dev.gmarques.controledenotificacoes.domain.usecase.preferences

import dev.gmarques.controledenotificacoes.domain.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 06 de maio de 2025 as 13:26.
 */
class SavePreferenceUseCase @Inject constructor(private val repository: PreferencesRepository) {
    operator fun <T> invoke(key: String, value: T) = CoroutineScope(IO).launch { repository.save(key, value) }
}