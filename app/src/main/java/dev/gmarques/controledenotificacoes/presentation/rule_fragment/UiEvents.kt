package dev.gmarques.controledenotificacoes.presentation.rule_fragment

import dev.gmarques.controledenotificacoes.presentation.EventWrapper

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 11 de abril de 2025 as 17:37.
 */
data class UiEvents(
    val simpleErrorMessageEvent: EventWrapper<String> = EventWrapper<String>(null),
    val nameErrorMessageEvent: EventWrapper<String> = EventWrapper<String>(null),
    val navigateHomeEvent: EventWrapper<Boolean> = EventWrapper<Boolean>(null),
    )
