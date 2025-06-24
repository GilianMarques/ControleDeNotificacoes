package dev.gmarques.controledenotificacoes.data.repository

import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.ManagedAppMapper
import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.ManagedAppValidator
import dev.gmarques.controledenotificacoes.domain.model.ManagedAppValidator.ManagedAppValidatorException.BlankRuleIdException
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ManagedAppRepositoryImplTest {

    @Mock
    private lateinit var dao: ManagedAppDao

    private lateinit var repository: ManagedAppRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = ManagedAppRepositoryImpl(dao)
    }

    @Test
    fun `ao adicionar app valido deve salvar com sucesso`() = runTest {
        val app = ManagedApp("com.app1", "rule1", hasPendingNotifications = false)
        val appEntity = ManagedAppMapper.mapToEntity(app)

        `when`(dao.insertOrUpdateManagedApp(appEntity)).thenAnswer { }

        repository.addOrUpdateManagedAppOrThrow(app)

        verify(dao).insertOrUpdateManagedApp(appEntity)
    }

    @Test(expected = ManagedAppValidator.ManagedAppValidatorException.BlankPackageIdException::class)
    fun `ao adicionar app com package id em branco deve lancar excecao`() = runTest {
        val app = ManagedApp("", "rule1", hasPendingNotifications = false)
        repository.addOrUpdateManagedAppOrThrow(app)
    }


    @Test(expected = BlankRuleIdException::class)
    fun `ao adicionar app com rule id em branco deve lancar excecao`() = runTest {
        val app = ManagedApp("dev.gmarques.app", "", hasPendingNotifications = false)
        repository.addOrUpdateManagedAppOrThrow(app)
    }

    @Test
    fun `ao atualizar app valido deve salvar alteracao`() = runTest {
        val app = ManagedApp("com.app2", "rule1", hasPendingNotifications = false)
        val appEntity = ManagedAppMapper.mapToEntity(app)

        val updatedApp = app.copy(ruleId = "rule2")
        val updatedAppEntity = ManagedAppMapper.mapToEntity(updatedApp)

        `when`(dao.insertOrUpdateManagedApp(appEntity)).thenAnswer { }
        `when`(dao.updateManagedApp(updatedAppEntity)).thenAnswer { }

        repository.addOrUpdateManagedAppOrThrow(app)
        repository.updateManagedAppOrThrow(updatedApp)

        verify(dao).updateManagedApp(updatedAppEntity)
    }

    @Test(expected = ManagedAppValidator.ManagedAppValidatorException.BlankPackageIdException::class)
    fun `ao atualizar app com package id em branco deve lancar excecao`() = runTest {
        val app = ManagedApp("", "ruleX", hasPendingNotifications = false)
        repository.updateManagedAppOrThrow(app)
    }

    @Test
    fun `ao remover app deve retornar nulo na busca por id`() = runTest {
        val app = ManagedApp("com.app3", "rule3", hasPendingNotifications = false)
        val appEntity = ManagedAppMapper.mapToEntity(app)

        `when`(dao.insertOrUpdateManagedApp(appEntity)).thenAnswer { }
        `when`(dao.deleteManagedAppsByRuleId(appEntity.ruleId)).thenAnswer { 1 }

        repository.addOrUpdateManagedAppOrThrow(app)
        repository.deleteManagedAppsByRuleId(app.ruleId)

        verify(dao).deleteManagedAppsByRuleId(appEntity.ruleId)

        `when`(dao.getManagedAppByPackageId("com.app3")).thenReturn(null)
        val resultado = repository.getManagedAppByPackageId("com.app3")
        assertNull(resultado)
    }

    @Test
    fun `ao buscar app por id existente deve retornar objeto correspondente`() = runTest {
        val app = ManagedApp("com.app4", "rule4", hasPendingNotifications = false)
        val appEntity = ManagedAppMapper.mapToEntity(app)

        `when`(dao.getManagedAppByPackageId("com.app4")).thenReturn(appEntity)

        val resultado = repository.getManagedAppByPackageId("com.app4")
        assertEquals(app, resultado)
    }

    @Test
    fun `ao buscar app por id inexistente deve retornar nulo`() = runTest {
        `when`(dao.getManagedAppByPackageId("inexistente")).thenReturn(null)

        val resultado = repository.getManagedAppByPackageId("inexistente")
        assertNull(resultado)
    }


}
