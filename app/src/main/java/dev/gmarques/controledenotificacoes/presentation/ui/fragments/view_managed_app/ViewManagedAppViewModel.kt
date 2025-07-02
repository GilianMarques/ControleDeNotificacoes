package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.DeleteRuleWithAppsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.DeleteAllAppNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.ObserveAppNotificationsByPkgIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppByPackageOrDefaultUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.DeleteManagedAppAndItsNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.ObserveManagedApp
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveRuleUseCase
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private val getInstalledAppIconUseCase: GetInstalledAppIconUseCase,
    private val getInstalledAppByPackageOrDefaultUseCase: GetInstalledAppByPackageOrDefaultUseCase,
    private val observeManagedApp: ObserveManagedApp,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private var initialized = false

    /**indica que o app gerenciado recebido não existe na lista de apps instalados. Veja [InstalledApp.NOT_FOUND_APP_PKG]*/
    var notFoundApp = false
        private set

    private val _managedAppFlow = MutableStateFlow<ManagedAppWithRule?>(null)
    val managedAppFlow: StateFlow<ManagedAppWithRule?> get() = _managedAppFlow

    private val _appNotificationHistoryFlow = MutableStateFlow<List<AppNotification>>(emptyList<AppNotification>())
    val appNotificationHistoryFlow: StateFlow<List<AppNotification>> get() = _appNotificationHistoryFlow

    private val _eventsFlow = MutableSharedFlow<Event>(replay = 1)
    val eventsFlow: SharedFlow<Event> get() = _eventsFlow

    private var ruleObserverJob: Job? = null

    fun setup(pkg: String) = viewModelScope.launch(IO) {

        val managedApp = getManagedAppByPackageIdUseCase(pkg)

        val ruleId = managedApp?.ruleId
        if (ruleId == null) return@launch

        val rule = getRuleByIdUseCase(ruleId)
        if (rule == null) return@launch

        val installedApp = getInstalledAppByPackageOrDefaultUseCase(pkg)

        setup(ManagedAppWithRule.from(installedApp, managedApp, rule))
    }

    fun setup(app: ManagedAppWithRule) = viewModelScope.launch(IO) {

        if (initialized == false) {
            initialized = true

            observeAppChanges(app.packageId)
            observeAppNotifications(app)
        }

        notFoundApp = app.uninstalled
        removeNotificationIndicator(app.packageId)
        _managedAppFlow.tryEmit(app)

    }

    /**
     * Necessário observar alterações  no app vindas do DB porque o usuario pode alterar a regra de um app
     * e se acontecer é necessário redefinir o listener que observa a regra para observar a nova regra do app
     */
    private fun observeAppChanges(pkg: String) = viewModelScope.launch(IO) {
        observeManagedApp(pkg).collect {
            Log.d("USUK", "ViewManagedAppViewModel.observeAppChanges: $it")

            if (it == null) _eventsFlow.tryEmit(Event.AppRemoved)
            else observeRuleChanges(it.ruleId)
        }
    }

    /**
     * Quando o usuário usa o menu para editar ou alterar uma regra,este listener é disparado para
     * atualizar a interface deste fragmento com a nova regra.
     *
     * Pode ser chamado multiplas vezes pois ecerra a corrotina anterior e cria uma nova.
     */
    private fun observeRuleChanges(ruleId: String) {
        ruleObserverJob?.cancel()
        ruleObserverJob = viewModelScope.launch(IO) {
            observeRuleUseCase(ruleId).collect {
                // a regra sera nula se o usuario a remover, nesse caso o fragmento será fechado
                // Log.d("USUK", "ViewManagedAppViewModel.observeRuleChanges: $it")
                it?.let { _managedAppFlow.emit(_managedAppFlow.value!!.copy(rule = it)) }
            }
        }
    }

    private fun observeAppNotifications(app: ManagedAppWithRule) = viewModelScope.launch(IO) {
        observeAppNotificationsByPkgIdUseCase(app.packageId).collect {

            val notifications = it.toMutableList()
                .apply {
                    reverse() // notificações mais recentes por cima
                }.distinctBy { it.title to it.content } // remove duplicatas (Impedir duplicatas de entrar no DB nao é viavel).

            _appNotificationHistoryFlow.tryEmit(notifications)
        }
    }

    /** Escreve no DB que o app ja nao tem notificações para serem vistas ja que o fragmento desse viewmodel exibe as notificações.
     * Deve ser chamado sempre que o fragmento for aberto e recriado pelo sistema
     */
    private fun removeNotificationIndicator(packageId: String) = viewModelScope.launch(IO) {
        delay(1000)// serve apenas pra nao me fazer pensar que tem um bug que faz os observadores do app e regra no DB dispararem duas vezes seguidas
        Log.d("USUK", "ViewManagedAppViewModel.removeNotificationIndicator: DB listeners will run, its not a bug!")
        getManagedAppByPackageIdUseCase(packageId)?.let { app ->
            updateManagedAppUseCase(app.copy(hasPendingNotifications = false))
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

    fun loadAppIcon(pkg: String, context: Context): Drawable = runBlocking {
        return@runBlocking getInstalledAppIconUseCase(pkg) ?: ContextCompat.getDrawable(context, R.drawable.vec_app)
    }

    /**
     * Atualiza a regra do app no DB.
     */
    fun updateAppsRule(newRule: Rule) = viewModelScope.launch {

        _managedAppFlow.value?.let {
            getManagedAppByPackageIdUseCase(it.packageId)?.let { app ->
                updateManagedAppUseCase(app.copy(ruleId = newRule.id))
                NotificationListener.instance?.reEvaluateActiveNotifications()
            }
        }
    }

}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class Event {
    object FinishWithSuccess : Event()
    object AppRemoved : Event()
}
