package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app

import android.app.PendingIntent
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
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
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.pendingIntentId
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nameOrDescription
import dev.gmarques.controledenotificacoes.framework.PendingIntentCache
import dev.gmarques.controledenotificacoes.framework.model.ShakeDetectorHelper
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.dialogs.ConfirmRuleRemovalDialog
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule.SelectRuleFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule.SelectRuleFragment.Companion.BUNDLED_RULE_KEY
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.DomainRelatedExtFuns.getAdequateIconReferenceSmall
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.setStartDrawable
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ViewManagedAppFragment() : MyFragment() {

    private val viewModel: ViewManagedAppViewModel by viewModels()
    private lateinit var binding: FragmentViewManagedAppBinding
    private val args: ViewManagedAppFragmentArgs by navArgs()

    @Inject
    lateinit var shakeDetector: ShakeDetectorHelper
    private lateinit var adapter: AppNotificationAdapter
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

        pkg?.let { appIcon = viewModel.loadAppIcon(pkg, requireContext()) }

        observeRuleChanges()
        observeNotificationHistory()
        observeEvents()
        setupRecyclerView()
        setupFabOpenApp()
        setupSelectRuleListener()
    }

    /**atua nas animaçoes do lottie e vibração*/
    private fun toggleEmptyState(enabled: Boolean) {

        binding.lottieView.isVisible = enabled
        binding.tvHint.isVisible = enabled

        if (enabled) shakeDetector.start {
            with(binding) {
                if (!lottieView.isAnimating) {
                    lottieView.playAnimation()
                    vibrator.sineAnimation()
                }
            }
        } else shakeDetector.stop()

    }

    private fun setupFabOpenApp() = with(binding) {

        hideViewOnRVScroll(rvHistory, fabOpenApp)
        fabOpenApp.setOnClickListener(AnimatedClickListener {

            val packageId = viewModel.managedAppFlow.value!!.packageId
            val launched = requireMainActivity().launchApp(packageId)
            if (!launched) showErrorSnackBar(getString(R.string.Nao_foi_poss_vel_abrir_o_app), fabOpenApp)

        })
    }

    private fun setupActionBar(app: ManagedAppWithRule) = with(binding) {

        val drawable = ContextCompat.getDrawable(requireActivity(), app.rule.getAdequateIconReferenceSmall())
            ?: error("O icone deve existir pois é um recurso interno do app")

        tvAppName.text = app.name
        tvRuleName.text = app.rule.nameOrDescription()
        tvRuleName.setStartDrawable(drawable)

        lifecycleScope.launch {
            Glide.with(binding.ivAppIcon.context).load(appIcon).transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.vec_app).into(binding.ivAppIcon)
        }

        ivGoBack.setOnClickListener(AnimatedClickListener {
            goBack()
        })

        ivMenu.setOnClickListener(AnimatedClickListener {
            showMenu()
        })

    }

    private fun setupRecyclerView() = with(binding) {
        adapter = AppNotificationAdapter(appIcon, ::onNotificationClick)
        rvHistory.adapter = adapter
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.setHasFixedSize(true)
        hideViewOnRVScroll(binding.rvHistory, binding.fabOpenApp)

    }

    private fun onNotificationClick(notification: AppNotification) {
        try {
            val originalPendingIntent: PendingIntent? = PendingIntentCache(notification.pendingIntentId())
            originalPendingIntent?.send()
        } catch (_: Exception) {
            PendingIntentCache.remove(notification.pendingIntentId())
            showErrorSnackBar(getString(R.string.Nao_foi_possivel_abrir_a_notifica_o), binding.fabOpenApp)
        }
    }

    private fun showMenu() {
        val popupMenu = popupMenu {
            if (!viewModel.notFoundApp) section {
                title = getString(R.string.Notificacoes)
                item {
                    label = getString(R.string.Limpar_historico)
                    icon = R.drawable.vec_try_again
                    callback = {
                        confirmClearHistory()
                    }
                }
            }

            section {
                title = getString(R.string.App)
                item {
                    label = getString(R.string.Remover_app)
                    icon = R.drawable.vec_remove
                    callback = {
                        confirmRemoveApp()
                    }
                }
            }

            if (!viewModel.notFoundApp) section {
                title = getString(R.string.Regras)

                item {
                    label = getString(R.string.Trocar_regra)
                    icon = R.drawable.vec_change_rule
                    callback = {
                        navigateToSelectRule()
                    }
                }

                item {
                    label = getString(R.string.Editar_regra)
                    icon = R.drawable.vec_edit
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

        popupMenu.show(this@ViewManagedAppFragment.requireContext(), binding.ivMenu)
    }

    private fun confirmClearHistory() {
        MaterialAlertDialogBuilder(requireActivity()).setTitle(getString(R.string.Por_favor_confirme))
            .setMessage(getString(R.string.Deseja_mesmo_apagar_o_hist_rico_de_notifica_es_deste_app_essa_acao_nao))
            .setPositiveButton(getString(R.string.Apagar)) { dialog, _ ->
                viewModel.clearHistory()
            }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
            }.setCancelable(false).setIcon(R.drawable.vec_alert).show()
    }

    private fun confirmRemoveApp() {
        viewModel.managedAppFlow.value
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Por_favor_confirme)).setMessage(
            getString(R.string.Deseja_mesmo_remover_este_aplicativo_da_lista_de_gerenciamento)
        ).setPositiveButton(
            getString(R.string.Remover), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    viewModel.deleteApp()
                }
            }).setNegativeButton(
            getString(R.string.Cancelar), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                }
            }).setCancelable(false).setIcon(R.drawable.vec_alert).show()
    }

    private fun navigateToEditRule() {
        findNavController().navigate(ViewManagedAppFragmentDirections.toAddRuleFragment(viewModel.managedAppFlow.value!!.rule))
    }

    private fun setupSelectRuleListener() {

        setFragmentResultListener(SelectRuleFragment.RESULT_LISTENER_KEY) { _, bundle ->
            val rule = requireSerializableOf(bundle, BUNDLED_RULE_KEY, Rule::class.java)
            viewModel.updateAppsRule(rule!!)
        }

    }

    private fun navigateToSelectRule() {
        findNavController().navigate(ViewManagedAppFragmentDirections.toSelectRuleFragment())
    }

    private fun confirmRemoveRule() {
        ConfirmRuleRemovalDialog(this@ViewManagedAppFragment, viewModel.managedAppFlow.value!!.rule) {
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
            toggleEmptyState(history.isEmpty())
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

                Event.AppRemoved -> goBack()

            }
        }
    }

    override fun onPause() {
        toggleEmptyState(false)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toggleEmptyState(false)
    }

}