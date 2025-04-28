package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.GetAllInstalledAppsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.ObserveAllManagedApps
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveAllRulesUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel responsável por gerenciar o estado da lista de aplicativos controlados.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeAllRulesUseCase: ObserveAllRulesUseCase,
    observeAllManagedApps: ObserveAllManagedApps,
    private val getAllInstalledAppsUseCase: GetAllInstalledAppsUseCase,
    @ApplicationContext context: Context,
) : ViewModel() {

    private val defaultAppIfInstalledOneWasUninstalled by lazy {
        InstalledApp(
            name = context.getString(R.string.App_nao_encontrado),
            packageId = "not.found.app",
            icon = ContextCompat.getDrawable(context, R.drawable.vec_remove)!!
        )
    }

    private val installedApps = MutableStateFlow<HashMap<String, InstalledApp>?>(null)
    private val rules = observeAllRulesUseCase().init(null)
    private val managedApps = observeAllManagedApps().init(null)
    val managedAppsWithRules = combine(rules, managedApps, installedApps, ::combineFlows).init(null)

    init {
        viewModelScope.launch {
            loadInstalledAppsInCache()
        }
    }


    private suspend fun loadInstalledAppsInCache() = withContext(IO) {
        installedApps.value = HashMap(getAllInstalledAppsUseCase().associateBy { it.packageId })
    }


    /**
     * Essa função existe para que seja feita a delegação da parte de combinar valores do flow, uma vez que o código de inicialização
     * do [managedAppsWithRules] estava muito Comprida
     *
     * Inicializa o [managedAppsWithRules] flow
     *
     * Combina a lista de regras com a lista de aplicativos gerenciados + os aplicativos instalados no dispositivo
     * ou armazenados em cache na memoria para criar uma lista de [ManagedAppWithRule].
     *
     * @param rules Lista de regras.
     * @param managedApps Lista de aplicativos gerenciados.
     * @return Lista de [ManagedAppWithRule].
     */
    private fun combineFlows(
        rules: List<Rule>?,
        managedApps: List<ManagedApp>?,
        installedAppsCache: HashMap<String, InstalledApp>?,
    ): List<ManagedAppWithRule>? {

        if (rules == null || managedApps == null || installedAppsCache == null) return null

        val rulesMap = rules.associateBy { it.id }

        return managedApps.map { managedApp ->

            val installedApp = installedAppsCache[managedApp.packageId] ?: defaultAppIfInstalledOneWasUninstalled

            ManagedAppWithRule(
                name = installedApp.name,
                packageId = installedApp.packageId,
                icon = installedApp.icon,
                rule = rulesMap[managedApp.ruleId]
                    ?: error("Todo aplicativo gerenciado deve ter uma regra relacionada. Verifique se não existe um bug na hora de buscar a regra ou se a regra foi removida e por algum motivo o aplicativo gerenciado não foi removido junto.")
            )

        }.sortedBy { it.name }

    }


    /**
     * Essa função evita boilerplate code e garante a conformidade com o DRY neste viewmodel
     *
     * Transforma um [Flow] qualquer em um [StateFlow] que vai emitir valores enquanto o ciclo de vida
     * da ViewModel estiver ativo.
     *
     * O [StateFlow] vai compartilhar o valor para novos coletores e manterá em memoria o ultimo
     * valor emitido.
     *
     */
    private fun <T> Flow<T>.init(initialValue: T): StateFlow<T> {
        return this.stateIn(
            scope = this@HomeViewModel.viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = initialValue
        )
    }

}


