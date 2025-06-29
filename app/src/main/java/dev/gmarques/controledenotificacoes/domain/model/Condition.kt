package dev.gmarques.controledenotificacoes.domain.model

import androidx.annotation.Keep
import dev.gmarques.controledenotificacoes.domain.model.enums.ConditionType
import dev.gmarques.controledenotificacoes.domain.model.enums.NotificationField
import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 20 de junho de 2025 as 15:03.
 */
@Keep
data class Condition(
    val type: ConditionType,
    val field: NotificationField,
    val keywords: List<String>,
    val caseSensitive: Boolean = false,
) : Serializable {

    companion object {
        const val SEPARATOR = ","
    }
}