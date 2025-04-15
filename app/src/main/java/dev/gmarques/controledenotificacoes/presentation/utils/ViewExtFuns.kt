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

    fun ViewGroup.addViewWithTwoStepsAnimation(child: View, index: Int = -1) {

        child.visibility = View.INVISIBLE
        addView(child, if (index != -1) index else childCount)

        child.postDelayed({
            child.visibility = View.VISIBLE
        }, 300)
    }

}

