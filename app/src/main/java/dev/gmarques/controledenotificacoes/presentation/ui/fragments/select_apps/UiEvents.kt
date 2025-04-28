package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps

import dev.gmarques.controledenotificacoes.presentation.EventWrapper
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 17 de abril de 2025 as 22:03
 */
data class UiEvents(
    val cantSelectMoreApps: EventWrapper<String> = EventWrapper<String>(null),
    val navigateHomeEvent: EventWrapper<List<InstalledApp>> = EventWrapper<List<InstalledApp>>(null),
)
