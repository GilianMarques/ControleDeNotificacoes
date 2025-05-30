package dev.gmarques.controledenotificacoes.presentation.ui.fragments.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.domain.data.PreferenceProperty
import dev.gmarques.controledenotificacoes.domain.data.Preferences
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.SavePreferenceUseCase
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

        var errors = false


        val resettablePreferences = Preferences.Resettable::class.java.declaredMethods.toHashSet().map {
            it.name.removePrefix("get").replaceFirstChar { it.lowercase() }
        }

        PreferencesImpl::class.java.declaredFields
            .filter {
                it.name.removeSuffix("\$delegate") in resettablePreferences
            }
            .forEach { field ->
                field.isAccessible = true
                val lazyValue = field.get(PreferencesImpl)

                // Se for Lazy, acessa o valor real
                val value = if (lazyValue is Lazy<*>) lazyValue.value else lazyValue

                if (value is PreferenceProperty<*>) value.reset()
                else {
                    errors = true
                    Log.e("USUK", "ProfileViewModel.resetHints: unsupported type ${field.type}")
                }

            }

        _eventsFlow.tryEmit(Event.PreferencesCleaned(errors == false))
    }


}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class Event {
    class PreferencesCleaned(val success: Boolean) : Event()
}