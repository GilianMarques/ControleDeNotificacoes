import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.RuleMapper
import dev.gmarques.controledenotificacoes.data.repository.RuleRepositoryImpl
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
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
  val rule = Rule("1", "Teste", listOf(WeekDay.MONDAY), listOf(TimeInterval(8, 0, 12, 0)))

  repository.addRule(rule)

  verify(ruleDao).insertRule(RuleMapper.mapToEntity(rule))
 }

 @Test
 fun `updateRule deve chamar updateRule no dao`() = runBlocking {
  val rule = Rule("1", "Teste", listOf(WeekDay.MONDAY), listOf(TimeInterval(8, 0, 12, 0)))

  repository.updateRule(rule)

  verify(ruleDao).updateRule(RuleMapper.mapToEntity(rule))
 }

 @Test
 fun `removeRule deve chamar deleteRule no dao`() = runBlocking {
  val rule = Rule("1", "Teste", listOf(WeekDay.MONDAY), listOf(TimeInterval(8, 0, 12, 0)))

  repository.removeRule(rule)

  verify(ruleDao).deleteRule(RuleMapper.mapToEntity(rule))
 }

 @Test
 fun `getRuleById deve retornar regra convertida`() = runBlocking {
  val ruleId = "1"
  val ruleEntity = RuleMapper.mapToEntity(Rule(ruleId, "Teste", listOf(WeekDay.MONDAY), listOf(TimeInterval(8, 0, 12, 0))))
  `when`(ruleDao.getRuleById(ruleId)).thenReturn(ruleEntity)

  val result = repository.getRuleById(ruleId)

  assert(result != null)
  assert(result!!.id == ruleId)
 }

 @Test
 fun `getAllRules deve retornar lista de regras convertidas`() = runBlocking {
  val ruleEntityList = listOf(
   RuleMapper.mapToEntity(Rule("1", "Teste 1", listOf(WeekDay.MONDAY), listOf(TimeInterval(8, 0, 12, 0)))),
   RuleMapper.mapToEntity(Rule("2", "Teste 2", listOf(WeekDay.TUESDAY), listOf(TimeInterval(10, 0, 14, 0))))
  )
  `when`(ruleDao.getAllRules()).thenReturn(ruleEntityList)

  val result = repository.getAllRules()

  assert(result.size == ruleEntityList.size)
 }
}