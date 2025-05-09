package dev.gmarques.controledenotificacoes.presentation.ui.fragments.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.Preferences
import dev.gmarques.controledenotificacoes.domain.usecase.settings.SavePreferenceUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 09 de maio de 2025 as 10:12.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(private val savePreferenceUseCase: SavePreferenceUseCase) : ViewModel() {


    private val _eventsFlow = MutableSharedFlow<Event>(replay = 1)
    val eventsFlow: SharedFlow<Event> get() = _eventsFlow


    fun resetHints() = viewModelScope.launch(IO) {
        Preferences::class.java.fields
            .forEach {
                if (!it.name.lowercase().startsWith("show_hint")) return@forEach
                savePreferenceUseCase(it.name, true)
            }
        _eventsFlow.tryEmit(Event.PreferencesCleaned)
    }
}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class Event {
    object PreferencesCleaned : Event()
}