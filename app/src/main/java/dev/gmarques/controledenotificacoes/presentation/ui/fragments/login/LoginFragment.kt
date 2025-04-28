package dev.gmarques.controledenotificacoes.presentation.ui.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes.DEVELOPER_ERROR
import com.firebase.ui.auth.ErrorCodes.NO_NETWORK
import com.firebase.ui.auth.ErrorCodes.PROVIDER_ERROR
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentLoginBinding
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import java.text.MessageFormat

/**
 * Criado por Gilian Marques
 * Em domingo, 27 de abril de 2025 as 17:55.
 */
class LoginFragment : MyFragment() {

    // TODO: otimizar isso pq foi feito as pressas

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res -> onSignInResult(res) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser != null) {
            findNavController().navigate(LoginFragmentDirections.toHomeFragment())
            return
        }

        setupFabTryAgain()
        doLogin()
    }

    private fun setupFabTryAgain() {
        binding.fabTryAgain.setOnClickListener {
            binding.fabTryAgain.visibility = View.GONE
            binding.tvInfo.text = ""
            doLogin()
        }
    }

    private fun doLogin() {
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            // .setLogo(R.drawable.vec_product)
            // .setTheme(R.style.Login_Activity)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            signInSuccess(FirebaseAuth.getInstance().currentUser)
        } else handleSignInErrors(response)
    }

    private fun signInSuccess(user: FirebaseUser?) {
        vibrator.success()

        val nome = user?.displayName?.split(" ")?.firstOrNull() ?: "?"
        binding.tvInfo.text = getString(R.string.BemvindoX, nome).ifBlank { nome }

        findNavController().navigate(LoginFragmentDirections.toHomeFragment())
    }

    private fun handleSignInErrors(response: IdpResponse?) {
        vibrator.error()
        binding.fabTryAgain.visibility = View.VISIBLE

        if (response == null) {
            binding.tvInfo.setText(R.string.Voce_cancelou_o_login)
        } else when (response.error?.errorCode) {
            NO_NETWORK -> binding.tvInfo.setText(R.string.Vocenaoestaconectadoainternetouadata)
            DEVELOPER_ERROR -> binding.tvInfo.text = getString(
                R.string.O_login_falhou_por_um_erro_de_desenvolvimento_da_aplica_o_contate_o_desenvolvedor_c_digo_de_erro_1_mensagem_2,
                response.error!!.errorCode,
                response.error!!.message!!
            )

            PROVIDER_ERROR -> binding.tvInfo.text = getString(
                R.string.O_login_falhou_por_um_erro_no_provedor_de_login,
                response.error!!.errorCode,
                response.error!!.message!!
            )

            else -> binding.tvInfo.text = MessageFormat.format(
                getString(R.string.Ologinfalhou),
                response.error!!.errorCode,
                response.error!!.message!!
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
