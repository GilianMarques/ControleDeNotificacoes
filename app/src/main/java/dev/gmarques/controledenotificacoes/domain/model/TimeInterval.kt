package dev.gmarques.controledenotificacoes.domain.model

import TimeIntervalValidator

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 21:15.
 */
data class TimeInterval(val startHour: Int, val startMinute: Int, val endHour: Int, val endMinute: Int) {
    init {
        TimeIntervalValidator.validate(this)
    }
}