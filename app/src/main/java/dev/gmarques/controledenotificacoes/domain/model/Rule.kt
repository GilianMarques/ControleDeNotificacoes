package dev.gmarques.controledenotificacoes.domain.model

import androidx.annotation.Keep
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import java.io.Serializable
import java.util.UUID

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 *
 * Obtenha uma descrição legível dessa regra usando [dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleDescriptionUseCase] caso o nome esteja vazio
 *
 */

@Keep
data class Rule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val days: List<WeekDay>,
    val timeRanges: List<TimeRange>,
    val condition: Condition?,
    val ruleType: RuleType = RuleType.RESTRICTIVE,
) : Serializable
