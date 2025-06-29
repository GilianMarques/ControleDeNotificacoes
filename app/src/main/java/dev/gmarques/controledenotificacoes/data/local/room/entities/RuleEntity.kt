package dev.gmarques.controledenotificacoes.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val days: String,
    val timeRanges: String,
    val condition: String?,
    val ruleType: RuleType = RuleType.RESTRICTIVE,
)

