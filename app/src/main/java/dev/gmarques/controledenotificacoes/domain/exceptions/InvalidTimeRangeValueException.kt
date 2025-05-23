package dev.gmarques.controledenotificacoes.domain.exceptions

/**
 * Criado por Gilian Marques
 * Em Sexta-feira, 23 de maio de 2025 as 20:05.
 *
 * Usada para informar quando o valor de um [dev.gmarques.controledenotificacoes.domain.model.TimeRange]
 * está fora do Range adequado de horas 0-23 e minutos 0-59
 */
class InvalidTimeRangeValueException(
    val minLength: Int,
    val maxLength: Int,
    val actual: Int,
) : Exception("O hora ou minuto valido é de $minLength a $maxLength. valor atual: $actual")