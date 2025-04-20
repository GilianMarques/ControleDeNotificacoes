package dev.gmarques.controledenotificacoes.presentation.ui.select_rule_fragment

import dev.gmarques.controledenotificacoes.presentation.EventWrapper
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp

/**
 * Criado por Gilian Marques
 * Em sábado, 19 de abril de 2025 as 15:14.
 */
data class UiEvents(
    val cantSelectMoreApps: EventWrapper<String> = EventWrapper<String>(null),
    val navigateHomeEvent: EventWrapper<List<InstalledApp>> = EventWrapper<List<InstalledApp>>(null),
)
