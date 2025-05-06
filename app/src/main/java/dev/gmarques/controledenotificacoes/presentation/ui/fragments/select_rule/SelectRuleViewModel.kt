package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.DeleteRuleWithAppsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveAllRulesUseCase
import dev.gmarques.controledenotificacoes.presentation.EventWrapper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 19 de abril de 2025 as 15:14.
 */
@HiltViewModel
class SelectRuleViewModel @Inject constructor(
    observeAllRulesUseCase: ObserveAllRulesUseCase,
    private val deleteRuleWithAppsUseCase: DeleteRuleWithAppsUseCase,
) : ViewModel() {

    private val _ruleRemovalResult: MutableLiveData<EventWrapper<Result<Unit>>> = MutableLiveData()
    val ruleRemovalResult: LiveData<EventWrapper<Result<Unit>>> = _ruleRemovalResult


    val rules: StateFlow<List<Rule>> = observeAllRulesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteRule(rule: Rule) = viewModelScope.launch(IO) {
        try {
            deleteRuleWithAppsUseCase(rule)
            _ruleRemovalResult.postValue(EventWrapper(Result.success(Unit)))
        } catch (e: Exception) {
            _ruleRemovalResult.postValue(EventWrapper(Result.failure(e)))
        }
    }
}


