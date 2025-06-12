package dev.gmarques.controledenotificacoes.presentation.ui.fragments.settings


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.domain.data.PreferenceProperty
import dev.gmarques.controledenotificacoes.domain.data.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 30 de maio de 2025 as 11:33.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _eventsFlow = MutableSharedFlow<SettingsEvent>(replay = 1)
    val eventsFlow: SharedFlow<SettingsEvent> get() = _eventsFlow

    fun resetHints() = viewModelScope.launch(Dispatchers.IO) {
        var errors = false

        val resettableDialogHintsPreferences = Preferences.ResettableDialogHints::class.java.declaredMethods.map {
            it.name.removePrefix("get").replaceFirstChar { c -> c.lowercase() }
        }

        PreferencesImpl::class.java.declaredFields.filter { it.name.removeSuffix("\$delegate") in resettableDialogHintsPreferences }
            .forEach { field ->
                field.isAccessible = true
                val lazyValue = field.get(PreferencesImpl)
                val value = if (lazyValue is Lazy<*>) lazyValue.value else lazyValue

                if (value is PreferenceProperty<*>) value.reset()
                else {
                    errors = true
                    Log.e("SettingsViewModel", "resetHints: unsupported type ${field.type}")
                }
            }

        _eventsFlow.tryEmit(SettingsEvent.PreferencesCleaned(!errors))
    }

    fun resetBatteryOptimization() {
        //PreferencesImpl.showWarningCardBatteryRestriction.reset()
        _eventsFlow.tryEmit(SettingsEvent.BatteryOptimizationWarningResetted)

    }
}

sealed class SettingsEvent {
    class PreferencesCleaned(val success: Boolean) : SettingsEvent()
    object BatteryOptimizationWarningResetted : SettingsEvent()
}
