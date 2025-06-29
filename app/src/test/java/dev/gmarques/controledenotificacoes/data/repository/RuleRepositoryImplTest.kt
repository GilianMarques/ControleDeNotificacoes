import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.RuleMapper
import dev.gmarques.controledenotificacoes.data.repository.RuleRepositoryImpl
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.fail
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class RuleRepositoryImplTest {

    @Mock
    private lateinit var ruleDao: RuleDao
    private lateinit var repository: RuleRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = RuleRepositoryImpl(ruleDao)
    }

    @Test
    fun `addRule deve chamar insertRule no dao`() = runBlocking {
        val rule = Rule("1", "Teste", listOf(WeekDay.MONDAY), listOf(TimeRange(8, 0, 12, 0)), null)

        repository.addRuleOrThrow(rule)

        verify(ruleDao).insertRule(RuleMapper.mapToEntity(rule))
    }

    @Test
    fun `addRuleOrThrow deve lancar excecao ao passar um regra invalida`() = runBlocking {
        val rules = listOf(
            Rule("1", "Teste1", listOf(), listOf(TimeRange(8, 0, 12, 0)), null),
            Rule("1", "Teste2", listOf(WeekDay.TUESDAY), listOf(TimeRange(8, 0, 8, 0)), null),
            Rule("", "Teste3", listOf(WeekDay.TUESDAY), listOf(TimeRange(8, 0, 9, 0)), null),
        )

        rules.forEach { rule ->
            try {
                repository.addRuleOrThrow(rule)
                fail { "Objeto invalido entrou no DB. Chamada deveria ter lançado uma exceção. ${rule.name}" }
            } catch (_: Exception) {
                true
            }
        }

    }

    @Test
    fun `updateRuleOrThrow deve lancar excecao ao passar um regra invalida`() = runBlocking {
        val rules = listOf(
            Rule("1", "Teste1", listOf(), listOf(TimeRange(8, 0, 12, 0)), null),
            Rule("1", "Teste2", listOf(WeekDay.TUESDAY), listOf(TimeRange(8, 0, 8, 0)), null),
            Rule("", "Teste3", listOf(WeekDay.TUESDAY), listOf(TimeRange(8, 0, 9, 0)), null),
        )

        rules.forEach { rule ->
            try {
                repository.updateRuleOrThrow(rule)
                fail { "Objeto invalido entrou no DB. Chamada deveria ter lançado uma exceção. ${rule.name}" }
            } catch (_: Exception) {
                true
            }
        }

    }

    @Test
    fun `updateRule deve chamar updateRule no dao`() = runBlocking {
        val rule = Rule("1", "Teste", listOf(WeekDay.MONDAY), listOf(TimeRange(8, 0, 12, 0)), null)

        repository.updateRuleOrThrow(rule)

        verify(ruleDao).updateRule(RuleMapper.mapToEntity(rule))
    }

    @Test
    fun `removeRule deve chamar deleteRule no dao`() = runBlocking {
        val rule = Rule("1", "Teste", listOf(WeekDay.MONDAY), listOf(TimeRange(8, 0, 12, 0)), null)

        repository.deleteRule(rule)

        verify(ruleDao).deleteRule(RuleMapper.mapToEntity(rule))
    }

    @Test
    fun `getRuleById deve retornar regra convertida`() = runBlocking {
        val ruleId = "1"
        val ruleEntity =
            RuleMapper.mapToEntity(Rule(ruleId, "Teste", listOf(WeekDay.MONDAY), listOf(TimeRange(8, 0, 12, 0)), null))
        `when`(ruleDao.getRuleById(ruleId)).thenReturn(ruleEntity)

        val result = repository.getRuleById(ruleId)

        assert(result != null)
        assert(result!!.id == ruleId)
    }

    @Test
    fun `getAllRules deve retornar lista de regras convertidas`() = runBlocking {
        val ruleEntityList = listOf(
            RuleMapper.mapToEntity(Rule("1", "Teste 1", listOf(WeekDay.MONDAY), listOf(TimeRange(8, 0, 12, 0)), null)),
            RuleMapper.mapToEntity(Rule("2", "Teste 2", listOf(WeekDay.TUESDAY), listOf(TimeRange(10, 0, 14, 0)), null))
        )
        `when`(ruleDao.getAllRules()).thenReturn(ruleEntityList)

        val result = repository.getAllRules()

        assert(result.size == ruleEntityList.size)
    }
}