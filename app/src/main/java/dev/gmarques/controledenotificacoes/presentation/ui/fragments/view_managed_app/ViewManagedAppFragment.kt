package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app

import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentViewManagedAppBinding
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nameOrDescription
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.dialogs.ConfirmRuleRemovalDialog
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.DomainRelatedExtFuns.getAdequateIconReferenceSmall
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.setStartDrawable
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentViewManagedApp() : MyFragment() {

    companion object {
        fun newInstance(): FragmentViewManagedApp {
            return FragmentViewManagedApp()
        }
    }

    private lateinit var adapter: AppNotificationAdapter

    private val viewModel: ViewManagedAppViewModel by viewModels()
    private lateinit var binding: FragmentViewManagedAppBinding
    private val args: FragmentViewManagedAppArgs by navArgs()
    private lateinit var appIcon: Drawable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentViewManagedAppBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pkg = if (args.packageId != null) {
            viewModel.setup(args.packageId!!)
            args.packageId!!
        } else if (args.app != null) {
            viewModel.setup(args.app!!)
            args.app!!.packageId
        } else {
            goBack()
            null
        }


        pkg?.let { appIcon = viewModel.loadAppIcon(pkg, requireActivity()) }

        observeRuleChanges()
        observeNotificationHistory()
        observeEvents()
        setupRecyclerView()
        setupFabOpenApp()

    }

    private fun setupFabOpenApp() = with(binding) {

        hideViewOnRVScroll(rvHistory, fabOpenApp)
        fabOpenApp.setOnClickListener(AnimatedClickListener {
            val packageId = viewModel.managedAppFlow.value!!.packageId
            val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(packageId)

            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                showErrorSnackBar(getString(R.string.Nao_foi_poss_vel_abrir_o_app))
            }
        })
    }

    private fun setupActionBar(app: ManagedAppWithRule) = with(binding) {

        val drawable = ContextCompat.getDrawable(requireActivity(), app.rule.getAdequateIconReferenceSmall())
            ?: error("O icone deve existir pois é um recurso interno do app")

        tvAppName.text = app.name
        tvRuleName.text = app.rule.nameOrDescription()
        tvRuleName.setStartDrawable(drawable)

        lifecycleScope.launch {
            Glide.with(binding.ivAppIcon.context)
                .load(appIcon)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.vec_app)
                .into(binding.ivAppIcon)
        }

        ivGoBack.setOnClickListener(AnimatedClickListener {
            goBack()
        })

        ivMenu.setOnClickListener(AnimatedClickListener {
            showMenu()
        })

    }

    private fun setupRecyclerView() = with(binding) {
        adapter = AppNotificationAdapter(appIcon)
        rvHistory.adapter = adapter
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.setHasFixedSize(true)
        hideViewOnRVScroll(binding.rvHistory, binding.fabOpenApp)

    }

    private fun showMenu() {
        val popupMenu = popupMenu {

            section {
                item {
                    label = getString(R.string.Limpar_historico)
                    icon = R.drawable.vec_try_again
                    callback = {
                        confirmClearHistory()
                    }
                }
            }

            section {
                item {
                    label = getString(R.string.Remover_app)
                    icon = R.drawable.vec_remove
                    callback = {
                        confirmRemoveApp()
                    }
                }
            }

            section {
                title = getString(R.string.Regras)

                item {
                    label = getString(R.string.Editar_regra)
                    icon = R.drawable.vec_edit_rule
                    callback = {
                        navigateToEditRule()
                    }
                }

                item {
                    label = getString(R.string.Remover_regra)
                    icon = R.drawable.vec_remove
                    callback = {
                        confirmRemoveRule()
                    }
                }


            }

        }
        popupMenu.show(this@FragmentViewManagedApp.requireContext(), binding.ivMenu)
    }

    private fun confirmClearHistory() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.Por_favor_confirme))
            .setMessage(getString(R.string.Deseja_mesmo_apagar_o_hist_rico_de_notifica_es_deste_app_essa_acao_nao))
            .setPositiveButton(getString(R.string.Apagar)) { dialog, _ ->
                viewModel.clearHistory()
            }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
            }.setCancelable(false)
            .setIcon(R.drawable.vec_alert)
            .show()
    }

    private fun confirmRemoveApp() {
        viewModel.managedAppFlow.value
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.Por_favor_confirme))
            .setMessage(
                getString(R.string.Deseja_mesmo_remover_este_aplicativo_da_lista_de_gerenciamento)
            )
            .setPositiveButton(
                getString(R.string.Remover),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        viewModel.deleteApp()
                    }
                })
            .setNegativeButton(
                getString(R.string.Cancelar),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }
                })
            .setCancelable(false)
            .setIcon(R.drawable.vec_alert)
            .show()
    }

    private fun navigateToEditRule() {
        findNavController().navigate(FragmentViewManagedAppDirections.toAddRuleFragment(viewModel.managedAppFlow.value!!.rule))
    }

    private fun confirmRemoveRule() {
        ConfirmRuleRemovalDialog(this@FragmentViewManagedApp, viewModel.managedAppFlow.value!!.rule) {
            viewModel.deleteRule()
        }
    }

    private fun observeRuleChanges() {
        collectFlow(viewModel.managedAppFlow) { app ->
            app?.let {
                setupActionBar(app)
            }
        }
    }

    private fun observeNotificationHistory() {
        collectFlow(viewModel.appNotificationHistoryFlow) { history ->
            adapter.submitList(history)
        }
    }

    /**
     * Observa os estados da UI disparados pelo viewmodel chamando a função adequada para cada estado.
     * Utiliza a função collectFlow para coletar os estados do flow de forma segura e sem repetições de código.
     */
    private fun observeEvents() {
        collectFlow(viewModel.eventsFlow) { event ->
            when (event) {
                is Event.FinishWithSuccess -> {
                    vibrator.success()
                    goBack()
                }
            }
        }
    }

}