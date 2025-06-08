package dev.gmarques.controledenotificacoes.presentation.model

import androidx.annotation.Keep
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em sábado, 26 de abril de 2025 as 17:41.
 *
 * Use a função estática interna para instanciar essa classe ao inves de usar este construtor.
 */
@Keep
data class ManagedAppWithRule(
    val name: String,
    val packageId: String,
    val rule: Rule,
    val hasPendingNotifications: Boolean,
    val uninstalled: Boolean,
) : Serializable {

    companion object {

        fun from(installedApp: InstalledApp, managedApp: ManagedApp, rule: Rule): ManagedAppWithRule {
            return ManagedAppWithRule(
                name = installedApp.name,
                packageId = installedApp.packageId,
                rule = rule,
                hasPendingNotifications = managedApp.hasPendingNotifications,
                installedApp.uninstalled,
            )
        }

    }
}