package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveRuleUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ViewManagedAppViewModel @Inject constructor(
    private val observeRuleUseCase: ObserveRuleUseCase,
) : ViewModel() {
    private var initialized = false
    private lateinit var _managedAppFlow: MutableStateFlow<ManagedAppWithRule>
    val managedAppFlow: StateFlow<ManagedAppWithRule> get() = _managedAppFlow


    fun setup(app: ManagedAppWithRule) {

        if (initialized) error("Não chame essa função mais que 1 vez")

        _managedAppFlow = MutableStateFlow(app)

        observeRuleChanges(app.rule)
    }

    /**
     * Quando o usuário usa o menu para editar uma regra o fragmento que adiciona e edita regras salva a modificação
     * no DB e este listener é disparado para atualizar a interface deste fragmento com a nova regra
     */
    private fun observeRuleChanges(rule: Rule) = viewModelScope.launch {
        observeRuleUseCase(rule.id)
            .collect {
                _managedAppFlow.tryEmit(_managedAppFlow.value.copy(rule = it))
            }
    }

}

