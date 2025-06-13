package dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.setup_flow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentEchoStepOneBinding
import dev.gmarques.controledenotificacoes.databinding.ItemAppSmartWatchBinding
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_managed_apps.ContainerController
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 *Criado por Gilian Marques
 * Em 12/06/2025 as 14:29
 */
@AndroidEntryPoint
class EchoStepOneFragment : MyFragment() {

    private val viewModel: EchoFlowSharedViewModel by viewModels()
    private lateinit var binding: FragmentEchoStepOneBinding
    private val manageAppsViewsMutex = Mutex()
    private lateinit var containerController: ContainerController

    @Inject
    lateinit var getInstalledAppIconUseCase: GetInstalledAppIconUseCase

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
        containerController = ContainerController(viewLifecycleOwner, binding.llConteinerApps)
        binding.tvIntroduction.text =
            getString(R.string.Configure_o_app_do_seu_smartwatch_para_emitir_notifica_es_apenas_de, getString(R.string.app_name))
        setupFabEcho()
        loadSmartWatchApps()
        observeStates()
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
                is EchoState.StepOne.SmartWatchApps -> loadAppsViews(state.apps)
            }
        }
    }

    private fun loadAppsViews(apps: List<InstalledApp>) = lifecycleScope.launch {
        manageAppsViewsMutex.withLock {

            val children = apps.map { app ->

                val itemBinding = ItemAppSmartWatchBinding.inflate(layoutInflater).apply {
                    tvName.text = getString(R.string.Abrir_X_app, app.name)
                    ivAppIcon.setImageDrawable(getInstalledAppIconUseCase(app.packageId))
                    tvName.setOnClickListener(AnimatedClickListener {
                        val launched = requireMainActivity().launchApp(app.packageId)
                        if (!launched) showErrorSnackBar(getString(R.string.Nao_foi_poss_vel_abrir_o_app), binding.fab)
                        else lifecycleScope.launch {
                            delay(500)
                            binding.fab.isVisible = true
                            binding.tvAppNotFound.isVisible = false
                        }
                    })
                }

                ContainerController.Child(app.packageId, app.name, itemBinding)
            }

            containerController.submitList(children)
        }
    }


}
