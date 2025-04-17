package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    fun setApps(apps: List<InstalledApp>) {
        _selectedApps.postValue(apps.associate { it.packageId to it })
    }

    fun setRule(rule: Rule) {
        _selectedRule.value = rule
    }

    fun clearRule() {
        _selectedRule.value = null
    }

    fun getSelectedPackages(): Array<String> {
        return selectedApps.value!!.values.map { it.packageId }.toTypedArray()
    }
}
