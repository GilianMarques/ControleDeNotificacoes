package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.usecase.rules.RemoveRuleUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class RemoveRuleUseCaseTest {

    private lateinit var useCase: RemoveRuleUseCase
    private val repository: RuleRepository = mock()

    @BeforeEach
    fun setUp() {
        useCase = RemoveRuleUseCase(repository)
    }

    @Test
    fun `dada uma regra, quando execute for chamado, então repositorio removeRule deve ser invocado`() = runTest {
        val rule = Rule(
            name = "Regra Teste",
            days = listOf(WeekDay.FRIDAY),
            timeRanges = listOf(TimeRange(10, 30, 11, 35))
        )
        useCase(rule)

        verify(repository).removeRule(rule)
    }
}
