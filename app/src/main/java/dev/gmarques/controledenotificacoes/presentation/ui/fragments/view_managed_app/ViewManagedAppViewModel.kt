package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.AddManagedAppUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject


@HiltViewModel
class ViewManagedAppViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addManagedAppUseCase: AddManagedAppUseCase,
) : ViewModel() {

    private lateinit var managedAppWithRule: ManagedAppWithRule

    private val _eventsFlow = MutableSharedFlow<ViewManagedAppsEvent>(replay = 1)
    val eventsFlow: SharedFlow<ViewManagedAppsEvent> get() = _eventsFlow


    fun setApp(app: ManagedAppWithRule) {
        this.managedAppWithRule = app
        _eventsFlow.tryEmit(ViewManagedAppsEvent.UpdateToolBar(app))
    }

}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class ViewManagedAppsEvent {
    data class UpdateToolBar(val app: ManagedAppWithRule) : ViewManagedAppsEvent()
}
