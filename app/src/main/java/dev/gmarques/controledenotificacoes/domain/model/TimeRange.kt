package dev.gmarques.controledenotificacoes.domain.model

import java.util.UUID

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 21:15.
 */

data class TimeRange(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val id: String = UUID.randomUUID().toString(),
)
