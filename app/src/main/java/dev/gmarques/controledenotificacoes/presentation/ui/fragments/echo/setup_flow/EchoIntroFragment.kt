package dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.setup_flow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentEchoIntroBinding
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment

/**
 *Criado por Gilian Marques
 * Em 12/06/2025 as 14:29
 */
@AndroidEntryPoint
class EchoIntroFragment : MyFragment() {

    private val viewModel: EchoFlowSharedViewModel by navGraphViewModels(R.id.nav_graph_echo) {
        defaultViewModelProviderFactory
    }
    private lateinit var binding: FragmentEchoIntroBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentEchoIntroBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigateToEchoIsEnabledFragIfNeeded()
        setupFabEcho()
    }


    /**Se o echo ja estiver ligado, navega direto pra tela onde ele pode ser desligado.*/
    private fun navigateToEchoIsEnabledFragIfNeeded() {
        if (viewModel.isEchoEnabled()) findNavController().navigate(EchoIntroFragmentDirections.toEchoIsEnabled())
    }


    /**
     * Configura o listener para o botão de echo.
     */
    private fun setupFabEcho() {
        binding.fab.setOnClickListener {
            findNavController().navigate(EchoIntroFragmentDirections.toEchoStepOneFragment())
        }
    }


}
