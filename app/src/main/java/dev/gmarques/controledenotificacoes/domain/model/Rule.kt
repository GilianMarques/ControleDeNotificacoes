package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import java.io.Serializable
import java.util.UUID

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 *
 * Obtenha uma descrição legível dessa regra usando [dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleNameUseCase] caso o nome esteja vazio
 *
 */

data class Rule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val days: List<WeekDay>,
    val timeRanges: List<TimeRange>,
    val ruleType: RuleType = RuleType.RESTRICTIVE,
) : Serializable {
    companion object {
        const val MAX_APPS_PER_RULE = 999 // TODO: validar isso no validator depois de implmentado
    }
}
