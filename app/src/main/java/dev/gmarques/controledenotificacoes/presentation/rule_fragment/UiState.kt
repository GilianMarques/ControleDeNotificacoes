package dev.gmarques.controledenotificacoes.presentation.rule_fragment

import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 02 de abril de 2025 as 21:51.
 *
 *
 * Obs: Estao é algo imutavel. As propriedades devem ser imutaveis.
 */
data class UiState(
    val ruleType: RuleType = RuleType.RESTRICTIVE,
    val timeIntervals: Map<String, TimeInterval> = emptyMap(),
    val selectedDays: List<WeekDay> = emptyList(),
    val ruleName: String? = null,
)