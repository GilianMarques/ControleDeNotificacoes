package dev.gmarques.controledenotificacoes.domain.model.validators

import TimeRangeValidator
import TimeRangeValidator.TimeRangeValidatorException.InversedRangeException
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeRangeValidatorTest {

    @Test
    fun `deve retornar sucesso para intervalo valido`() {
        val timeRange = TimeRange(8, 0, 10, 30)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isSuccess)
        assertEquals(timeRange, result.getOrNull())
    }

    @Test
    fun `deve falhar se startHour estiver fora do intervalo`() {
        val timeRange = TimeRange(-1, 0, 10, 0)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TimeRangeValidator.TimeRangeValidatorException.HourOutOfRangeException)
    }

    @Test
    fun `deve falhar se endHour estiver fora do intervalo`() {
        val timeRange = TimeRange(8, 0, 24, 0)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TimeRangeValidator.TimeRangeValidatorException.HourOutOfRangeException)
    }

    @Test
    fun `deve falhar se startMinute estiver fora do intervalo`() {
        val timeRange = TimeRange(8, -5, 10, 0)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TimeRangeValidator.TimeRangeValidatorException.MinuteOutOfRangeException)
    }

    @Test
    fun `deve falhar se endMinute estiver fora do intervalo`() {
        val timeRange = TimeRange(8, 0, 10, 60)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TimeRangeValidator.TimeRangeValidatorException.MinuteOutOfRangeException)
    }

    @Test
    fun `deve falhar se intervalo for invertido`() {
        val timeRange = TimeRange(10, 0, 9, 30)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InversedRangeException)
    }

    @Test
    fun `deve falhar se intervalo for igual`() {
        val timeRange = TimeRange(10, 0, 10, 0)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InversedRangeException)
    }
}
