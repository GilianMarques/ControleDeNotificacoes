package dev.gmarques.controledenotificacoes.data.local.room.mapper

import dev.gmarques.controledenotificacoes.data.local.room.entities.ManagedAppEntity
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import org.junit.Assert.assertEquals
import org.junit.Test

class ManagedAppMapperTest {

    @Test
    fun `deve mapear ManagedApp para ManagedAppEntity corretamente quando os dados forem validos`() {
        val model = ManagedApp(packageId = "com.exemplo.app", ruleId = "regra1", hasPendingNotifications = false)
        val entity = ManagedAppMapper.mapToEntity(model)
        assertEquals(model.packageId, entity.packageId)
        assertEquals(model.ruleId, entity.ruleId)
    }

    @Test
    fun `deve manter ruleId vazio ao mapear ManagedApp para ManagedAppEntity`() {
        val model = ManagedApp(packageId = "com.exemplo.app", ruleId = "", hasPendingNotifications = false)
        val entity = ManagedAppMapper.mapToEntity(model)
        assertEquals("", entity.ruleId)
    }

    @Test
    fun `deve manter packageId vazio ao mapear ManagedApp para ManagedAppEntity`() {
        val model = ManagedApp(packageId = "", ruleId = "regra1", hasPendingNotifications = false)
        val entity = ManagedAppMapper.mapToEntity(model)
        assertEquals("", entity.packageId)
    }

    @Test
    fun `deve mapear ManagedAppEntity para ManagedApp corretamente quando os dados forem validos`() {
        val entity = ManagedAppEntity(packageId = "com.exemplo.app", ruleId = "regra1", hasPendingNotifications = false)
        val model = ManagedAppMapper.mapToModel(entity)
        assertEquals(entity.packageId, model.packageId)
        assertEquals(entity.ruleId, model.ruleId)
    }

    @Test
    fun `deve manter ruleId vazio ao mapear ManagedAppEntity para ManagedApp`() {
        val entity = ManagedAppEntity(packageId = "com.exemplo.app", ruleId = "", hasPendingNotifications = false)
        val model = ManagedAppMapper.mapToModel(entity)
        assertEquals("", model.ruleId)
    }

    @Test
    fun `deve manter packageId vazio ao mapear ManagedAppEntity para ManagedApp`() {
        val entity = ManagedAppEntity(packageId = "", ruleId = "regra1", hasPendingNotifications = false)
        val model = ManagedAppMapper.mapToModel(entity)
        assertEquals("", model.packageId)
    }

    @Test
    fun `deve manter integridade dos dados ao mapear ManagedApp para ManagedAppEntity e de volta para ManagedApp`() {
        val original = ManagedApp(packageId = "com.exemplo.app", ruleId = "regra1", hasPendingNotifications = false)
        val entity = ManagedAppMapper.mapToEntity(original)
        val result = ManagedAppMapper.mapToModel(entity)
        assertEquals(original, result)
    }

    @Test
    fun `deve manter integridade dos dados ao mapear ManagedAppEntity para ManagedApp e de volta para ManagedAppEntity`() {
        val original = ManagedAppEntity(packageId = "com.exemplo.app", ruleId = "regra1", hasPendingNotifications = false)
        val model = ManagedAppMapper.mapToModel(original)
        val result = ManagedAppMapper.mapToEntity(model)
        assertEquals(original, result)
    }
}
