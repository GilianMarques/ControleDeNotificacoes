package dev.gmarques.controledenotificacoes.presentation.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionValues
import androidx.transition.Visibility

/**
 * Criado por Gilian Marques
 * Em terça-feira, 22 de abril de 2025 as 22:29.
 */
class SlideTransition : Visibility() {
    override fun onAppear(
        sceneRoot: ViewGroup,
        view: View,
        startValues: TransitionValues?,
        endValues: TransitionValues?,
    ): Animator {
        val distance = view.height * 0.1f
        view.translationY = distance
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f)
    }

    override fun onDisappear(
        sceneRoot: ViewGroup,
        view: View,
        startValues: TransitionValues?,
        endValues: TransitionValues?,
    ): Animator {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.height * 0.1f)
    }
}
