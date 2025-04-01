package dev.gmarques.controledenotificacoes.domain.utils

import dev.gmarques.controledenotificacoes.domain.model.TimeInterval

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 31 de março de 2025 as 23:20.
 *
 * Classe utilitária para adcionar funcionalidades ao [TimeInterval]
 *
 */
object TimeIntervalExtensionFun {

    /**
     * Formata horas e minutos garantindo que sempre tenham dois dígitos (ex: 08:00).
     *
     * @return String no formato "HH:MM".
     */
    fun TimeInterval.startIntervalFormatted() = "%02d:%02d".format(this.startHour, this.startMinute)

    /**
     * Formata horas e minutos garantindo que sempre tenham dois dígitos (ex: 08:00).
     *
     * @return String no formato "HH:MM".
     */
    fun TimeInterval.endIntervalFormatted() = "%02d:%02d".format(this.endHour, this.endMinute)

}