package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_managed_apps

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmOnAppsRuleChangeUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppByPackageOrDefaultUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.AddManagedAppUseCase
import dev.gmarques.controledenotificacoes.presentation.EventWrapper
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.measureTime


@HiltViewModel
class AddManagedAppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addManagedAppUseCase: AddManagedAppUseCase,
    private val rescheduleAlarmOnAppsRuleChangeUseCase: RescheduleAlarmOnAppsRuleChangeUseCase,
    private val getInstalledAppByPackageOrDefaultUseCase: GetInstalledAppByPackageOrDefaultUseCase,

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

    fun setRule(rule: Rule?) = viewModelScope.launch(Main) {
        _selectedRule.value = rule
        rule?.let { PreferencesImpl.lastSelectedRule(rule.id) }
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
    fun deleteApp(app: InstalledApp) {
        _selectedApps.value = _selectedApps.value!!.toMutableMap().apply { remove(app.packageId) }
    }

    /**
     * Valida a seleção atual de aplicativos e regra.
     *
     * Esta função verifica se uma regra foi selecionada e se pelo menos um aplicativo foi selecionado.
     * Se a regra ou nenhum aplicativo foi selecionado, um erro correspondente é exibido ao usuário.
     * Caso contrário, adiciona os aplicativos selecionados à lista de aplicativos gerenciados
     * associados à regra selecionada.
     * Após a adição bem-sucedida, notifica os observadores para fechar o fragmento.
     *
     * Esta operação é realizada em um [viewModelScope] e no [Main] dispatcher para garantir
     * que as atualizações da UI ocorram na thread principal.
     */
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

        val x = measureTime {
            apps.map {
                async {
                    val managedApp = ManagedApp(it.packageId, rule.id, false)
                    addManagedApp(managedApp)
                    rescheduleAlarm(it, managedApp, rule)
                }
            }.awaitAll()
        }
        Log.d("USUK", "AddManagedAppsViewModel.validateSelection: Time: $x millis")
        _successCloseFragment.postValue(EventWrapper(Unit))

    }

    /**
     * Reagenda o alarme para um aplicativo, se necessário.
     *
     * Esta função verifica se o aplicativo instalado já está sendo gerenciado.
     * Se estiver, ela usa o [rescheduleAlarmOnAppsRuleChangeUseCase] para verificar se tem algum alarme agendado para mostrar a
     * notificação de relatorio de notificações recebidas durante bloqueio e reagendar esse alarme (se houver) considerando
     * a nova regra do aplicativo.
     *
     * @param installedApp O [InstalledApp] que está sendo verificado.
     * @param managedApp O [ManagedApp] representando o aplicativo com a nova regra. (é um objeto contendo as mesmas informações do [InstalledApp] só por conveniência)
     * @param rule A nova regra [Rule] que foi aplicada ao aplicativo.
     *
     */
    private suspend fun rescheduleAlarm(
        installedApp: InstalledApp,
        managedApp: ManagedApp,
        rule: Rule,
    ) {
        if (!installedApp.isBeingManaged) return
        rescheduleAlarmOnAppsRuleChangeUseCase(managedApp, rule)
    }

    /**
     * Adiciona um aplicativo gerenciado ao armazenamento de dados.
     *
     * @param app O objeto [ManagedApp] a ser adicionado.
     *
     * Esta função suspensa chama o [addManagedAppUseCase] para adicionar o aplicativo gerenciado.
     */
    private suspend fun addManagedApp(app: ManagedApp) {
        addManagedAppUseCase(app)
    }

    fun addSelectedAppByPkgId(pkgId: String) = viewModelScope.launch {
        val installedApp = getInstalledAppByPackageOrDefaultUseCase(pkgId)

        if (installedApp.uninstalled) {
            _showError.postValue(EventWrapper(context.getString(R.string.O_aplicativo_n_o_pode_ser_selecionado)))
            return@launch
        }

        addNewlySelectedApps(listOf(installedApp))
    }

}
