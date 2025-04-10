package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.repository.RuleRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class GetRuleByIdUseCaseTest {

    private lateinit var useCase: GetRuleByIdUseCase
    private val repository: RuleRepository = mock()

    @BeforeEach
    fun setUp() {
        useCase = GetRuleByIdUseCase(repository)
    }

    @Test
    fun `dado um id, quando execute for chamado, então repositorio getRuleById deve ser invocado`() = runTest {
        val id = "radlkhjglç"

        useCase.execute(id)

        verify(repository).getRuleById(id)
    }
}
