import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.utils.TimeIntervalExtensionFun.startIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.utils.TimeIntervalExtensionFun.endIntervalFormatted
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeIntervalExtensionFunTest {

    @Test
    fun `start interval formatted deve retornar hora e minuto com dois digitos`() {
        val casos = listOf(
            TimeInterval(8, 0, 10, 30) to "08:00",
            TimeInterval(12, 5, 14, 10) to "12:05",
            TimeInterval(23, 59, 0, 0) to "23:59",
            TimeInterval(0, 0, 6, 15) to "00:00",
            TimeInterval(1, 9, 15, 30) to "01:09"
        )

        for ((interval, expected) in casos) {
            assertEquals("Erro ao formatar ${interval.startHour}:${interval.startMinute}", expected, interval.startIntervalFormatted())
        }
    }

    @Test
    fun `end interval formatted deve retornar hora e minuto com dois digitos`() {
        val casos = listOf(
            TimeInterval(8, 0, 10, 30) to "10:30",
            TimeInterval(12, 5, 14, 10) to "14:10",
            TimeInterval(23, 59, 0, 0) to "00:00",
            TimeInterval(0, 0, 6, 15) to "06:15",
            TimeInterval(1, 9, 15, 30) to "15:30"
        )

        for ((interval, expected) in casos) {
            assertEquals("Erro ao formatar ${interval.endHour}:${interval.endMinute}", expected, interval.endIntervalFormatted())
        }
    }
}
