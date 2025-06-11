package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.bitmapId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.pendingIntentId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.timeFormatted
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AppNotificationExtensionFunTest {

    @Test
    fun quando_data_for_hoje_timeFormatted_deve_retornar_hora_e_minutos() {
        val timestamp = System.currentTimeMillis()
        val notification = AppNotification("com.app", "titulo", "conteudo", timestamp)

        val expected = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        val result = notification.timeFormatted()

        assertEquals(expected, result)
    }

    @Test
    fun quando_data_for_antiga_timeFormatted_deve_retornar_data_completa() {
        val oldTimestamp = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        val notification = AppNotification("com.app", "titulo", "conteudo", oldTimestamp)

        val expected = SimpleDateFormat("EEEE, dd/MM HH:mm", Locale.getDefault()).format(Date(oldTimestamp))
        val result = notification.timeFormatted()

        assertEquals(expected, result)
    }

    @Test
    fun pendingIntentId_deve_retornar_packageId_mais_timestamp() {
        val notification = AppNotification("com.app", "titulo", "conteudo", 123456789L)

        val result = notification.pendingIntentId()

        assertEquals("com.app_123456789", result)
    }

    @Test
    fun bitmapId_deve_gerar_id_com_caracteres_validos() {
        val notification = AppNotification(
            packageId = "com.exemplo.app",
            title = "Titulo Notificacao!",
            content = "Conteudo #1"
        )

        val result = notification.bitmapId()

        val expected = "comexemploappTituloNotificacaoConteudo1.png"
        assertEquals(expected, result)
    }
}
