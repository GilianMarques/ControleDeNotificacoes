package dev.gmarques.controledenotificacoes.data.repository

import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.ManagedAppMapper
import dev.gmarques.controledenotificacoes.domain.exceptions.BlankStringException
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.repository.ManagedAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        val app = ManagedApp("com.app1", "rule1")
        val appEntity = ManagedAppMapper.mapToEntity(app)

        `when`(dao.insertManagedAppOrThrow(appEntity)).thenAnswer { }

        repository.addManagedAppOrThrow(app)

        verify(dao).insertManagedAppOrThrow(appEntity)
    }

    @Test(expected = BlankStringException::class)
    fun `ao adicionar app com package id em branco deve lancar excecao`() = runTest {
        val app = ManagedApp("", "rule1")
        repository.addManagedAppOrThrow(app)
    }


    @Test(expected = BlankStringException::class)
    fun `ao adicionar app com rule id em branco deve lancar excecao`() = runTest {
        val app = ManagedApp("dev.gmarques.app", "")
        repository.addManagedAppOrThrow(app)
    }

    @Test
    fun `ao atualizar app valido deve salvar alteracao`() = runTest {
        val app = ManagedApp("com.app2", "rule1")
        val appEntity = ManagedAppMapper.mapToEntity(app)

        val updatedApp = app.copy(ruleId = "rule2")
        val updatedAppEntity = ManagedAppMapper.mapToEntity(updatedApp)

        `when`(dao.insertManagedAppOrThrow(appEntity)).thenAnswer { }
        `when`(dao.updateManagedAppOrThrow(updatedAppEntity)).thenAnswer { }

        repository.addManagedAppOrThrow(app)
        repository.updateManagedAppOrThrow(updatedApp)

        verify(dao).updateManagedAppOrThrow(updatedAppEntity)
    }

    @Test(expected = BlankStringException::class)
    fun `ao atualizar app com package id em branco deve lancar excecao`() = runTest {
        val app = ManagedApp("", "ruleX")
        repository.updateManagedAppOrThrow(app)
    }

    @Test
    fun `ao remover app deve retornar nulo na busca por id`() = runTest {
        val app = ManagedApp("com.app3", "rule3")
        val appEntity = ManagedAppMapper.mapToEntity(app)

        `when`(dao.insertManagedAppOrThrow(appEntity)).thenAnswer { }
        `when`(dao.deleteManagedApp(appEntity)).thenAnswer { }

        repository.addManagedAppOrThrow(app)
        repository.removeManagedApp(app)

        verify(dao).deleteManagedApp(appEntity)

        `when`(dao.getManagedAppByPackageId("com.app3")).thenReturn(null)
        val resultado = repository.getManagedAppById("com.app3")
        assertNull(resultado)
    }

    @Test
    fun `ao buscar app por id existente deve retornar objeto correspondente`() = runTest {
        val app = ManagedApp("com.app4", "rule4")
        val appEntity = ManagedAppMapper.mapToEntity(app)

        `when`(dao.getManagedAppByPackageId("com.app4")).thenReturn(appEntity)

        val resultado = repository.getManagedAppById("com.app4")
        assertEquals(app, resultado)
    }

    @Test
    fun `ao buscar app por id inexistente deve retornar nulo`() = runTest {
        `when`(dao.getManagedAppByPackageId("inexistente")).thenReturn(null)

        val resultado = repository.getManagedAppById("inexistente")
        assertNull(resultado)
    }

    @Test
    fun `ao buscar todos os apps deve retornar lista completa`() = runTest {
        val apps = listOf(
            ManagedApp("com.a", "ruleA"),
            ManagedApp("com.b", "ruleB"),
            ManagedApp("com.c", "ruleC")
        )
        val appEntities = apps.map { ManagedAppMapper.mapToEntity(it) }

        `when`(dao.getAllManagedApps()).thenReturn(appEntities)

        val resultado = repository.getAllManagedApps()

        assertEquals(3, resultado.size)
        assertTrue(resultado.containsAll(apps))
    }
}
