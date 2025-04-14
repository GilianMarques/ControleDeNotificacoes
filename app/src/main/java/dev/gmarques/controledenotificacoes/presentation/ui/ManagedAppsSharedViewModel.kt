package dev.gmarques.controledenotificacoes.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 16 de abril de 2025 as 17:30.
 */
class ManagedAppsSharedViewModel : ViewModel() {

    private val _selectedApps = MutableLiveData<Map<String, InstalledApp>>()
    val selectedApps: LiveData<Map<String, InstalledApp>> = _selectedApps

    private val _selectedRule = MutableLiveData<Rule?>()
    val selectedRule: LiveData<Rule?> = _selectedRule

    fun setApps(apps: List<InstalledApp>) {

        _selectedApps.value = apps.associate { it.packageId to it }
    }

    fun setRule(rule: Rule) {
        _selectedRule.value = rule
    }

    fun clearRule() {
        _selectedRule.value = null
    }
}
