package dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dev.gmarques.controledenotificacoes.databinding.FragmentEchoStepOneBinding
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.setup_flow.EchoFlowSharedViewModel
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.setup_flow.EchoState

/**
 *Criado por Gilian Marques
 * Em 12/06/2025 as 14:29
 */
class EchoStepOneFragment : MyFragment() {

    private val viewModel: EchoFlowSharedViewModel by viewModels()
    private lateinit var binding: FragmentEchoStepOneBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentEchoStepOneBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFabEcho()
        loadSmartWatchApps()
    }

    private fun loadSmartWatchApps() {
        viewModel.loadSmartWatchApps()
    }

    private fun setupFabEcho() {
        binding.fab.setOnClickListener {
            findNavController().navigate(EchoStepOneFragmentDirections.toEchoStepTwoFragment())
        }
    }

    /**
     * Observa os estados da UI disparados pelo viewmodel chamando a função adequada para cada estado.
     * Utiliza a função collectFlow para coletar os estados do flow de forma segura e sem repetições de código.
     */
    private fun observeStates() {
        collectFlow(viewModel.statesFlow) { state ->
            when (state) {
                EchoState.Idle -> {}
                is EchoState.StepOne.SmartWatchApps -> loadAppsViews()
            }
        }
    }

    private fun loadAppsViews() {


    }

}
