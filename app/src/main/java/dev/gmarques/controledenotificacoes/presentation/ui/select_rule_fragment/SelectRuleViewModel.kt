package dev.gmarques.controledenotificacoes.presentation.ui.select_rule_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.GetAllRulesUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 19 de abril de 2025 as 15:14.
 */
@HiltViewModel
class SelectRuleViewModel @Inject constructor(
    private val getAllRulesUseCase: GetAllRulesUseCase,
    val generateRuleNameUseCase: GenerateRuleNameUseCase,
) : ViewModel() {


    private val _rules = MutableLiveData<List<Rule>>(emptyList<Rule>())
    val rulesLd: LiveData<List<Rule>> = _rules


    init {
        loadRules()
    }

    private fun loadRules() = viewModelScope.launch(IO) {

        val loadedRules = getAllRulesUseCase()
        _rules.postValue(loadedRules)
    }
}
