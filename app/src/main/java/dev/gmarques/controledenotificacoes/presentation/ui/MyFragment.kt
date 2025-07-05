package dev.gmarques.controledenotificacoes.presentation.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionSet
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.ViewActivityHeaderBinding
import dev.gmarques.controledenotificacoes.domain.data.PreferenceProperty
import dev.gmarques.controledenotificacoes.domain.framework.VibratorInterface
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.ReadPreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.SavePreferenceUseCase
import dev.gmarques.controledenotificacoes.framework.VibratorImpl
import dev.gmarques.controledenotificacoes.presentation.ui.activities.MainActivity
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_managed_apps.AddManagedAppsFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_condition.AddOrUpdateConditionFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule.AddOrUpdateRuleFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.home.HomeFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.profile.ProfileFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.SelectAppsFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_notification.SelectNotificationFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule.SelectRuleFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.settings.SettingsFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.splash.SplashFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.SlideTransition
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 16 de abril de 2025 as 17:43.
 */
@AndroidEntryPoint
open class MyFragment() : Fragment() {

    private var dialogHint: AlertDialog? = null

    @Inject
    lateinit var vibrator: VibratorInterface

    @Inject
    lateinit var savePreferenceUseCase: SavePreferenceUseCase

    @Inject
    lateinit var readPreferenceUseCase: ReadPreferenceUseCase

