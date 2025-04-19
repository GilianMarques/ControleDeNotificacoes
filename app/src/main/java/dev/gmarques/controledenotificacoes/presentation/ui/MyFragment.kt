package dev.gmarques.controledenotificacoes.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.ViewActivityHeaderBinding
import dev.gmarques.controledenotificacoes.domain.plataform.VibratorInterface
import dev.gmarques.controledenotificacoes.plataform.VibratorImpl
import dev.gmarques.controledenotificacoes.presentation.ui.home_fragment.HomeFragment
import dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment.AddManagedAppsFragment
import dev.gmarques.controledenotificacoes.presentation.ui.rule_fragment.AddRuleFragment
import dev.gmarques.controledenotificacoes.presentation.ui.select_apps_fragment.SelectAppsFragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = TransitionInflater.from(requireContext())

        enterTransition = inflater.inflateTransition(android.R.transition.fade)
        exitTransition = inflater.inflateTransition(android.R.transition.fade)

        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupGoBackButton(ivGoBack: AppCompatImageView) {
        ivGoBack.setOnClickListener(AnimatedClickListener {
            goBack()
            vibrator.interaction()

        })
    }

    protected open fun initActionBar(binding: ViewActivityHeaderBinding) {

        when (this@MyFragment) {

            is HomeFragment -> {
                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.app_name)
                binding.ivMenu.isGone = true
            }

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

            is SelectAppsFragment -> {
                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Selecionar_aplicativos)
                binding.ivMenu.isGone = true
            }

            else -> {
                throw IllegalArgumentException("Inclua o codigo de inicializaçao da Actionbar para esse fragmento aqui")
            }

        }
    }

    /**
     * Exibe um Snackbar de erro com a mensagem de erro fornecida e aciona uma vibração como feedback.
     *
     * Esta função é uma utilidade para mostrar mensagens de erro não críticas ao usuário. Ela utiliza
     * o Snackbar do Android para uma exibição temporária da mensagem e a combina com uma breve vibração
     * para fornecer feedback adicional.
     *
     * @param errorMsg A mensagem de erro a ser exibida no Snackbar. Esta deve ser uma string concisa
     *                 explicando a natureza do erro ao usuário.
     *
     * @see Snackbar
     * @see VibratorImpl
     */
    protected open fun showErrorSnackBar(errorMsg: String, targetView: View = requireView()) {
        Snackbar.make(requireView(), errorMsg, Snackbar.LENGTH_LONG)
            .setAnchorView(targetView).show()
        vibrator.error()
    }

    /**
     * Navega o usuário de volta para a tela anterior na pilha de navegação.
     *
     * Esta função utiliza o méto-do `navigateUp()` do componente Navigation para
     * mover o usuário de volta para o destino de onde ele veio. É uma maneira
     * comum de implementar a funcionalidade "voltar" na interface do usuário de um aplicativo.
     *
     * Este méto-do é chamado para simular o pressionamento do botão voltar.
     */
    protected fun goBack() {
        findNavController().popBackStack()
    }
}