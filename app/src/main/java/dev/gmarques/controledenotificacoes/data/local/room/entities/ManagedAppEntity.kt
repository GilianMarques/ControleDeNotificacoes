package dev.gmarques.controledenotificacoes.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 17:17.
 */
@Entity(tableName = "managed_apps")
data class ManagedAppEntity(
    @PrimaryKey
    val packageId: String,
    val ruleId: String,
    val hasPendingNotifications: Boolean,
)

