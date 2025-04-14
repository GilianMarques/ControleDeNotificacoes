package dev.gmarques.controledenotificacoes.presentation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.usecase.AddRuleUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.GetAllRulesUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
class MainActivityViewModel @Inject constructor(
    private val addRuleUseCase: AddRuleUseCase,
    private val getAllRulesUseCase: GetAllRulesUseCase,

    ) : ViewModel() {


    fun testRuleOperations() {


        viewModelScope.launch {

            val rule = Rule(
                name = "Regra 1",
                days = listOf(WeekDay.MONDAY, WeekDay.WEDNESDAY),
                timeRanges = listOf(TimeRange(8, 0, 10, 0))
            )

            addRuleUseCase(rule)

            val rules = getAllRulesUseCase()
            rules.forEach { Log.d("USUK", "MainActivityViewModel.testRuleOperations: $it") }
        }


    }
}
