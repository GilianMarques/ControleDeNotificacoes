package dev.gmarques.controledenotificacoes.presentation.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
     * dando tempo do container ajustar suas dimensoes antes da view ser exibida com um fade-in.
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

    fun TextView.setStartDrawable(adequatedDrawable: Drawable) {
        this.setCompoundDrawablesWithIntrinsicBounds(
            adequatedDrawable,
            null,
            null,
            null
        )
    }

    fun TextView.removeDrawables() {
        this.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            null,
            null
        )
    }

    fun View.showKeyboard() {
        this.requestFocus()

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun View.hideKeyboard() {
        this.requestFocus()

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

}

