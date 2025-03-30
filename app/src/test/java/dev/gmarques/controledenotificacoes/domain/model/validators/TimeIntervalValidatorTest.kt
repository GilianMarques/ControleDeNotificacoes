package dev.gmarques.controledenotificacoes.domain.model.validators

import TimeIntervalValidator
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TimeIntervalValidatorTest {

    @Test
    fun `ao pasar um objeto invalido, o validador deve retornar um result com falha`() {

        //valores fora dos limites aceitos
        val invalidList = listOf(
            TimeInterval(24, 30, 20, 45),
            TimeInterval(8, 61, 20, 45),
            TimeInterval(8, 30, -1, 45),
            TimeInterval(8, 30, 20, 90),
        )

        invalidList.forEach {
            assertTrue { TimeIntervalValidator.validate(it).isFailure }
        }
    }

    @Test
    fun `ao pasar um intervalo invertido, o validador deve retornar um result com falha`() {

        val i = TimeInterval(8, 30, 7, 29)

        assertTrue { TimeIntervalValidator.validate(i).isFailure }
    }

    @Test
    fun `ao pasar um objeto valido, o validador deve retornar um result com sucesso`() {

        val i = TimeInterval(8, 30, 20, 45)

        assertTrue { TimeIntervalValidator.validate(i).isSuccess }
    }
}