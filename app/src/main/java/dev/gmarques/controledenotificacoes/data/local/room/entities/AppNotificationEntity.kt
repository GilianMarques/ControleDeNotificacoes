package dev.gmarques.controledenotificacoes.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    val packageId: String,
    val title: String,
    val content: String,
    @PrimaryKey
    val timestamp: Long,
)