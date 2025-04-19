package dev.gmarques.controledenotificacoes.presentation.ui.select_apps_fragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.GetInstalledAppsUseCase
import dev.gmarques.controledenotificacoes.presentation.EventWrapper
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.model.SelectableApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
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
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    @ApplicationContext private val context: android.content.Context,
) : ViewModel() {


    private val _installedApps = MutableLiveData<List<SelectableApp>>()
    val installedApps: LiveData<List<SelectableApp>> = _installedApps

    private val _blockUiSelection = MutableLiveData<Boolean>(false)
    val blockUiSelection: LiveData<Boolean> = _blockUiSelection

    private val _uiEvents = MutableLiveData(UiEvents())

    val uiEvents: LiveData<UiEvents> get() = _uiEvents

    private var initialized = false

    private val selectedApps = HashSet<InstalledApp>()

    var preSelectedAppsToHide: HashSet<String> = hashSetOf()

    val onAppCheckedMutex = Mutex()

    fun searchApps() = viewModelScope.launch(IO) {

        if (initialized) return@launch

        initialized = true

        val result = getInstalledAppsUseCase("", preSelectedAppsToHide).map {
            Log.d("USUK", "SelectAppsViewModel.searchApps: ${it.name}")
            SelectableApp(it)
        }
        _installedApps.postValue(result)
        _blockUiSelection.postValue(!canSelectMoreApps())

    }


    fun onAppChecked(app: SelectableApp, checked: Boolean) = viewModelScope.launch(Dispatchers.Default) {
        onAppCheckedMutex.withLock {

            if (checked && !canSelectMoreApps()) {
                notifyCantSelectMoreApps()
                return@launch
            }

            val safeList = _installedApps.value!!.toMutableList()
            val index = safeList.indexOfFirst { it.installedApp.packageId == app.installedApp.packageId }
            safeList[index] = app.copy(isSelected = checked)

            _installedApps.postValue(safeList.toList())

            selectedApps.apply {
                if (checked) add(app.installedApp)
                else remove(app.installedApp)
            }

            _blockUiSelection.postValue(!canSelectMoreApps())

        }
    }

    private fun notifyCantSelectMoreApps() {
        _uiEvents.postValue(
            _uiEvents.value!!.copy(
                cantSelectMoreApps = EventWrapper(
                    context.getString(
                        R.string.Nao_possivel_selecionar_mais_que_x_aplicativos,
                        Rule.MAX_APPS_PER_RULE
                    )
                )
            )
        )
    }

    fun canSelectMoreApps(): Boolean {
        return (selectedApps.size + preSelectedAppsToHide.size) < Rule.MAX_APPS_PER_RULE
    }

    fun validateSelection() = viewModelScope.launch(IO) {
        if (installedApps.value!!.isEmpty()) {

            _uiEvents.postValue(
                _uiEvents.value!!.copy(
                    cantSelectMoreApps = EventWrapper(
                        context.getString(
                            R.string.Selecione_pelo_menos_um_aplicativo
                        )
                    )
                )
            )

            return@launch
        }

        _uiEvents.postValue(
            _uiEvents.value!!.copy(
                navigateHomeEvent = EventWrapper(selectedApps.toList())
            )
        )
    }

    fun selectAppsAllOrNone(all: Boolean) = viewModelScope.launch(IO) {
        for (app in _installedApps.value!!) {
            delay(75) // serve pra garantir que vai dar tempo do livedata postar a atualizaçao. A soluçao definitiva nao vale a pena implementar nesse momento.
            onAppChecked(app, all)
            if (all && !canSelectMoreApps()) break
        }
    }

    fun invertSelection() = viewModelScope.launch(IO) {
        var notifyAboutLimit = false
        for (app in _installedApps.value!!) {
            delay(75) // serve pra garantir que vai dar tempo do livedata postar a atualizaçao. A soluçao definitiva nao vale a pena implementar nesse momento.

            val isAppGoingToBeChecked = !app.isSelected

            if (isAppGoingToBeChecked && !canSelectMoreApps()) {
                notifyAboutLimit = true
                continue
            }

            onAppChecked(app, isAppGoingToBeChecked)

        }
        if (notifyAboutLimit) notifyCantSelectMoreApps()
    }

}
