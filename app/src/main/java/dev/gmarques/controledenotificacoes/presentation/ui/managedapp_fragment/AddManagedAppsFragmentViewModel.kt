package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject
import kotlin.text.isEmpty


@HiltViewModel
class AddManagedAppsFragmentViewModel @Inject constructor(
    private val generateRuleNameUseCase: GenerateRuleNameUseCase,
) :
    ViewModel() {

    private val _selectedApps = MutableLiveData<Map<String, InstalledApp>>(emptyMap())
    val selectedApps: LiveData<Map<String, InstalledApp>> = _selectedApps

    private val _selectedRule = MutableLiveData<Rule?>()
    val selectedRule: LiveData<Rule?> = _selectedRule

    fun setSelectedApps(apps: List<InstalledApp>) {
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

    /**
     * Recupera o nome de uma [Rule] (Regra) fornecida.
     *
     * Se o nome da regra estiver vazio, ele gera um nome usando [generateRuleNameUseCase].
     * Caso contrário, retorna o nome existente da regra.
     *
     * @param rule O objeto [Rule] para o qual o nome deve ser obtido.
     * @return O nome da regra, seja o nome existente ou um gerado.
     */
    fun getRuleName(rule: Rule): String {
        return if (rule.name.isEmpty()) generateRuleNameUseCase(rule) else rule.name
    }

    /**
     * Remove um aplicativo da lista de aplicativos atualmente selecionados.
     *
     * A função recebe um objeto [InstalledApp] representando o aplicativo a ser removido.
     * Ela localiza o aplicativo na lista usando o [packageId] do aplicativo e o remove.
     * A remoção atualiza o [LiveData] `_selectedApps`, que notifica os observadores
     * sobre a mudança na lista de aplicativos selecionados.
     *
     * @param app O [InstalledApp] a ser removido da lista de aplicativos selecionados.
     */
    fun removeApp(app: InstalledApp) {
        _selectedApps.value = _selectedApps.value!!.toMutableMap().apply { remove(app.packageId) }
    }
}
