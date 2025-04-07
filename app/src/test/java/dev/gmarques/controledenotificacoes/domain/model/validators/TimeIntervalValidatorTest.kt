package dev.gmarques.controledenotificacoes.domain.model.validators

import TimeRangeValidator
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TimeRangeValidatorTest {

    @Test
    fun `ao pasar um objeto invalido, o validador deve retornar um result com falha`() {

        //valores fora dos limites aceitos
        val invalidList = listOf(
            TimeRange(24, 30, 20, 45),
            TimeRange(8, 61, 20, 45),
            TimeRange(8, 30, -1, 45),
            TimeRange(8, 30, 20, 90),
        )

        invalidList.forEach {
            assertTrue { TimeRangeValidator.validate(it).isFailure }
        }
    }

    @Test
    fun `ao pasar um intervalo invertido, o validador deve retornar um result com falha`() {

        val i = TimeRange(8, 30, 7, 29)

        assertTrue { TimeRangeValidator.validate(i).isFailure }
    }

    @Test
    fun `ao pasar um objeto valido, o validador deve retornar um result com sucesso`() {

        val i = TimeRange(8, 30, 20, 45)

        assertTrue { TimeRangeValidator.validate(i).isSuccess }
    }
}