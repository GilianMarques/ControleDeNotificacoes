package dev.gmarques.controledenotificacoes.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Criado por Gilian Marques
 * Em Quarta, 14 de maio de 2025 as 11:21.
 */
object AppNotificationExtensionFun {


    /**
     * Formata o timestamp da notificação em um formato legível para o usuário.
     * Se a notificação for de hoje, exibe apenas a hora e os minutos (HH:mm).
     * Caso contrário, exibe o dia da semana completo, a data (dia/mês) e a hora (EEEE, dd/MM HH:mm).
     */
    fun AppNotification.timeFormatted(): String {
        val now = Calendar.getInstance()
        val notificationTime = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        return if (now.get(Calendar.YEAR) == notificationTime.get(Calendar.YEAR)
            && now.get(Calendar.DAY_OF_YEAR) == notificationTime.get(Calendar.DAY_OF_YEAR)
        ) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        } else {
            SimpleDateFormat("EEEE, dd/MM HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
    }

    fun AppNotification.pendingIntentId(): String {
        return "${this.packageId}_${this.timestamp}"
    }

    /**
     * Cria uma id para os bitmaps armazenados em cache. Todos os caracteres exceto a-z, A-Z e 0-9 são removidos, incluindo espaços.
     * A estrutura da id contem o nome de pacote da aplicação  pra que seja possivel filtrar todos os bitmaps de um determinado app
     * e o uso do titulo e conteudo serve para impedir que uma determinada notificação salve um novo bitmap sempre que for atualizada.
     * A ideia é que se a notificação nao sofrer alteraçao no titulo e/ou conteudo o bitmap dela sobrescrevera o bitmap que ja existe
     * evitando um consumo desnecessario de armazenamento. Pense em um player de musica, toda vez que ocorre um play/pause a notificação
     * é  atualizada e um novo bitmap é gerado mesmo que o bitmap seja o mesmo ja que a musica nao mudou. Entendeu?
     * */
    fun AppNotification.bitmapId(): String {
        return "${this.packageId}_${this.title}_${this.content}"
            .replace(Regex("[^a-zA-Z0-9]"), "") +
                ".png"

    }

}