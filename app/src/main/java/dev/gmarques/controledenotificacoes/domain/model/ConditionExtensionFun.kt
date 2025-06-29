package dev.gmarques.controledenotificacoes.domain.model

import android.content.Context
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.enums.ConditionType
import dev.gmarques.controledenotificacoes.domain.model.enums.NotificationField


/**
 * Criado por Gilian Marques
 * Em segunda-feira, 31 de março de 2025 as 23:20.
 *
 * Classe utilitária para adicionar funcionalidades ao [TimeRange]
 *
 */
object ConditionExtensionFun {


    fun Condition.description(context: Context): String {

        val maxKeywords = 3
        val hintBuilder = StringBuilder()

        hintBuilder.append(
            when (type) {
                ConditionType.ONLY_IF -> context.getString(R.string.apenas_se)
                ConditionType.EXCEPT -> context.getString(R.string.exceto_se)
            }
        )

        hintBuilder.append(
            when (field) {
                NotificationField.TITLE -> context.getString(R.string.o_t_tulo_da_notificacao_contiver)
                NotificationField.CONTENT -> context.getString(R.string.o_conte_do_da_notificacao_contiver)
                NotificationField.BOTH -> context.getString(R.string.o_t_tulo_ou_o_conte_do_da_notificacao_contiverem)
            }
        )

        if (keywords.size > maxKeywords) {
            keywords.take(maxKeywords).forEachIndexed { index, keyword ->
                hintBuilder.append(
                    when {
                        index + 1 < maxKeywords -> " \"$keyword\","
                        else -> " \"$keyword\"..."
                    }
                )
            }
        } else {
            keywords.forEachIndexed { index, keyword ->
                hintBuilder.append(
                    when {
                        index + 2 < keywords.size -> " \"$keyword\","
                        index + 1 < keywords.size -> " \"$keyword\""
                        keywords.size > 1 -> context.getString(R.string.ou, keyword)
                        else -> " \"$keyword\""
                    }
                )
            }
        }

        hintBuilder.append(if (keywords.isNotEmpty()) "," else " (*)")

        hintBuilder.append(
            if (caseSensitive) context.getString(R.string.considerando_letras_mai_sculas_e_min_sculas)
            else context.getString(R.string.independentemente_de_letras_mai_sculas_e_min_sculas)
        )

        return hintBuilder.toString().trim()
    }

    /**
     * Verifica se uma [AppNotification] satisfaz uma [Condition].
     *
     * A satisfação ocorre se o campo especificado na condição ([Condition.field])
     * da notificação contiver alguma das palavras-chave ([Condition.keywords]).
     * A verificação pode ser sensível a maiúsculas e minúsculas ([Condition.caseSensitive]).
     *
     * @param notification A notificação a ser verificada.
     * @return `true` se a notificação satisfizer a condição, `false` caso contrário.
     *
     * Criado por Gilian Marques
     * Em 29/06/2025 as 14:04
     */
    fun Condition.isSatisfiedBy(notification: AppNotification): Boolean {

        val textToCheck = when (field) {
            NotificationField.TITLE -> notification.title
            NotificationField.CONTENT -> notification.content
            NotificationField.BOTH -> "${notification.title} ${notification.content}"
        }

        val normalizedNotificationText = if (caseSensitive) textToCheck else textToCheck.lowercase()

        val keywords = if (caseSensitive) keywords
        else keywords.map { it.lowercase() }

        return keywords.any { keyword ->
            normalizedNotificationText.contains(keyword)
        }

    }
}