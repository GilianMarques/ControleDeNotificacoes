package dev.gmarques.controledenotificacoes.presentation.rule_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay

class AddRuleViewModel : ViewModel() {

    private var state = UiState()

    private val _uiState = MutableLiveData(state)
    val uiState: LiveData<UiState> get() = _uiState

    fun updateRuleType(type: RuleType) {
        _uiState.value = _uiState.value?.copy(ruleType = type)
    }

    fun addTimeRange(range: TimeRange) {

        val update = state.timeRanges.toMutableMap().apply {
            this[range.id] = range
        }
        updateState(state.copy(timeRanges = update))
    }


    fun removeTimeRange(range: TimeRange) {
        val update = state.timeRanges.toMutableMap().apply {
            this.remove(range.id)
        }
        updateState(state.copy(timeRanges = update))
    }

    fun updateSelectedDays(days: List<WeekDay>) {
        updateState(state.copy(selectedDays = days))
    }

    fun updateRuleName(name: String) {
        updateState(state.copy(ruleName = name))
    }


    /**
     * Atualiza o estado interno e publica o novo estado no LiveData.
     *
     * Esta função é responsável por:
     * 1. Atualizar a variável privada `state` com o `newState` fornecido.
     * 2. Publicar o valor atualizado de `state` no LiveData `_uiState`, o que dispara notificações para os observadores sobre a mudança.
     *
     * Esta função deve ser chamada sempre que uma alteração no estado da UI (Interface do Usuário) ocorrer.
     *
     * **Observação sobre a decisão de projeto:**
     * Inicialmente, a intenção era realizar a atualização do `_uiState` diretamente no *setter* (definidor) da variável `state`.
     * No entanto, essa abordagem foi considerada uma **má prática**. Isso ocorre porque delegar a lógica de atualização do LiveData ao *setter* da variável `state` criaria uma **complexidade oculta**.
     * Ou seja, haveria um ponto de alteração de estado não explícito, o que poderia levar a **bugs difíceis de rastrear e corrigir no futuro**, já que a mudança do `_uiState` ocorreria de forma indireta toda vez que `state` fosse alterado.
     * Portanto, optamos por criar esta função `updateState` **explícita** para centralizar e tornar clara a lógica de atualização do estado e do LiveData.
     * Dessa forma, a responsabilidade de atualizar o `_uiState` fica bem definida e visível, melhorando a **manutenibilidade e a legibilidade do código**, prevenindo assim os potenciais problemas de complexidade oculta.
     *
     * @param newState O novo estado da UI a ser definido.
     */
    private fun updateState(newState: UiState) {
        state = newState
        _uiState.postValue(state)
    }

}

