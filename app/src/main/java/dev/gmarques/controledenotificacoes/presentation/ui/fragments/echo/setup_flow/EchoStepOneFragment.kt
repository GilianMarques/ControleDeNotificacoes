package dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.setup_flow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    private val viewModel: EchoFlowSharedViewModel by navGraphViewModels(R.id.nav_graph_echo) {
        defaultViewModelProviderFactory
    }
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
        containerController = ContainerController(viewLifecycleOwner, binding.llContainerApps)
        binding.tvIntroduction.text =
            getString(R.string.Configure_o_app_do_seu_smartwatch_para_emitir_notifica_es_apenas_de, getString(R.string.app_name))
        setupFabEcho()
        loadSmartWatchApps()
        observeStates()
        setupNotFoundAppTextView()
        setupEmptyView()
        if (viewModel.makeStepOnFabVisible) makeFabVisible()
    }

    private fun setupEmptyView() {
        binding.emptyView.setOnClickListener(AnimatedClickListener {
            showAppNotFoundDialog()
        })
    }

    private fun setupNotFoundAppTextView() {
        binding.tvAppNotInList.setOnClickListener(AnimatedClickListener {
            showAppNotFoundDialog()
        })
    }

    private fun showAppNotFoundDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.Ligar_Echo))
            .setMessage(getString(R.string.Abra_o_aplicativo_do_seu_rel_gio_manualmente_para_fazer_a_configura_o_e_volte))
            .setPositiveButton(getString(R.string.Entendi)) { dialog, _ ->
                makeFabVisible()
            }.setCancelable(false)
            .setIcon(R.drawable.vec_echo)
            .show()
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

        if (apps.isEmpty()) {
            binding.emptyView.isVisible = true
            binding.tvAppNotInList.isVisible = false
        } else manageAppsViewsMutex.withLock {

            val children = apps.map { app ->

                val itemBinding = ItemAppSmartWatchBinding.inflate(layoutInflater)
                    .apply {
                        tvName.text = getString(R.string.Abrir_X_app, app.name)
                        ivAppIcon.setImageDrawable(getInstalledAppIconUseCase(app.packageId))
                        tvName.setOnClickListener(AnimatedClickListener {
                            val launched = requireMainActivity().launchApp(app.packageId)
                            if (!launched) showErrorSnackBar(getString(R.string.Nao_foi_poss_vel_abrir_o_app), binding.fab)
                            else lifecycleScope.launch {
                                delay(500)
                                makeFabVisible()
                            }
                        })
                    }

                ContainerController.Child(app.packageId, app.name, itemBinding)
            }

            containerController.submitList(children)
        }
    }

    private fun makeFabVisible() {
        binding.fab.isVisible = true
        binding.tvAppNotInList.isVisible = false
        viewModel.makeStepOnFabVisible = true
    }

}
