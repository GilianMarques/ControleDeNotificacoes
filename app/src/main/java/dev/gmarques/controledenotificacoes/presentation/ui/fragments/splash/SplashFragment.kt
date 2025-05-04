package dev.gmarques.controledenotificacoes.presentation.ui.fragments.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentSplashBinding
import dev.gmarques.controledenotificacoes.domain.model.User
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.home.HomeViewModel
import kotlinx.coroutines.launch

/**
 * Criado por Gilian Marques
 * Em domingo, 27 de abril de 2025 as 17:55.
 */
class SplashFragment : MyFragment() {

    /*Eu acesso esse ViewModel a partir daqui para que ele ao inicializar carregue os dados do HomeFragment.
    Assim, quando o usuário terminar o processo de login os dados já estejam carregados */
    private val homeFragmentViewModel: HomeViewModel by activityViewModels()
    private val viewModel: LoginViewModel by viewModels()

    private lateinit var binding: FragmentSplashBinding

    private val signInLauncher = setupSignInLauncher()


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
        observeNavigationEvent()
        setupFabTryAgain()
        observeSharedViewModel()
        viewModel.checkUserLoggedIn()
    }

    /**
     * Configura o ActivityResultLauncher para o fluxo de login do FirebaseUI.
     * @return O ActivityResultLauncher configurado.
     */
    private fun setupSignInLauncher(): ActivityResultLauncher<Intent?> {
        return registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { res ->
            viewModel.handleLoginResult(res.resultCode, res.idpResponse)
        }
    }

    /**
     * Observa os eventos do ViewModel para reagir a diferentes estados do fluxo de login.
     */
    private fun observeViewModelEvents() {
        collectFlow(viewModel.eventFlow) { event ->
            when (event) {

                is LoginEvent.StartFlow -> startLoginFlow()
                is LoginEvent.Success -> onLoginSuccess(event.user)
                is LoginEvent.Error -> onLoginError(event.message)
            }
        }
    }

    /**
     * Essa função observa um flow que carrega um objeto que indica quando todos os requisitos necessários foram satisfeitos
     * para que este fragmento navegue para o próximo
     */
    private fun observeNavigationEvent() {
        collectFlow(viewModel.navigationFlow) { requirements ->

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
    }

    /**
     * Configura o listener para o botão de tentar novamente.
     */
    private fun setupFabTryAgain() {
        binding.fabTryAgain.setOnClickListener {
            binding.fabTryAgain.isInvisible = true
            binding.progressBar.isVisible = true
            binding.tvInfo.text = ""
            startLoginFlow()
        }
    }

    /**
     * Inicia o fluxo de login usando o FirebaseUI.
     */
    private fun startLoginFlow() {
        val providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build())

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.ic_launcher_foreground)
            .setTheme(R.style.AppTheme)
            .build()

        signInLauncher.launch(signInIntent)
    }

    /**
     * Manipula o sucesso do login, configurando a UI com os dados do usuário.
     * @param user O objeto User do usuário logado.
     */
    private fun onLoginSuccess(user: User) {
        setupUiWithUserData(user)
    }

    /**
     * Manipula o erro do login, exibindo uma mensagem e o botão de tentar novamente.
     * @param message A mensagem de erro a ser exibida.
     */
    private fun onLoginError(message: String) {

        vibrator.error()
        binding.fabTryAgain.isVisible = true
        binding.progressBar.isInvisible = true
        binding.tvInfo.text = message
    }

    private fun setupUiWithUserData(user: User) = lifecycleScope.launch {
        /**
         * Configura a interface do usuário com os dados do usuário logado.
         * @param user O objeto User do usuário logado.
         */
        with(binding) {

            progressBar.isVisible = false


            val nome = user.name.split(" ").firstOrNull().orEmpty().ifBlank { "?" }

            vibrator.success()

            tvUserName.text = user.name
            tvGreetings.text = getString(R.string.BemvindoX, nome)

            user.photoUrl.let { photoUrl ->
                Glide.with(root.context)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(ivProfilePicture)

            }
        }
    }

    /**
     * Observa um flow no viewmodel do HomeFragmentos que indica quando os dados foram carregados e então chama no viewmodel uma função
     * que indica que este requisito, o de carregar os dados locais, foi satisfeito isso garante que o homefragment não será chamado
     * antes que os dados locais sejam carregados garantindo que haja uma transição suave entre fragmentos e que todos os dados
     * apareçam instantaneamente na tela
     */
    private fun observeSharedViewModel() {
        collectFlow(homeFragmentViewModel.managedAppsWithRules) { apps ->
            apps?.let {
                viewModel.localDataLoaded()
            }
        }
    }

}
