package dev.gmarques.controledenotificacoes.presentation.utils

import android.util.Log
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


/**
 * Criado por Gilian Marques
 * Em terça-feira, 01 de abril de 2025 as 00:09.
 */
object ViewExtFuns {

    /**
     * Adiciona uma View filha a este ViewGroup com uma animação de duas etapas: inicialmente invisível, depois visível com um atraso.
     * Remove a View do pai anterior, se necessário, antes de adicionar.
     *
     * @param child A View a ser adicionada.
     * @param index Índice onde a View será adicionada. -1 para o final. Padrão: -1.
     *
     * **Comportamento:**
     * - **Início:** `child` fica `View.INVISIBLE`.
     * - **Atraso:** Após 300ms, `child` fica `View.VISIBLE`.
     * - **Remoção:** Se `child` já tem um pai, é removida e adicionada novamente após 50ms.
     * - **Thread:** Executa na thread principal.
     *
     * **Exemplo:**
     * ```kotlin
     * myViewGroup.addViewWithTwoStepsAnimation(myView)
     * ```
     */
    fun ViewGroup.addViewWithTwoStepsAnimation(child: View, index: Int = -1) {

        fun attemptAdd() {

            val parent = child.parent as? ViewGroup

            if (parent != null) {
                parent.removeView(child)
                handler.postDelayed(::attemptAdd, 50)
            } else {
                child.visibility = View.INVISIBLE
                addView(child, if (index != -1) index else childCount)

                child.postDelayed({
                    child.visibility = View.VISIBLE
                }, 300)
            }
        }

        attemptAdd()
    }

}