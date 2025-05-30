package dev.gmarques.controledenotificacoes.presentation.model

import androidx.annotation.Keep
import dev.gmarques.controledenotificacoes.domain.model.Rule
import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em sábado, 26 de abril de 2025 as 17:41.
 */
@Keep
data class ManagedAppWithRule(
    val name: String,
    val packageId: String,
    val rule: Rule,
    val hasPendingNotifications: Boolean,
) : Serializable