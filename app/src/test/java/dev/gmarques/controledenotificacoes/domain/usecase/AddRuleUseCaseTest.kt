package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.usecase.rules.AddRuleUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


class AddRuleUseCaseTest {

    private lateinit var useCase: AddRuleUseCase
    private val repository: RuleRepository = mock()

    @BeforeEach
    fun setUp() {
        useCase = AddRuleUseCase(repository) // Criamos a instância do UseCase antes de cada teste
    }

    @Test
    fun `dada uma regra valida, quando execute for chamado, então repositorio addRule deve ser invocado`() = runTest {

        val rule = Rule(
            name = "Regra Teste",
            days = listOf(WeekDay.FRIDAY),
            timeRanges = listOf(TimeRange(10, 30, 11, 35)),
            condition = null

        )

        useCase(rule)

        verify(repository).addRuleOrThrow(rule)
    }

}
