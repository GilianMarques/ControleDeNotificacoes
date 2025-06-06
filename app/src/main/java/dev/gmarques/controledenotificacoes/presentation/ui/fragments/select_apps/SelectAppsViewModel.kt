package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetAllInstalledAppsUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.model.SelectableApp
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.Event.BlockSelection
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.Event.NavigateHome
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.Event.SelectedAlreadyManagedApp
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.Event.SimpleErrorMessage
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.State.Idle
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.State.Loading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 16 de abril de 2025 as 15:35.
 */
@HiltViewModel
class SelectAppsViewModel @Inject constructor(
    private val getAllInstalledAppsUseCase: GetAllInstalledAppsUseCase,
    @ApplicationContext
    private val context: android.content.Context,
) : ViewModel() {

    companion object {
        const val MAX_APPS_PER_RULE = 999
    }

    private val _installedApps = MutableStateFlow<List<SelectableApp>>(listOf<SelectableApp>())
    val installedApps: StateFlow<List<SelectableApp>> = _installedApps

    private val _eventsFlow = MutableSharedFlow<Event>(replay = 1)
    val eventsFlow: SharedFlow<Event> get() = _eventsFlow

    private val _statesFlow = MutableStateFlow<State>(Loading)
    val statesFlow: StateFlow<State> get() = _statesFlow

    private var selectedApps = HashSet<InstalledApp>()
    var preSelectedAppsToHide: HashSet<String> = hashSetOf()


    val onAppCheckedMutex = Mutex()


    fun searchApps() = viewModelScope.launch(IO) {

        _statesFlow.tryEmit(Loading)
        _installedApps.tryEmit(emptyList())

        val installedApps = getAllInstalledAppsUseCase(
            excludePackages = preSelectedAppsToHide,
        ).map { installedApp ->
            SelectableApp(installedApp, selectedApps.any { it.packageId == installedApp.packageId })
        }

        selectedApps = selectedApps.filter { selectedApp ->
            installedApps.any { selectedApp.packageId == it.installedApp.packageId }
        }.toHashSet()

        _statesFlow.tryEmit(Idle)
        _installedApps.tryEmit(installedApps)

    }


    /**
     * Atualiza o estado de seleção de um aplicativo e notifica a UI sobre a mudança
     * e sobre o estado de bloqueio de seleção.
     * Garante que apenas um aplicativo seja selecionado/desselecionado por vez.
     */
    fun onAppChecked(app: SelectableApp, checked: Boolean) = viewModelScope.launch(Dispatchers.Default) {
        onAppCheckedMutex.withLock {

            if (checked && shouldBlockSelection()) {
                notifyErrorMessage(
                    context.getString(
                        R.string.Nao_possivel_selecionar_mais_que_x_aplicativos,
                        MAX_APPS_PER_RULE
                    )
                )

                return@launch
            }

            if (checked && app.installedApp.isBeingManaged) {
                _eventsFlow.tryEmit(SelectedAlreadyManagedApp)
            }

            val apps = installedApps.value.toMutableList()

            val index = apps.indexOfFirst { it.installedApp.packageId == app.installedApp.packageId }
            apps[index] = app.copy(isSelected = checked)
            _installedApps.tryEmit(apps.toList())

            selectedApps.apply {
                if (checked) add(app.installedApp)
                else remove(app.installedApp)
            }

            _eventsFlow.tryEmit(BlockSelection(shouldBlockSelection()))

        }
    }

    /**
     * Notifica a UI com uma mensagem de erro simples.
     */
    private fun notifyErrorMessage(msg: String) {
        _eventsFlow.tryEmit(SimpleErrorMessage(msg))
    }

    fun shouldBlockSelection(): Boolean {
        return (selectedApps.size + preSelectedAppsToHide.size) >= MAX_APPS_PER_RULE
    }

    fun validateSelection() = viewModelScope.launch(IO) {

        if (selectedApps.isEmpty()) {
            notifyErrorMessage(context.getString(R.string.Selecione_pelo_menos_um_aplicativo))
            return@launch
        }

        _eventsFlow.tryEmit(NavigateHome(selectedApps.toList()))
    }

    fun selectAppsAllOrNone(all: Boolean) = viewModelScope.launch(IO) {
        for (app in installedApps.value) {
            onAppChecked(app, all)
            if (all && shouldBlockSelection()) break
        }
    }

    fun invertSelection() = viewModelScope.launch(IO) {

        val apps = installedApps.value.toList()

        for (app in apps) {
            onAppChecked(app, !app.isSelected)
        }

    }

    fun toggleIncludeSystemApps() = viewModelScope.launch {

        with(PreferencesImpl.prefIncludeSystemApps) {
            invoke(value.not())
        }

        searchApps()
    }

    fun toggleIncludeManagedApps() = viewModelScope.launch {

        with(PreferencesImpl.prefIncludeManagedApps) {
            invoke(value.not())
        }
        searchApps()
    }

}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class Event {
    data class BlockSelection(val block: Boolean) : Event()
    data class SimpleErrorMessage(val error: String) : Event()
    object SelectedAlreadyManagedApp : Event()
    data class NavigateHome(val apps: List<InstalledApp>) : Event()
}

/**
 * Representa os estados da interface
 */
sealed class State {
    object Idle : State()
    object Loading : State()
}
