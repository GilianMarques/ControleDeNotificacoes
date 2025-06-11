package dev.gmarques.controledenotificacoes.presentation.ui.fragments.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieOnCompositionLoadedListener
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentLoginBinding
import dev.gmarques.controledenotificacoes.domain.model.User
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment

/**
 * Criado por Gilian Marques
 * Em domingo, 27 de abril de 2025 as 17:55.
 */
class LoginFragment : MyFragment() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding

    private val signInLauncher = setupSignInLauncher()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentLoginBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        awaitLottieAnim()
        setupFabTryAgain()
        setupFabLogin()
        observeViewModelEvents()
    }

    private fun awaitLottieAnim() {
        binding.lottieView.addLottieOnCompositionLoadedListener(object : LottieOnCompositionLoadedListener {
            override fun onCompositionLoaded(composition: LottieComposition?) {
                binding.clContent.isVisible = true
            }
        })
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
     * Configura o listener para o botão de login.
     */
    private fun setupFabLogin() {
        binding.fabLogin.setOnClickListener {
            startLoginFlow()
        }
    }

    /**
     * Inicia o fluxo de login usando o FirebaseUI.
     */
    private fun startLoginFlow() {

        binding.tvInfo.isInvisible = true
        binding.tvDataDetail.isInvisible = true
        binding.fabLogin.isInvisible = true
        binding.progressBar.isVisible = true


        val providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build())

        val signInIntent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
            .setLogo(R.drawable.ic_launcher_foreground).setTheme(R.style.AppTheme).build()

        signInLauncher.launch(signInIntent)
    }

    /**
     * Manipula o sucesso do login, configurando a UI com os dados do usuário.
     * @param user O objeto User do usuário logado.
     */
    private fun onLoginSuccess(user: User) {
        findNavController().navigate(LoginFragmentDirections.toSplashFragment(user))
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
        binding.tvInfo.isVisible = true

    }


}