    private var isFabVisible = true
    private var animatingFab = false


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
            interpolator = AccelerateDecelerateInterpolator()
            duration = 250
        }

        // Transição de retorno
        sharedElementReturnTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(Fade())
            addTransition(SlideTransition())
            interpolator = AccelerateDecelerateInterpolator()
            duration = 250
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        blockAppIfNeeded()
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

            is SettingsFragment -> {

                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Configuracoes)
                binding.ivMenu.isGone = true
            }

            is AddOrUpdateConditionFragment -> {

                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Nova_condicao)
                binding.ivMenu.isGone = true
            }

            is SelectNotificationFragment -> {
                setupGoBackButton(binding.ivGoBack)
                binding.tvTitle.text = getString(R.string.Selecionar_notificacao)
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
        Snackbar.make(requireView(), errorMsg, Snackbar.LENGTH_LONG).setAnchorView(targetView).show()
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
        vibrator.interaction()
        findNavController().popBackStack()
    }

    protected fun requireMainActivity(): MainActivity {
        return requireActivity() as MainActivity
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
            @Suppress("DEPRECATION") bundle.getSerializable(key).let {
                if (clazz.isInstance(it)) clazz.cast(it)
                else throw IllegalStateException("Objeto serializado sob a chave '$key' não é do tipo esperado: ${clazz.name}. Valor real: ${it?.javaClass?.name}")

            }
        }
    }

    /**
     * Exibe um diálogo de dica para o usuário. O diálogo será mostrado apenas uma vez para cada chave de dica.
     *
     * @param showHintPreference Uma chave única para identificar esta dica. Usada para rastrear se a dica já foi exibida.
     * @param msg A mensagem de texto da dica a ser exibida no diálogo.
     * @param delay Um atraso opcional em milissegundos antes de exibir o diálogo. O padrão é 500.
     *
     * O diálogo tem duas opções:
     * - "Entendi": Salva a preferência para não mostrar esta dica novamente e fecha o diálogo.
     * - "Lembre-me da próxima vez": Fecha o diálogo sem salvar a preferência, permitindo que a dica seja mostrada novamente.
     *
     */
    protected fun showHintDialog(
        showHintPreference: PreferenceProperty<Boolean>,
        msg: String,
        delay: Long = 500L,
    ) = lifecycleScope.launch {


        if (showHintPreference.value == false) return@launch

        dialogHint?.dismiss()

        if (delay > 0) delay(delay)

        vibrator.interaction()

        dialogHint = MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Dica)).setMessage(msg)
            .setPositiveButton(getString(R.string.Entendi)) { dialog, _ ->
                lifecycleScope.launch { showHintPreference(false) }
            }.setNegativeButton(getString(R.string.Lembre_me_da_proxima_vez)) { dialog, _ ->
            }.setCancelable(false).setIcon(R.drawable.vec_hint)
            .show()
    }

    /**
     * Configura um listener de scroll em um RecyclerView para ocultar ou exibir uma view alvo (tipicamente um ExtendedFloatingActionButton)
     * com base na direção do scroll.
     *
     * Quando o usuário rola para baixo (dy > 0) e o botão está visível, ele é ocultado com uma animação.
     * Quando o usuário rola para cima (dy < 0) e o botão está oculto, ele é exibido com uma animação.
     *
     * @param recyclerView O RecyclerView ao qual o listener de scroll será adicionado.
     * @param targetView A view (geralmente um ExtendedFloatingActionButton) que será ocultada ou exibida.
     * @see toggleFabVisibility
     */
    protected fun hideViewOnRVScroll(
        recyclerView: RecyclerView,
        targetView: ExtendedFloatingActionButton,
    ) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && isFabVisible) {
                    toggleFabVisibility(false, targetView)
                } else if (dy < 0 && !isFabVisible) {
                    toggleFabVisibility(true, targetView)
                }
            }
        })
    }

    /**
     * Alterna a visibilidade de uma view, geralmente Floating Action Button (FAB) com uma animação de transição.
     *
     * Se a view já estiver em processo de animação (`animatingFab` é `true`), a função retorna
     * imediatamente para evitar animações concorrentes.  Caso contrário, define a translação Y
     * da view para mostrar ou esconder o botão e inicia a animação.
     *
     * @param show `true` para mostrar a view, `false` para escondê-lo.
     * @param targetView a view que sera animada
     */
    protected fun toggleFabVisibility(show: Boolean, targetView: View) {

        if (animatingFab) return
        else isFabVisible = show
        val translationY = if (show) 0f else (targetView.height * 2f)

        targetView.animate().translationY(translationY).setDuration(400L)
            .setInterpolator(android.view.animation.AnticipateOvershootInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    animatingFab = true
                    super.onAnimationStart(animation)
                }

                override fun onAnimationEnd(animation: Animator) {
                    animatingFab = false
                    super.onAnimationEnd(animation)
                }
            }).start()
    }

    /**
     *  Define o app como bloqueado com base em sua versao e configurações do remote config
     */
    private fun blockAppIfNeeded() {

        collectFlow(App.instance.remoteConfigValues) {
            if (it == null || !it.blockApp) return@collectFlow

            vibrator.error()
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle(getString(R.string.Atencao))
                .setMessage(
                    getString(
                        R.string.Esta_versao_do_app_foi_bloqueada_verifique_atualiza_es_na_play_store,
                        getString(R.string.app_name)
                    )
                )
                .setPositiveButton(getString(R.string.Ir_a_loja)) { dialog, _ ->
                    openPlayStore()
                }
                .setNegativeButton(getString(R.string.Sair)) { dialog, _ ->
                    exitProcess(0)
                }
                .setCancelable(false)
                .show()
        }

    }

    /**
     * Abre a página do aplicativo na Play Store.
     *
     * Tenta abrir diretamente no aplicativo da Play Store. Se não estiver instalado,
     * abre no navegador.
     *
     * Utiliza um link do Firebase Remote Config se disponível, caso contrário, usa o nome do pacote do aplicativo.
     */
    protected fun openPlayStore() {
        val appPackageName = App.instance.packageName
        // TODO: otimizar depois dos testes

        val playStoreLink = App.instance.remoteConfigValues.value?.playStoreAppLink
        if (!playStoreLink.isNullOrBlank()) {

            try {

                val intent = Intent(Intent.ACTION_VIEW, playStoreLink.toUri()).apply {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                    setPackage("com.android.vending")
                }
                startActivity(intent)

            } catch (_: ActivityNotFoundException) {

                val intent = Intent(Intent.ACTION_VIEW, playStoreLink.toUri()).apply {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)

            }
        } else try {

            val intent = Intent(
                Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri()
            ).addFlags(FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.android.vending")
            startActivity(intent)

        } catch (_: ActivityNotFoundException) {

            val intent = Intent(
                Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
            ).addFlags(FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }
    }

}
