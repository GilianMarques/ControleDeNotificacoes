package dev.gmarques.controledenotificacoes.data.repository

import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.entities.ManagedAppEntity
import dev.gmarques.controledenotificacoes.domain.exceptions.BlankStringException
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.repository.ManagedAppRepository


class ManagedAppRepositoryImplTest {

    private lateinit var dao: FakeManagedAppDao
    private lateinit var repositorio: ManagedAppRepository

    @Before
    fun setup() {
        dao = FakeManagedAppDao()
        repositorio = ManagedAppRepositoryImpl(dao)
    }

    @Test
    fun ao_adicionar_app_valido_deve_salvar_com_sucesso() = runTest {
        val app = ManagedApp("com.app1", "rule1")

        repositorio.addManagedApp(app)

        val resultado = repositorio.getManagedAppById("com.app1")
        assertEquals(app, resultado)
    }

    @Test(expected = BlankStringException::class)
    fun ao_adicionar_app_com_package_id_em_branco_deve_lancar_excecao() = runTest {
        val app = ManagedApp("", "rule1")
        repositorio.addManagedApp(app)
    }

    @Test
    fun ao_atualizar_app_valido_deve_salvar_alteracao() = runTest {
        val app = ManagedApp("com.app2", "rule1")
        repositorio.addManagedApp(app)

        val atualizado = app.copy(ruleId = "rule2")
        repositorio.updateManagedApp(atualizado)

        val resultado = repositorio.getManagedAppById("com.app2")
        assertEquals("rule2", resultado?.ruleId)
    }

    @Test(expected = BlankStringException::class)
    fun ao_atualizar_app_com_package_id_em_branco_deve_lancar_excecao() = runTest {
        val app = ManagedApp("", "ruleX")
        repositorio.updateManagedApp(app)
    }

    @Test
    fun ao_remover_app_deve_retornar_nulo_na_busca_por_id() = runTest {
        val app = ManagedApp("com.app3", "rule3")
        repositorio.addManagedApp(app)

        repositorio.removeManagedApp(app)

        val resultado = repositorio.getManagedAppById("com.app3")
        assertNull(resultado)
    }

    @Test
    fun ao_buscar_app_por_id_existente_deve_retornar_objeto_correspondente() = runTest {
        val app = ManagedApp("com.app4", "rule4")
        repositorio.addManagedApp(app)

        val resultado = repositorio.getManagedAppById("com.app4")
        assertEquals(app, resultado)
    }

    @Test
    fun ao_buscar_app_por_id_inexistente_deve_retornar_nulo() = runTest {
        val resultado = repositorio.getManagedAppById("inexistente")
        assertNull(resultado)
    }

    @Test
    fun ao_buscar_todos_os_apps_deve_retornar_lista_completa() = runTest {
        val apps = listOf(
            ManagedApp("com.a", "ruleA"), ManagedApp("com.b", "ruleB"), ManagedApp("com.c", "ruleC")
        )
        apps.forEach { repositorio.addManagedApp(it) }

        val resultado = repositorio.getAllManagedApps()

        assertEquals(3, resultado.size)
        assertTrue(resultado.containsAll(apps))
    }

    class FakeManagedAppDao : ManagedAppDao {

        private val banco = mutableMapOf<String, ManagedAppEntity>()

        override suspend fun insertManagedApp(managedAppEntity: ManagedAppEntity) {
            banco[managedAppEntity.packageId] = managedAppEntity
        }

        override suspend fun updateManagedApp(managedAppEntity: ManagedAppEntity) {
            banco[managedAppEntity.packageId] = managedAppEntity
        }

        override suspend fun deleteManagedApp(managedAppEntity: ManagedAppEntity) {
            banco.remove(managedAppEntity.packageId)
        }

        override suspend fun getManagedAppByPackageId(id: String): ManagedAppEntity? {
            return banco[id]
        }

        override suspend fun getAllManagedApps(): List<ManagedAppEntity> {
            return banco.values.toList()
        }
    }
}
