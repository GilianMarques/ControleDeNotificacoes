package dev.gmarques.controledenotificacoes.presentation.rule_fragment

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 11 de abril de 2025 as 17:37.
 */
sealed class UiEvent {
    data class ErrorEvent(val message: String) : UiEvent()
}
