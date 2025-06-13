package dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentEchoIsEnabledBinding
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.setup_flow.EchoFlowSharedViewModel

/**
 *Criado por Gilian Marques
 * Em 12/06/2025 as 14:29
 */
@AndroidEntryPoint
class EchoIsEnabledFragment : MyFragment() {

    private val viewModel: EchoFlowSharedViewModel by navGraphViewModels(R.id.nav_graph_echo) {
        defaultViewModelProviderFactory
    }
    private lateinit var binding: FragmentEchoIsEnabledBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentEchoIsEnabledBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnBackPressedListener()
        setupFabTurnOffEcho()
        setupFabFinish()
    }

    private fun setupOnBackPressedListener() {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeFlow()
            }
        })


    }


    private fun setupFabTurnOffEcho() {
        if (viewModel.setupConcluded) binding.fabDisableEcho.isVisible = false
        binding.fabDisableEcho.setOnClickListener {
            viewModel.disableEcho()
            closeFlow()
        }
    }


    private fun setupFabFinish() {
        if (!viewModel.setupConcluded) binding.fabFinish.isVisible = false
        binding.fabFinish.setOnClickListener {
            closeFlow()
        }
    }

    /**Navega pra fora do navgraph atual*/
    private fun closeFlow() {
        // NavController externo (do container que hospeda o NavHostFragment)
        val externalNavController = NavHostFragment.findNavController(
            requireActivity().supportFragmentManager.primaryNavigationFragment!!
        )
        externalNavController.popBackStack()
    }

}
