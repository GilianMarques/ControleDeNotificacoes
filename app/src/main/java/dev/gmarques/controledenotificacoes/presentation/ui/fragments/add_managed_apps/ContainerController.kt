package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_managed_apps

import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.math.min

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 06 de junho de 2025 as 12:38.
 *
 * Controlador para gerenciar a adição e remoção de views em um ViewGroup. gerencia automaticamente
 * as views do container para que sempre reflitam o conteudo recebido, adicionando e removendo views conforme necessario,
 * parecido com o que um recyclerview faz, porem permitindo que a view que contem o container seja redimensionada sob demanda
 * sem sacrificar as animações de adição e remoção de views.
 * observando o ciclo de vida do componente proprietário para limpar recursos.
 *
 * ##### Deve ser uma instancia global Activity ou Fragmento em que será usado afim de evitar MemoryLeaks
 *
 * @property lifecycleOwner O LifecycleOwner cujo ciclo de vida este controlador observará.
 * @property container O ViewGroup onde as views filhas serão adicionadas ou removidas, defina 'animateLayoutChanges = true' no layout do container.
 * @property maxViews O número máximo de views que podem ser mantidas no container. O padrão é 99.
 */
class ContainerController(
    lifecycleOwner: LifecycleOwner,
    private val container: ViewGroup,
    private val maxViews: Int = 99,
) : DefaultLifecycleObserver {

    /**
     * Um HashMap para manter os objetos presentes na UI e seus respectivos holders, usando o id da Child como chave.
     */
    private val children = LinkedHashMap<String, Child>()
    private var lastObjectsList = emptyList<Child>()

    /**
     * Atualiza a lista de views no container.
     * Remove views que não estão na nova lista, adiciona novas views e garante que o número máximo de views não seja excedido.
     * @param objs A nova lista de objetos Child a serem exibidos.
     */
    fun submitList(objs: List<Child>) {
        //   Log.d("USUK", "ContainerController.submitList: ${lastObjectsList.joinToString("\n") { it.sortableProp }}")
        val sortedObjects = objs.sortedBy { it.sortableProp }
            .subList(0, min(maxViews, objs.size))

        /** removo todas as views que nao estao presentes entre as [maxViews] primeiras posições do array*/
        children.values.filter { currentObject ->
            !sortedObjects.any { newObject -> newObject.id == currentObject.id }
        }.forEach {
            removeView(it)
        }

        /** adiciono as novas [maxViews] views conforme sua posição na lista ordenada*/
        sortedObjects.forEachIndexed { index, obj ->

            // removo a view antiga se existir (serva para "atualizar" a view)
            children[obj.id]?.let { removeView(it) }

            addView(
                obj,
                min(index, container.childCount),
                (lastObjectsList.isNotEmpty() && !lastObjectsList.any { obj.id == it.id })
            )
        }
        lastObjectsList = objs
    }

    /**
     * Remove uma view específica do container e do holder.
     * @param obj O objeto Child cuja view correspondente deve ser removida.
     */
    private fun removeView(obj: Child) {
        //  Log.d("USUK", "ContainerController.".plus("removeView() obj = ${obj.sortableProp}"))
        container.removeView(obj.binding.root)
        children.remove(obj.id)
    }

    /**
     * Adiciona uma nova view ao container em um índice específico e ao holder.
     * @param obj O objeto Child cuja view correspondente deve ser adicionada.
     * @param index O índice no qual a view deve ser adicionada no container.
     */
    private fun addView(obj: Child, index: Int, delayedAnimation: Boolean = false) {
        // Log.d(    "USUK",   "ContainerController.".plus("addView() obj = ${obj.sortableProp}, index = $index, delayedAnimation = $delayedAnimation")   )
        if (delayedAnimation) {
            container.postDelayed({ container.addView(obj.binding.root, index) }, 350)

        } else container.addView(obj.binding.root, index)

        children[obj.id] = obj
    }

    /**
     * Bloco de inicialização que adiciona este controlador como um observador do ciclo de vida do lifecycleOwner.
     */
    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    /**
     * Remove todas as views do container e limpa o holder.
     * Este mét.odo é chamado quando o ciclo de vida do proprietário é destruído.
     */
    private fun clear() {
        children.values.forEach {
            container.removeView(it.binding.root)
        }
        children.clear()
        lastObjectsList = emptyList()
    }

    /**
     * Chamado quando o LifecycleOwner associado é destruído.
     * Limpa todas as views e recursos mantidos pelo controlador.
     * @param owner O LifecycleOwner cujo estado mudou.
     */
    override fun onDestroy(owner: LifecycleOwner) {
        clear()
    }

    /**
     * Classe de dados para representar uma view filha a ser gerenciada pelo ContainerController.
     *
     * @property id Um identificador único para a view filha.
     * @property sortableProp Uma propriedade usada para ordenar a lista de Child.
     * @property binding O ViewBinding associado à view filha.
     */
    data class Child(
        val id: String,
        /**usado para ordenar os objetos deste tipo na lista*/
        val sortableProp: String,
        val binding: ViewBinding,
    )
}