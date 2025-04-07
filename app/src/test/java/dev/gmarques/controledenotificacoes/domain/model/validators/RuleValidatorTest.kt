package dev.gmarques.controledenotificacoes.domain.model.validators

import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

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
    fun `ao passar uma lista de intervalos de tempo fora do range permitido a funcao validadora deve retornar um result com falha`() {

        assertTrue(RuleValidator.validateTimeRanges(emptyList()).isFailure)

        val tooMuchIntervals = mutableListOf<TimeRange>()

        repeat(RuleValidator.MAX_RANGES + 1)
        { tooMuchIntervals.add(TimeRange(1, 2, 3, 4)) }

        assertTrue(RuleValidator.validateTimeRanges(tooMuchIntervals).isFailure)
    }


    @Test
    fun `ao passar um unico intervalo de tempo valido a funcao validadora deve retornar um ok`() {

        val intervals = mutableListOf<TimeRange>(
            TimeRange(1, 0, 10, 0),
        )

        assertTrue(RuleValidator.validateTimeRanges(intervals).isSuccess)
    }

    @Test
    fun `ao passar intervalos de tempo que se intersecionam a funcao de validacao deve retornar uma excecao`() {

        val casosDeTeste = listOf(
            "interseção no início" to listOf(TimeRange(12, 0, 18, 0), TimeRange(13, 0, 19, 0)),
            "interseção no início (invertido)" to listOf(TimeRange(13, 0, 19, 0), TimeRange(12, 0, 18, 0)),
            "interseção no fim" to listOf(TimeRange(12, 0, 18, 0), TimeRange(8, 0, 13, 0)),
            "interseção no fim (invertido)" to listOf(TimeRange(8, 0, 13, 0), TimeRange(12, 0, 18, 0)),
            "intervalo dentro do outro" to listOf(TimeRange(8, 0, 18, 0), TimeRange(9, 0, 17, 0)),
            "intervalo dentro do outro (invertido)" to listOf(TimeRange(9, 0, 17, 0), TimeRange(8, 0, 18, 0)),
            "interseção no início com varios intervalos" to listOf(
                TimeRange(4, 0, 5, 0),
                TimeRange(6, 0, 7, 0),
                TimeRange(8, 0, 9, 0),
                TimeRange(10, 0, 12, 0),
                TimeRange(11, 0, 13, 0)
            ),
        )

        casosDeTeste.forEach { (descricao, lista) ->
            val resultado = RuleValidator.validateTimeRanges(lista)
            assertTrue(resultado.isFailure, "Falhou no caso '$descricao': $lista")
        }

    }

    // TODO: testar intervalos repetios
    @Test
    fun `ao passar intervalos de tempo validos e sem interseccao a validacao deve ser bem sucedida`() {
        val timeRanges = listOf(
            TimeRange(8, 0, 10, 0),
            TimeRange(10, 30, 12, 0),
            TimeRange(13, 0, 15, 0)
        )

        val resultado = RuleValidator.validateTimeRanges(timeRanges)

        assertTrue(resultado.isSuccess, "Esperado sucesso na validação, mas falhou com: ${resultado.exceptionOrNull()}")
    }

}