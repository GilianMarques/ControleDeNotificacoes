package dev.gmarques.controledenotificacoes.presentation.utils

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.ScaleAnimation
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [AnimatedClickListener] é um [View.OnClickListener] personalizado que fornece um feedback visual
 * ao usuário ao clicar em uma view. Isso é feito redimensionando a view brevemente, simulando
 * um efeito de pressionamento, e disparando uma ação personalizada após a conclusão da animação.
 *
 * @property action Uma função lambda que representa a ação a ser executada após o término da animação de clique.
 */
class AnimatedClickListener(private val action: () -> Unit) : View.OnClickListener {

    override fun onClick(v: View) {
        animateView(v)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun animateView(view: View) = GlobalScope.launch(Main) {
        val animation = ScaleAnimation(
            1f, 0.96f,
            1f, 0.96f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ScaleAnimation.REVERSE
            repeatCount = 1
        }

        view.startAnimation(animation)
        action.invoke()
    }
}
