package dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.setup_flow

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetSmartWatchInstalledAppsUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsável por gerenciar o estado do processo de echo
 * e fornecer informações relevantes para a interface de usuário.
 *
 * Criado por Gilian Marques
 * Em 12/06/2025 as 14:29
 */
@HiltViewModel
class EchoFlowSharedViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSmartWatchInstalledAppsUseCase: GetSmartWatchInstalledAppsUseCase,
) : ViewModel() {

    private val _eventsFlow = MutableSharedFlow<EchoEvent>(replay = 1)
    val eventsFlow: SharedFlow<EchoEvent> get() = _eventsFlow

    private val _statesFlow = MutableStateFlow<EchoState>(EchoState.Idle)
    val statesFlow: StateFlow<EchoState> get() = _statesFlow


    fun loadSmartWatchApps() = viewModelScope.launch(IO) {
        val apps = getSmartWatchInstalledAppsUseCase()
        _statesFlow.tryEmit(EchoState.StepOne.SmartWatchApps(apps))
    }
}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class EchoEvent {
    object EchoOn : EchoEvent()
    object EchoOff : EchoEvent()
}

/**
 * Representa os estados da interface
 */
sealed class EchoState {
    object Idle : EchoState()
    sealed class StepOne : EchoState() {
        data class SmartWatchApps(val apps: List<InstalledApp>) : StepOne()
    }
}


