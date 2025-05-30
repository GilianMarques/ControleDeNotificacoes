package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.DeleteRuleWithAppsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.DeleteAllAppNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.ObserveAppNotificationsByPkgIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.DeleteManagedAppAndItsNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveRuleUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ViewManagedAppViewModel @Inject constructor(
    private val observeRuleUseCase: ObserveRuleUseCase,
    private val deleteManagedAppAndItsNotificationsUseCase: DeleteManagedAppAndItsNotificationsUseCase,
    private val deleteRuleWithAppsUseCase: DeleteRuleWithAppsUseCase,
    private val observeAppNotificationsByPkgIdUseCase: ObserveAppNotificationsByPkgIdUseCase,
    private val deleteAllAppNotificationsUseCase: DeleteAllAppNotificationsUseCase,
    private val getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val updateManagedAppUseCase: UpdateManagedAppUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private var initialized = false

    private val _managedAppFlow = MutableStateFlow<ManagedAppWithRule?>(null)
    val managedAppFlow: StateFlow<ManagedAppWithRule?> get() = _managedAppFlow

    private val _appNotificationHistoryFlow = MutableStateFlow<List<AppNotification>>(emptyList<AppNotification>())
    val appNotificationHistoryFlow: StateFlow<List<AppNotification>> get() = _appNotificationHistoryFlow

    private val _eventsFlow = MutableSharedFlow<Event>(replay = 1)
    val eventsFlow: SharedFlow<Event> get() = _eventsFlow

    fun setup(app: ManagedAppWithRule) = viewModelScope.launch(IO) {

        if (initialized) error("Não chame essa função mais que 1 vez")

        Log.d("USUK", "ViewManagedAppViewModel.setup: $app")

        removeNotificationIndicator(app.packageId)

        _managedAppFlow.tryEmit(app)

        observeAppNotificationsByPkgIdUseCase(app.packageId)
            .collect {
                _appNotificationHistoryFlow.tryEmit(it.toMutableList().apply { reverse() })
            }

        observeRuleChanges(app.rule)
        initialized = true
    }

    private fun removeNotificationIndicator(packageId: String) = viewModelScope.launch(IO) {
        getManagedAppByPackageIdUseCase(packageId)?.let { app ->
            updateManagedAppUseCase(app.copy(hasPendingNotifications = false))
        }
    }


    /**
     * Quando o usuário usa o menu para editar uma regra o fragmento que adiciona e edita regras salva a modificação
     * no DB e este listener é disparado para atualizar a interface deste fragmento com a nova regra
     */
    private fun observeRuleChanges(rule: Rule) = viewModelScope.launch {
        observeRuleUseCase(rule.id).collect {

            if (it == null) return@collect.also {
                Log.w(
                    "USUK",
                    "ViewManagedAppViewModel.".plus("observeRuleChanges() regra recebida é nula. Se a última operação feita foi uma remoção isso está certo senão pode ser resultado de um bug ")
                )
            }

            _managedAppFlow.tryEmit(_managedAppFlow.value!!.copy(rule = it))
        }
    }

    fun deleteApp() = viewModelScope.launch {
        deleteManagedAppAndItsNotificationsUseCase(_managedAppFlow.value!!.packageId)
        _eventsFlow.tryEmit(Event.FinishWithSuccess)
    }

    fun deleteRule() = viewModelScope.launch {
        deleteRuleWithAppsUseCase(_managedAppFlow.value!!.rule)
        _eventsFlow.tryEmit(Event.FinishWithSuccess)
    }

    fun clearHistory() = viewModelScope.launch {
        deleteAllAppNotificationsUseCase(managedAppFlow.value!!.packageId)
    }

    fun setup(pkg: String) = viewModelScope.launch {
        Log.d("USUK", "ViewManagedAppViewModel.setup: $pkg")

        val managedApp = getManagedAppByPackageIdUseCase(pkg)

        val ruleId = managedApp?.ruleId
        if (ruleId == null) return@launch

        val rule = getRuleByIdUseCase(ruleId)
        if (rule == null) return@launch

        val packageManager: PackageManager = context.packageManager
        val appInfo = packageManager.getApplicationInfo(pkg, PackageManager.GET_META_DATA)
        val appName = packageManager.getApplicationLabel(appInfo).toString()

        setup(ManagedAppWithRule(appName, pkg, rule, managedApp.hasPendingNotifications))
    }

}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class Event {
    object FinishWithSuccess : Event()
}
