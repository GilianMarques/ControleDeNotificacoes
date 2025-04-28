package dev.gmarques.controledenotificacoes.presentation.model

import android.graphics.drawable.Drawable
import dev.gmarques.controledenotificacoes.domain.model.Rule

/**
 * Criado por Gilian Marques
 * Em sábado, 26 de abril de 2025 as 17:41.
 */
data class ManagedAppWithRule(
    val name: String,
    val packageId: String,
    val icon: Drawable,
    val rule: Rule,
)