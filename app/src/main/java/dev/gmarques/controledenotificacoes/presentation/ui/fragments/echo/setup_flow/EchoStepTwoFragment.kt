package dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.setup_flow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentEchoStepTwoBinding
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment

/**
 *Criado por Gilian Marques
 * Em 12/06/2025 as 14:29
 */
@AndroidEntryPoint
class EchoStepTwoFragment : MyFragment() {

    private val viewModel: EchoFlowSharedViewModel by navGraphViewModels(R.id.nav_graph_echo) {
        defaultViewModelProviderFactory
    }
    private lateinit var binding: FragmentEchoStepTwoBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentEchoStepTwoBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFabEcho()
    }


    private fun setupFabEcho() {
        binding.fab.setOnClickListener {
            viewModel.enableEcho()
            viewModel.setupConcluded = true
            findNavController().navigate(EchoStepTwoFragmentDirections.toEchoIsEnabled())
        }
    }


}


