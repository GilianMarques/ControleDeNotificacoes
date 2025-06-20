package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.model.enums.ConditionType
import dev.gmarques.controledenotificacoes.domain.model.enums.NotificationField

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 20 de junho de 2025 as 15:03.
 */
data class Condition(
    val type: ConditionType,
    val field: NotificationField,
    val values: List<String>,
    val caseSensitive: Boolean = false,
)