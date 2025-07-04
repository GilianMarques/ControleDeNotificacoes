package dev.gmarques.controledenotificacoes.presentation.ui.fragments.splash

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentSplashBinding
import dev.gmarques.controledenotificacoes.domain.model.User
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.home.HomeFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.home.HomeViewModel
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.login.LoginFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Criado por Gilian Marques
 * Em domingo, 27 de abril de 2025 as 17:55.
 */
class SplashFragment : MyFragment() {

    /*Permite carregar os dados do usuario antecipadamente*/
    private val homeFragmentViewModel: HomeViewModel by activityViewModels()
    private val viewModel: SplashViewModel by viewModels()

    private val args: SplashFragmentArgs by navArgs()
    private lateinit var binding: FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModelEvents()
        observeRemoteConfig()
        observeNavigationEvent()
        observeSharedViewModel()

        val user = args.user
        if (user != null) {
            animateAndNavigateHomeIfUserJustLoggedIn(user)
        } else viewModel.checkLoginState()


    }

    /**
     * Observa as configurações remotas do aplicativo.
     * Esta função coleta um fluxo de valores de configuração remota e atualiza os requisitos de navegação
     * com base no status de bloqueio do aplicativo.
     */
    private fun observeRemoteConfig() {
        collectFlow(App.instance.remoteConfigValues) {
            it?.blockApp?.let {
                if (!it) viewModel.addNavigationRequirement(NavigationRequirements.Requirement.APP_NOT_BLOCKED)
            }
        }
    }

    /**
     * Se [SplashFragment] recebeu um [User] como parametro, significa que ele foi chamado pelo [LoginFragment]
     * após um fluxo bem sucedido de login e portanto deve navegar direto pra [HomeFragment] após animar a ui.
     *
     * Nota a navegação de fato só acontece após os requisitos de navegação serem conluidos veja
     * @see [navigateHomeIfRequirementsAreSatisfied]
     */
    private fun animateAndNavigateHomeIfUserJustLoggedIn(user: User) {
        lifecycleScope.launch {

            val typedValue = TypedValue()
            requireActivity().theme.resolveAttribute(R.attr.AppColorBackground, typedValue, true)
            val color = typedValue.data
            binding.root.setBackgroundColor(color)

            setupUiWithUserData(user)
            delay(2000)
            viewModel.addNavigationRequirement(NavigationRequirements.Requirement.USER_LOGGED_IN)

        }
    }

    /**
     * Observa os eventos do ViewModel para reagir a diferentes estados do fluxo de login.
     */
    private fun observeViewModelEvents() {
        collectFlow(viewModel.eventFlow) { event ->
            when (event) {
                is SplashEvent.NavigateToLoginFragment -> navigateToFragmentLogin()
            }
        }
    }

    /**
     * Essa função observa um flow que carrega um objeto que indica quando todos os requisitos necessários foram satisfeitos
     * para que este fragmento navegue para o próximo
     */
    private fun observeNavigationEvent() {
        collectFlow(viewModel.navigationFlow) { requirements ->
            navigateHomeIfRequirementsAreSatisfied(requirements)
        }
    }

    private fun navigateHomeIfRequirementsAreSatisfied(requirements: NavigationRequirements) {

        if (requirements.canNavigateHome()) {
            val extras = FragmentNavigatorExtras(
                binding.tvUserName to binding.tvUserName.transitionName,
                binding.ivProfilePicture to binding.ivProfilePicture.transitionName,
            )
            findNavController().navigate(
                SplashFragmentDirections.toHomeFragment(),
                extras
            )

        }
    }

    private fun navigateToFragmentLogin() {
        findNavController().navigate(SplashFragmentDirections.toLoginFragment())
    }

    private fun setupUiWithUserData(user: User) = lifecycleScope.launch {
        /**
         * Configura a interface do usuário com os dados do usuário logado.
         * @param user O objeto User do usuário logado.
         */
        with(binding) {

            user.name.split(" ").firstOrNull().orEmpty().ifBlank { "ué?" }

            vibrator.success()

            tvUserName.text = user.name
            tvUserName.isVisible = true

            user.photoUrl.let { photoUrl ->
                Glide.with(root.context)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .circleCrop()
                    .into(ivProfilePicture)

            }

            delay(100)
            ivProfilePicture.isVisible = true
        }
    }

    /**
     * Observa um flow no [HomeViewModel] que indica quando os dados do usuario foram carregados. Isso garante que
     * o [HomeFragment] não será chamado antes que os dados locais sejam carregados garantindo que haja uma transição
     * suave entre os fragmentos e que todos os dados apareçam instantaneamente na tela
     */
    private fun observeSharedViewModel() {
        collectFlow(homeFragmentViewModel.managedAppsWithRules) { apps ->
            apps?.let {
                viewModel.addNavigationRequirement(NavigationRequirements.Requirement.DATA_LOADED)
            }
        }
    }

}
