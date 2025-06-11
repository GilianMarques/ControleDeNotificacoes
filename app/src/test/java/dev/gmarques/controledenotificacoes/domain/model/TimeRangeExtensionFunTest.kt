package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.asRange
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startIntervalFormatted
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeRangeExtensionFunTest {

    @Test
    fun startIntervalFormatted_deve_retornar_horario_formatado_com_dois_digitos() {
        val timeRange = TimeRange(startHour = 8, startMinute = 5, endHour = 0, endMinute = 0)

        val resultado = timeRange.startIntervalFormatted()

        assertEquals("08:05", resultado)
    }

    @Test
    fun endIntervalFormatted_deve_retornar_horario_formatado_com_dois_digitos() {
        val timeRange = TimeRange(startHour = 0, startMinute = 0, endHour = 17, endMinute = 9)

        val resultado = timeRange.endIntervalFormatted()

        assertEquals("17:09", resultado)
    }

    @Test
    fun startInMinutes_deve_retornar_minutos_totais_do_horario_de_inicio() {
        val timeRange = TimeRange(startHour = 2, startMinute = 30, endHour = 0, endMinute = 0)

        val resultado = timeRange.startInMinutes()

        assertEquals(150, resultado)
    }

    @Test
    fun endInMinutes_deve_retornar_minutos_totais_do_horario_de_termino() {
        val timeRange = TimeRange(startHour = 0, startMinute = 0, endHour = 1, endMinute = 15)

        val resultado = timeRange.endInMinutes()

        assertEquals(75, resultado)
    }

    @Test
    fun asRange_deve_retornar_intervalo_com_inicio_e_fim_em_minutos() {
        val timeRange = TimeRange(startHour = 9, startMinute = 0, endHour = 10, endMinute = 30)

        val resultado = timeRange.asRange()

        assertEquals(540..630, resultado)
    }
}
