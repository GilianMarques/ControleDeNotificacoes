import dev.gmarques.controledenotificacoes.data.local.room.entities.RuleEntity
import dev.gmarques.controledenotificacoes.data.local.room.mapper.RuleMapper
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.model.validators.RuleValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito

class RuleMapperTest {

    @Test
    fun `mapToEntity deve converter Rule para RuleEntity corretamente`() {

        val rule = Rule("1", "Teste", listOf(WeekDay.MONDAY), listOf(TimeInterval(8, 0, 12, 0)))
        val entity = RuleMapper.mapToEntity(rule)

        assertEquals(rule.id, entity.id)
        assertEquals(rule.name, entity.name)
        assertEquals("[\"MONDAY\"]", entity.days)
        assertEquals("[{\"startHour\":8,\"startMinute\":0,\"endHour\":12,\"endMinute\":0}]", entity.timeIntervals)
    }


    @Test
    fun `mapToModel deve converter RuleEntity para Rule corretamente`() {

        val entity =
            RuleEntity("1", "Teste", "[\"MONDAY\"]", "[{\"startHour\":8,\"startMinute\":0,\"endHour\":12,\"endMinute\":0}]")

        val rule = RuleMapper.mapToModel(entity)

        assertEquals(entity.id, rule.id)
        assertEquals(entity.name, rule.name)
        assertEquals(listOf(WeekDay.MONDAY), rule.days)
        assertEquals(listOf(TimeInterval(8, 0, 12, 0)), rule.timeIntervals)
    }

}
