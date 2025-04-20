package dev.gmarques.controledenotificacoes.presentation.ui.select_rule_fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.ObserveRulesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 19 de abril de 2025 as 15:14.
 */
@HiltViewModel
class SelectRuleViewModel @Inject constructor(
    observeRulesUseCase: ObserveRulesUseCase,
    val generateRuleNameUseCase: GenerateRuleNameUseCase,
) : ViewModel() {

    val rules: StateFlow<List<Rule>> = observeRulesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
