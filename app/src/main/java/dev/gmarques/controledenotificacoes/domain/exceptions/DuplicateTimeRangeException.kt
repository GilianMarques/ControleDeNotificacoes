package dev.gmarques.controledenotificacoes.domain.exceptions

import dev.gmarques.controledenotificacoes.domain.model.TimeRange

/**
 * Criado por Gilian Marques
 * Em terça-feira, 08 de abril de 2025 as 22:24.
 */
class DuplicateTimeRangeException(
    private val interval: TimeRange,
    private val otherInterval: TimeRange,
) :
    Exception("Existem dois ou mais intervalos de tempo iguais na lista:\n$interval\n$otherInterval") {
}