package dev.gmarques.controledenotificacoes.presentation.utils

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


/**
 * Criado por Gilian Marques
 * Em terça-feira, 01 de abril de 2025 as 00:09.
 */
object ViewExtFuns {

    /**
     * Adiciona uma view filha ao ViewGroup com uma animação de duas etapas.
     *
     * A view é adicionada ao container oculta e, após um breve atraso, torna-se visível,
     * dando tempo do conteiner ajustar suas dimensoes antes da view ser exibida com um fade-in.
     *
     * @param child A view filha a ser adicionada.
     * @param index O índice onde a view será adicionada. -1 para adicionar ao final (padrão).
     * @throws IllegalArgumentException Se o índice estiver fora do intervalo (< -1 ou > childCount).
     */
    fun ViewGroup.addViewWithTwoStepsAnimation(child: View, index: Int = -1) {

        child.visibility = View.INVISIBLE
        addView(child, if (index != -1) index else childCount)

        child.postDelayed({
            child.visibility = View.VISIBLE
        }, 300)
    }

    fun TextView.setRuleDrawable(adequatedDrawable: Drawable) {
        this.setCompoundDrawablesWithIntrinsicBounds(
            adequatedDrawable,
            null,
            null,
            null
        )
    }

}

