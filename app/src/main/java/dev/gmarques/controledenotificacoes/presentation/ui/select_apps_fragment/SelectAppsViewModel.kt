package dev.gmarques.controledenotificacoes.presentation.ui.select_apps_fragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.usecase.GetInstalledAppsUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 16 de abril de 2025 as 15:35.
 */
@HiltViewModel
class SelectAppsViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
) : ViewModel() {

    private val _apps = MutableLiveData<List<InstalledApp>>()
    val apps: LiveData<List<InstalledApp>> = _apps

    private var searchJob: Job? = null

    init {
        searchApps() // carrega tudo inicialmente
    }

    fun searchApps(query: String = "") {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val result = getInstalledAppsUseCase(query)
            _apps.postValue(result)
        }
    }

    fun onAppChecked(app: InstalledApp, checked: Boolean) {
        // Lógica de marcação pode ser mantida aqui ou delegada a outro use case
        Log.d("AppListViewModel", "App '${app.name}' checked: $checked")
    }
}
