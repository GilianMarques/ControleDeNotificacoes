package dev.gmarques.controledenotificacoes.domain.exceptions

/**
 * Criado por Gilian Marques
 * Em domingo, 30 de março de 2025 as 15:00.
 */
class InversedRangeException(
    private val startIntervalMinutes: Int,
    private val endIntervalMinutes: Int,
) :
    Exception("O inicio do intervalo nao pode acontecer após o fim do mesmo startIntervalMinutes: $startIntervalMinutes endIntervalMinutes: $endIntervalMinutes")
