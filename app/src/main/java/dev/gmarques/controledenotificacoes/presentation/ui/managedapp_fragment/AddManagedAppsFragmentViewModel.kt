package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.WithFragmentBindings
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject


@HiltViewModel
class AddManagedAppsFragmentViewModel @Inject constructor() : ViewModel() {

    private val _selectedApps = MutableLiveData<Map<String, InstalledApp>>(emptyMap())
    val selectedApps: LiveData<Map<String, InstalledApp>> = _selectedApps

    private val _selectedRule = MutableLiveData<Rule?>()
    val selectedRule: LiveData<Rule?> = _selectedRule

    fun addNewlySelectedApps(apps: List<InstalledApp>) {
        _selectedApps.value = _selectedApps.value!!.values.toMutableList().apply {
            addAll(apps)
        }.associate { it.packageId to it }
    }

    fun setRule(rule: Rule) {
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
}
