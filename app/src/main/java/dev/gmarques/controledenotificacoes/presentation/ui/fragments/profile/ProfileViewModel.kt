package dev.gmarques.controledenotificacoes.presentation.ui.fragments.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.SavePreferenceUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 09 de maio de 2025 as 10:12.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(private val savePreferenceUseCase: SavePreferenceUseCase) : ViewModel() {

    private val _eventsFlow = MutableSharedFlow<Event>(replay = 1)
    val eventsFlow: SharedFlow<Event> get() = _eventsFlow

}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class Event {
    class PreferencesCleaned(val success: Boolean) : Event()
}