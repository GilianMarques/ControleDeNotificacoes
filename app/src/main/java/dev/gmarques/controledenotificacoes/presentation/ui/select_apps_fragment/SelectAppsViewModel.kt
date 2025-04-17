package dev.gmarques.controledenotificacoes.presentation.ui.select_apps_fragment

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

    var selectedApps = listOf<InstalledApp>()
        private set

    var preSelectedPackages: HashSet<String> = hashSetOf()
    private var preSelectedPackagesIncludedInList = false


    private var searchJob: Job? = null

    fun searchApps(query: String = "") {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val result = getInstalledAppsUseCase(query, preSelectedPackages)
            if (!preSelectedPackagesIncludedInList && preSelectedPackages.isNotEmpty()) includePreSelectedPackages(result)

            _apps.postValue(result)
        }
    }

    /**
     * Essa função inclui na lista de aplicativos selecionados todos os aplicativos que constam na lista de pacotes pré-selecionados.
     * Pacotes pré selecionados são aplicativos que foram selecionados pelo usuário neste mesmo fragmento anteriormente e devem
     * ser incluídos na lista para não se perderem já que a lista de aplicativos selecionados neste fragmento, quando retornada
     * para o fragmento anterior substituirá a lista que está presente lá e se os pacotes pré selecionados não forem incluídos
     * nesta lista eles desapareceram
     * */
    private fun includePreSelectedPackages(apps: List<InstalledApp>) {
        apps.forEach {
            if (preSelectedPackages.contains(it.packageId)) onAppChecked(it, true)
        }
    }

    fun onAppChecked(app: InstalledApp, checked: Boolean) {
        selectedApps = selectedApps.toMutableList().apply {
            if (checked) add(app) else remove(app)
        }
    }
}
