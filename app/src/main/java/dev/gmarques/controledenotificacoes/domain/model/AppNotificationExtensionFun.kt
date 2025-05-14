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


}