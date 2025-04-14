package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.usecase.GetInstalledAppsUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject


@HiltViewModel
class AddManagedAppsFragmentViewModel @Inject constructor(
    val getInstalledAppsUseCase: GetInstalledAppsUseCase,
) : ViewModel() {

    fun updateSelectedApps(app: InstalledApp, checked: Boolean) {
        // TODO:
    }

}
