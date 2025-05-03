package dev.gmarques.controledenotificacoes.presentation.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionSet
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.ViewActivityHeaderBinding
import dev.gmarques.controledenotificacoes.domain.plataform.VibratorInterface
import dev.gmarques.controledenotificacoes.plataform.VibratorImpl
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_managed_apps.AddManagedAppsFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule.AddOrUpdateRuleFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.home.HomeFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.profile.ProfileFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.SelectAppsFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule.SelectRuleFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.splash.SplashFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.SlideTransition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.Serializable
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

        val fadeTransition = Fade().apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 200
        }


        val slide = SlideTransition().apply {
            duration = 180
            interpolator = AccelerateDecelerateInterpolator()
        }

        val transitionSet = TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(fadeTransition)
            addTransition(slide)
        }


        exitTransition = transitionSet // saida do fragmento 1
        enterTransition = transitionSet // entrada do fragmento 2
        //    returnTransition = transitionSetExit2 // saida do fragmento 2
        //   reenterTransition = transitionSetEnter1 // retorno do fragmento 1


        // Transição de entrada
        sharedElementEnterTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(Fade())
            addTransition(SlideTransition())
            interpolator = OvershootInterpolator(1f)
            duration = 400
        }

        // Transição de retorno
        sharedElementReturnTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(Fade())
            addTransition(SlideTransition())
            interpolator = OvershootInterpolator(1f)
            duration = 400
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupGoBackButton(ivGoBack: AppCompatImageView) {
        ivGoBack.setOnClickListener(AnimatedClickListener {
            goBack()
        })
    }

    protected open fun setupActionBar(binding: ViewActivityHeaderBinding) {

        when (this@MyFragment) {

            is HomeFragment -> {
                binding.tvTitle.text = getString(R.string.app_name)
                binding.ivMenu.isGone = true
                binding.ivGoBack.isInvisible = true
            }

            is AddManagedAppsFragment -> {
                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Gerenciar_aplicativos)
                binding.ivMenu.isGone = true
            }

            is AddOrUpdateRuleFragment -> {
                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Adicionar_regra)
                binding.ivMenu.isGone = true
            }

            is SelectAppsFragment -> {
                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Selecionar_aplicativos)
                binding.ivMenu.isGone = true
            }

            is SelectRuleFragment -> {
                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Selecionar_regra)
                binding.ivMenu.isGone = true
            }

            is SplashFragment -> {

                binding.tvTitle.text = ""
                binding.ivMenu.isGone = true
                binding.ivGoBack.isGone = true
            }

            is ProfileFragment -> {

                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Perfil)
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

    /**
     * Favorece o DRY evitando boilerplate code
     * Observa um Flow de maneira segura no Fragment, garantindo que a coleta só aconteça enquanto
     * o Fragment estiver ativo (STARTED).
     */
    protected fun <T> collectFlow(flow: Flow<T>, onCollect: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { value ->
                    onCollect(value)
                }
            }
        }
    }

    /**
     * Obtém um objeto serializável de um Bundle com segurança de tipo, considerando as diferentes APIs do Android para desserialização.
     *
     * Este méto-do gerencia a recuperação de objetos serializáveis de um Bundle, tratando as diferenças entre as versões
     * mais recentes do Android (Tiramisu e superior) que oferecem uma API tipada para serialização/desserialização,
     * e versões anteriores onde é necessário realizar a conversão de tipo manualmente e verificar a instância.
     *
     * Ele lança uma exceção se a chave não existir no Bundle para evitar null pointers inesperados.
     * No caso de tipos não anuláveis, a ausência da chave ou um tipo incorreto resultarão em uma exceção.
     * @return O objeto serializável do tipo [T], ou null se a chave não existir no Bundle
     *         e o tipo for anulável.
     * @throws IllegalStateException Se o objeto serializado sob a chave não for uma instância de [clazz].
     */
    protected fun <T : Serializable> requireSerializableOf(bundle: Bundle, key: String, clazz: Class<T>): T? {

        if (!bundle.containsKey(key)) error("O pacote não tem nenhum conteudo sob a chave '$key'. Verifique se voce passou a chave certa. Se o objeto é nulavel, certifique-se de passar nulo para o bundle.")

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getSerializable(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            bundle.getSerializable(key).let {
                if (clazz.isInstance(it)) clazz.cast(it)
                else throw IllegalStateException("Objeto serializado sob a chave '$key' não é do tipo esperado: ${clazz.name}. Valor real: ${it?.javaClass?.name}")

            }
        }
    }

}