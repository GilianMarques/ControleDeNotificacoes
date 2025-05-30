package dev.gmarques.controledenotificacoes.domain.model

import java.io.Serializable


/**
 * Criado por Gilian Marques
 * Em domingo, 13 de abril de 2025 as 15:49.
 *
 * Representa um aplicativo que está sob controle de uma regra específica.
 *
 * @param packageId O identificador do pacote do aplicativo instalado (ex: com.whatsapp).
 * @param ruleId O identificador único da regra que está sendo aplicada a esse aplicativo.
 */
data class ManagedApp(
    val packageId: String,
    val ruleId: String,
    val hasPendingNotifications: Boolean,
) : Serializable
