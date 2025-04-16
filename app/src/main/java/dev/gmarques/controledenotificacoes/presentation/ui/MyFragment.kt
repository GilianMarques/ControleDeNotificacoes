package dev.gmarques.controledenotificacoes.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.ViewActivityHeaderBinding
import dev.gmarques.controledenotificacoes.domain.plataform.VibratorInterface
import dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment.AddManagedAppsFragment
import dev.gmarques.controledenotificacoes.presentation.ui.rule_fragment.AddRuleFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 16 de abril de 2025 as 17:43.
 */
@AndroidEntryPoint
open class MyFragment : Fragment() {

    @Inject
    lateinit var vibrator: VibratorInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupGoBackButton(ivGoBack: AppCompatImageView) {
        ivGoBack.setOnClickListener(AnimatedClickListener {
            findNavController().popBackStack()
            vibrator.interaction()

        })
    }

    protected fun initActionBar(binding: ViewActivityHeaderBinding) {

        when (this@MyFragment) {
            is AddManagedAppsFragment -> {
                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Gerenciar_aplicativos)
                binding.ivMenu.isGone = true
            }

            is AddRuleFragment -> {
                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Adicionar_regra)
                binding.ivMenu.isGone = true
            }
            // TODO: terminar de incluir os outro frags
        }
    }
}