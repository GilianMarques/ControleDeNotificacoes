import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startIntervalFormatted
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeRangeExtensionFunTest {

    @Test
    fun `start range formatted deve retornar hora e minuto com dois digitos`() {
        val casos = listOf(
            TimeRange(8, 0, 10, 30) to "08:00",
            TimeRange(12, 5, 14, 10) to "12:05",
            TimeRange(23, 59, 0, 0) to "23:59",
            TimeRange(0, 0, 6, 15) to "00:00",
            TimeRange(1, 9, 15, 30) to "01:09"
        )

        for ((range, expected) in casos) {
            assertEquals("Erro ao formatar ${range.startHour}:${range.startMinute}", expected, range.startIntervalFormatted())
        }
    }

    @Test
    fun `end range formatted deve retornar hora e minuto com dois digitos`() {
        val casos = listOf(
            TimeRange(8, 0, 10, 30) to "10:30",
            TimeRange(12, 5, 14, 10) to "14:10",
            TimeRange(23, 59, 0, 0) to "00:00",
            TimeRange(0, 0, 6, 15) to "06:15",
            TimeRange(1, 9, 15, 30) to "15:30"
        )

        for ((range, expected) in casos) {
            assertEquals("Erro ao formatar ${range.endHour}:${range.endMinute}", expected, range.endIntervalFormatted())
        }
    }
}
