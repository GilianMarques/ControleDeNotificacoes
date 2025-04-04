package dev.gmarques.controledenotificacoes.domain.model.validators

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class RuleValidatorTest {

    @Test
    fun `ao passar um nome invalido a funcao validadora deve retornar um result com falha`() {

        val invalidNames = listOf(
            "a",
            "",
            "hdlmkohjfdlzdkjg~çdjkfgz´d~fijglkdjfgz~´dfjg~kdjkdjfgljflgkflgjflhgjz[]d´rpfgk]zd´~fkjhopdjhíodjfikhjkgjh54.6f54g65d4h.4d.fty4xd4tyh9seryt"
        )

        invalidNames.forEach {
            assertTrue { RuleValidator.validateName(it).isFailure }
        }
    }


    @Test
    fun `ao passar um nome valido a funcao validadora deve retornar um result com o nome formatado`() {

        val validName = " regra  1 "
        val formatedName = "Regra 1"

        assertEquals(formatedName, RuleValidator.validateName(validName).getOrNull()!!)
    }

    @Test
    fun `ao passar um intervalo de dias invalido a funcao validadora deve retornar um result com falha`() {

        // sem nenhum dia selecionado
        assertTrue(RuleValidator.validateDays(emptyList()).isFailure)

        val eightDaysInAWeek = listOf(
            WeekDay.SUNDAY,
            WeekDay.SUNDAY,
            WeekDay.SUNDAY,
            WeekDay.SUNDAY,
            WeekDay.SUNDAY,
            WeekDay.SUNDAY,
            WeekDay.SUNDAY,
            WeekDay.SUNDAY,
        )
        assertTrue(RuleValidator.validateDays(eightDaysInAWeek).isFailure)
    }

    @Test
    fun `ao passar um listab de intervalos fora do intervalo permitido a funcao validadora deve retornar um result com falha`() {

        assertTrue(RuleValidator.validateTimeIntervals(emptyList()).isFailure)

        val tooMuchIntervals = listOf(
            TimeInterval(1, 2, 3, 4),
            TimeInterval(1, 2, 3, 4),
            TimeInterval(1, 2, 3, 4),
            TimeInterval(1, 2, 3, 4),
            TimeInterval(1, 2, 3, 4),
            TimeInterval(1, 2, 3, 4),
            TimeInterval(1, 2, 3, 4),
            TimeInterval(1, 2, 3, 4),
            TimeInterval(1, 2, 3, 4),
            TimeInterval(1, 2, 3, 4),
            TimeInterval(1, 2, 3, 4),
        )

        assertTrue(RuleValidator.validateTimeIntervals(tooMuchIntervals).isFailure)
    }
}