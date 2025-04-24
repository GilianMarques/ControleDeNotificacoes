package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.AddManagedAppUseCase
import dev.gmarques.controledenotificacoes.presentation.EventWrapper
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddManagedAppsFragmentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addManagedAppUseCase: AddManagedAppUseCase,
) : ViewModel() {

    private val _selectedApps = MutableLiveData<Map<String, InstalledApp>>(emptyMap())
    val selectedApps: LiveData<Map<String, InstalledApp>> = _selectedApps

    private val _selectedRule = MutableLiveData<Rule?>()
    val selectedRule: LiveData<Rule?> = _selectedRule

    private val _showError = MutableLiveData<EventWrapper<String>>()
    val showError: LiveData<EventWrapper<String>> = _showError

    private val _successCloseFragment = MutableLiveData<EventWrapper<Unit>>()
    val successCloseFragment: LiveData<EventWrapper<Unit>> = _successCloseFragment

    fun addNewlySelectedApps(apps: List<InstalledApp>) {
        _selectedApps.value = _selectedApps.value!!.values.toMutableList().apply {
            addAll(apps)
        }.associate { it.packageId to it }
    }

    fun setRule(rule: Rule) = viewModelScope.launch(Main) {
        _selectedRule.value = rule
    }


    fun getSelectedPackages(): Array<String> {
        return selectedApps.value!!.values.map { it.packageId }.toTypedArray()
    }


    /**
     * Remove um aplicativo da lista de aplicativos atualmente selecionados.
     *
     * A função recebe um objeto [InstalledApp] representando o aplicativo a ser removido.
     * Ela localiza o aplicativo na lista usando o packageId do aplicativo e o remove.
     * A remoção atualiza o [LiveData] `_selectedApps`, que notifica os observadores
     * sobre a mudança na lista de aplicativos selecionados.
     *
     * @param app O [InstalledApp] a ser removido da lista de aplicativos selecionados.
     */
    fun removeApp(app: InstalledApp) {
        _selectedApps.value = _selectedApps.value!!.toMutableMap().apply { remove(app.packageId) }
    }

    fun validateSelection() = viewModelScope.launch(Main) {

        val rule = _selectedRule.value
        val apps = _selectedApps.value!!.values.toList()

        if (rule == null) {
            _showError.postValue(EventWrapper(context.getString(R.string.Selecione_uma_regra)))
            return@launch
        }

        if (apps.isEmpty()) {
            _showError.postValue(EventWrapper(context.getString(R.string.Selecione_pelo_menos_um_aplicativo)))
            return@launch
        }

        apps.map {
            async { addManagedApp(ManagedApp(it.packageId, rule.id)) }
        }.awaitAll()

        _successCloseFragment.postValue(EventWrapper(Unit))

    }

    private suspend fun addManagedApp(app: ManagedApp) {
        addManagedAppUseCase(app)
    }
}
