package dev.gmarques.controledenotificacoes.domain.model.validators

import TimeRangeValidator
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleValidatorTest {

    @Test
    fun `ao passar nome valido a funcao validadora deve retornar sucesso`() {
        val result = RuleValidator.validateName("Minha Regra")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `ao passar nome vazio a funcao validadora deve retornar sucesso`() {
        val result = RuleValidator.validateName("")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `ao passar nome curto de mais a funcao validadora deve retornar falha`() {
        val shortName = "a".repeat(RuleValidator.MIN_NAME_LENGTH - 1)
        val result = RuleValidator.validateName(shortName)
        assertTrue(result.isFailure)
    }

    @Test
    fun `ao passar nome longo de mais a funcao validadora deve retornar falha`() {
        val nomeLongo = "a".repeat(RuleValidator.MAX_NAME_LENGTH + 1)
        val result = RuleValidator.validateName(nomeLongo)
        assertTrue(result.isFailure)
    }

    @Test
    fun `ao passar lista de dias valida a funcao validadora deve retornar sucesso`() {
        val dias = listOf(WeekDay.MONDAY, WeekDay.FRIDAY)
        val result = RuleValidator.validateDays(dias)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `ao passar um intervalo de dias muito longo a validacao deve falhar`() {
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
        val result = RuleValidator.validateDays(eightDaysInAWeek)
        assertTrue(result.isFailure)
    }

    @Test
    fun `ao passar lista de dias vazia a funcao validadora deve retornar falha`() {
        val result = RuleValidator.validateDays(emptyList())
        assertTrue(result.isFailure)
    }

    @Test
    fun `ao passar intervalos duplicados a funcao validadora deve retornar uma falha`() {
        val intervalosDuplicados = listOf(
            TimeRange(8, 0, 10, 0),
            TimeRange(8, 0, 10, 0)
        )
        val result = TimeRangeValidator.validateTimeRanges(intervalosDuplicados)
        assertTrue(result.isFailure)
    }

    @Test
    fun `ao passar intervalos de tempo que se intersecionam a  validacao deve falhar`() {

        val casosDeTeste = listOf(
            "intersecão no início" to listOf(TimeRange(12, 0, 18, 0), TimeRange(13, 0, 19, 0)),
            "intersecão no início (invertido)" to listOf(TimeRange(13, 0, 19, 0), TimeRange(12, 0, 18, 0)),
            "intersecão no fim" to listOf(TimeRange(12, 0, 18, 0), TimeRange(8, 0, 13, 0)),
            "intersecão no fim (invertido)" to listOf(TimeRange(8, 0, 13, 0), TimeRange(12, 0, 18, 0)),
            "intervalo dentro do outro" to listOf(TimeRange(8, 0, 18, 0), TimeRange(9, 0, 17, 0)),
            "intervalo dentro do outro (invertido)" to listOf(TimeRange(9, 0, 17, 0), TimeRange(8, 0, 18, 0)),
            "intersecão no início com varios intervalos" to listOf(
                TimeRange(4, 0, 5, 0),
                TimeRange(6, 0, 7, 0),
                TimeRange(8, 0, 9, 0),
                TimeRange(10, 0, 12, 0),
                TimeRange(11, 0, 13, 0)
            ),
        )

        casosDeTeste.forEach { (desc, list) ->
            val result = TimeRangeValidator.validateTimeRanges(list)
            assertTrue("Falhou no caso '$desc': $list", result.isFailure)
        }

    }

    @Test
    fun `ao passar intervalos validos e nao sobrepostos a funcao validadora deve retornar sucesso`() {
        val intervalosValidos = listOf(
            TimeRange(8, 0, 10, 0),
            TimeRange(10, 1, 12, 0)
        )
        val result = TimeRangeValidator.validateTimeRanges(intervalosValidos)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `ao validar uma regra completa com todos os dados corretos deve retornar sucesso`() {
        val rule = Rule(
            name = "Regra Válida",
            days = listOf(WeekDay.MONDAY, WeekDay.FRIDAY),
            timeRanges = listOf(TimeRange(8, 0, 12, 0)),
            condition = null
        )
        RuleValidator.validate(rule) // Se lancar excecão, o teste falha
    }

    @Test(expected = Exception::class)
    fun `ao validar uma regra com nome invalido deve lancar excecao`() {
        val rule = Rule(
            name = "a".repeat(RuleValidator.MAX_NAME_LENGTH + 1),
            days = listOf(WeekDay.MONDAY),
            condition = null,
            timeRanges = listOf(TimeRange(8, 0, 12, 0))
        )
        RuleValidator.validate(rule)
    }

    @Test(expected = Exception::class)
    fun `ao validar uma regra com dias invalidos deve lancar excecao`() {
        val rule = Rule(
            name = "Regra",
            days = emptyList(),
            condition = null,
            timeRanges = listOf(TimeRange(8, 0, 12, 0))
        )
        RuleValidator.validate(rule)
    }

    @Test(expected = Exception::class)
    fun `ao validar uma regra com intervalos duplicados deve lancar excecao`() {
        val rule = Rule(
            name = "Regra",
            days = listOf(WeekDay.MONDAY),
            condition = null,
            timeRanges = listOf(
                TimeRange(8, 0, 12, 0),
                TimeRange(8, 0, 12, 0)
            )
        )
        RuleValidator.validate(rule)
    }

    @Test(expected = Exception::class)
    fun `ao validar uma regra com intervalos que se interseccionam deve lancar excecao`() {
        val rule = Rule(
            name = "Regra",
            days = listOf(WeekDay.MONDAY),
            condition = null,
            timeRanges = listOf(
                TimeRange(8, 0, 12, 0),
                TimeRange(11, 0, 13, 0)
            )
        )
        RuleValidator.validate(rule)
    }
}
