package dev.gmarques.controledenotificacoes.domain.exceptions

import dev.gmarques.controledenotificacoes.domain.model.TimeRange

/**
 * Criado por Gilian Marques
 * Em domingo, 06 de março de 2025 as 20:54.
 */
class IntersectedRangeException(
    private val range1: TimeRange,
    private val range2: TimeRange,
) :
    Exception("Os intervalos fazem interseção entre si, de forma que um intervalo se inicia e/ou se encerra dentro de outro intervalo." +
            "\n$range1 e \n$range2")
