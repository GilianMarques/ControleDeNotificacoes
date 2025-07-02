package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_managed_apps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.databinding.FragmentAddManagedAppsBinding
import dev.gmarques.controledenotificacoes.databinding.ItemAppSmallBinding
import dev.gmarques.controledenotificacoes.databinding.ItemRuleSmallBinding
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nameOrDescription
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetAllRulesUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule.AddOrUpdateRuleFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.SelectAppsFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_notification.SelectNotificationFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule.SelectRuleFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule.SelectRuleFragment.Companion.BUNDLED_RULE_KEY
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.DomainRelatedExtFuns.getAdequateIconReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class AddManagedAppsFragment() : MyFragment() {

    private val maxAppsViews = 5

    @Inject
    lateinit var getAllRulesUseCase: GetAllRulesUseCase

    @Inject
    lateinit var getRuleByIdUseCase: GetRuleByIdUseCase

    @Inject
    lateinit var getInstalledAppIconUseCase: GetInstalledAppIconUseCase

    private val viewModel: AddManagedAppsViewModel by viewModels()
    private lateinit var binding: FragmentAddManagedAppsBinding

    private val manageAppsViewsMutex = Mutex()
    private lateinit var containerController: ContainerController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentAddManagedAppsBinding.inflate(inflater, container, false).also {
            binding = it
            setupActionBar(binding.actionbar)
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar(binding.actionbar)
        containerController = ContainerController(viewLifecycleOwner, binding.llContainerApps, maxAppsViews)

        setupSelectAppsListener()
        setupSelectRuleListener()
        setupSelectActiveNotificationListener()
        setupSelectAppsButton()
        setupSelectNotificationsButton()
        setupAddRuleButton()
        setupConcludeFab()
        observeViewModel()
        showHintDialog(PreferencesImpl.showHintHowRulesAndManagedAppsWork, getString(R.string.como_adicionar_o_primeiro_app))
        loadLastUsedOrAddedRule()
    }

    /**
     * Esta função tenta carregar a última regra selecionada pelo usuário a partir das preferências.
     * Se uma regra válida for encontrada, ela é definida no `viewModel`.
     *
     * Caso nenhuma regra tenha sido selecionada anteriormente, a função carrega a ultima regra adicionada pelo usuario.
     *
     * O carregamento é realizado de forma assíncrona dentro do escopo do ciclo de vida do fragmento.
     */
    private fun loadLastUsedOrAddedRule() = lifecycleScope.launch {

        removeSelectedRuleIfItWasDeletedByUser()

        if (viewModel.selectedRule.value != null) return@launch

        val pref = PreferencesImpl.lastSelectedRule
        if (!pref.isDefault()) {
            getRuleByIdUseCase(pref.value).let { rule ->
                if (rule == null) pref.reset()
                else viewModel.setRule(rule)
                return@launch
            }
        }

        val rules = getAllRulesUseCase()
        if (!rules.isEmpty()) viewModel.setRule(rules.last())
    }

    /**
     * O usuario pode apagar a regra que esta atualmente selecionada, se acontecer, esse função remove a seleçao
     * feita para impedir que o app salve uma regra que ja nao existe em um app, causando um problema gigantesco.
     */
    private suspend fun removeSelectedRuleIfItWasDeletedByUser() {
        viewModel.selectedRule.value?.id?.let {
            if (getRuleByIdUseCase(it) == null) viewModel.setRule(null)
        }
    }

    private fun setupConcludeFab() = with(binding) {
        fabConclude.setOnClickListener(AnimatedClickListener {
            fabConclude.isEnabled = false
            viewModel.validateSelection()

        })
    }

    private fun setupSelectAppsButton() = with(binding) {

        tvAddApp.setOnClickListener(AnimatedClickListener {


            findNavController().navigate(
                AddManagedAppsFragmentDirections.toSelectAppsFragment(viewModel.getSelectedPackages()), FragmentNavigatorExtras(
                    fabConclude to fabConclude.transitionName
                )
            )

        })

    }

    private fun setupSelectNotificationsButton() = with(binding) {

        tvSelectNotification.setOnClickListener(AnimatedClickListener {

            findNavController().navigate(
                AddManagedAppsFragmentDirections.toSelectNotificationFragment(), FragmentNavigatorExtras(
                    fabConclude to fabConclude.transitionName
                )
            )

        })

    }

    /**
     * Configura um listener para receber o resultado do `SelectAppsFragment`.
     *
     * Este listener é acionado quando o `SelectAppsFragment` envia um resultado via `setFragmentResult`,
     * contendo uma lista de aplicativos selecionados (`ArrayList<InstalledApp>`).
     *
     * Ele extrai a lista de apps do bundle recebido, tratando as diferenças entre as versões do Android
     * (API 33+ vs. anteriores), e então passa essa lista para `viewModel.setApps`.
     *
     * O listener também aguarda até que todos os aplicativos pré-selecionados sejam carregados na UI,
     * garantindo que a lista seja atualizada corretamente.
     *
     * O listener identifica o resultado usando a chave `SelectAppsFragment.RESULT_KEY`,
     * e os apps selecionados são armazenados no bundle com a chave `SelectAppsFragment.BUNDLED_SELECTED_APPS_KEY`.
     *
     * @see SelectAppsFragment
     * @see InstalledApp
     */
    private fun setupSelectAppsListener() {

        setFragmentResultListener(SelectAppsFragment.RESULT_LISTENER_KEY) { _, bundle ->

            @Suppress("UNCHECKED_CAST") val selectedApps = requireSerializableOf(
                bundle, SelectAppsFragment.BUNDLED_PACKAGES_KEY, ArrayList::class.java
            ) as ArrayList<InstalledApp>
            lifecycleScope.launch {

                val preSelectedApps = viewModel.selectedApps.value?.size ?: 0
                val awaitUntilAppsAreLoadedOnUi = preSelectedApps > 0

                if (awaitUntilAppsAreLoadedOnUi) do {
                    delay(100)
                } while (binding.llContainerApps.childCount < min(maxAppsViews, preSelectedApps))

                viewModel.addNewlySelectedApps(selectedApps)
            }

        }
    }

    private fun setupAddRuleButton() = with(binding) {

        tvAddRule.setOnClickListener(AnimatedClickListener {
            navigateToAddRule()
        })
    }

    private fun navigateToSelectRule() = with(binding) {
        findNavController().navigate(
            AddManagedAppsFragmentDirections.toSelectRuleFragment(), FragmentNavigatorExtras(
                fabConclude to fabConclude.transitionName,
                llRule to llRule.transitionName,
                tvRuleTittle to tvRuleTittle.transitionName,
            )
        )
    }

    private fun navigateToAddRule() = with(binding) {
        findNavController().navigate(
            AddManagedAppsFragmentDirections.toAddRuleFragment(), FragmentNavigatorExtras(
                tvRuleTittle to tvRuleTittle.transitionName,
                tvTargetApp to tvTargetApp.transitionName,
                appsContainer to appsContainer.transitionName,
                llRule to llRule.transitionName,
                tvTargetApp to tvTargetApp.transitionName,
                fabConclude to fabConclude.transitionName,
            )
        )
    }

    private fun setupSelectRuleListener() {

        setFragmentResultListener(SelectRuleFragment.RESULT_LISTENER_KEY) { _, bundle ->
            val rule = requireSerializableOf(bundle, BUNDLED_RULE_KEY, Rule::class.java)
            viewModel.setRule(rule!!)
        }

        setFragmentResultListener(AddOrUpdateRuleFragment.RESULT_LISTENER_KEY) { _, bundle ->
            val rule = requireSerializableOf(bundle, AddOrUpdateRuleFragment.RULE_KEY, Rule::class.java)
            viewModel.setRule(rule!!)
        }
    }


    private fun setupSelectActiveNotificationListener() {

        setFragmentResultListener(SelectNotificationFragment.RESULT_LISTENER_KEY) { _, bundle ->

            val pkgId = requireSerializableOf(
                bundle, SelectNotificationFragment.BUNDLED_PACKAGE_NAME_KEY, String()::class.java
            ) as String

            lifecycleScope.launch {
                viewModel.addSelectedAppByPkgId(pkgId)
            }

        }
    }

    private fun observeViewModel() {
        viewModel.selectedApps.observe(viewLifecycleOwner) { apps ->
            manageAppsViews(apps)
        }

        viewModel.selectedRule.observe(viewLifecycleOwner) { rule ->
            rule?.let { manageRuleView(rule) }
        }

        viewModel.showError.observe(viewLifecycleOwner) {

            it.consume()?.let {
                showErrorSnackBar(it, binding.fabConclude)
            }
            binding.fabConclude.isEnabled = true
        }

        viewModel.successCloseFragment.observe(viewLifecycleOwner) {
            goBack()
            vibrator.success()
        }

    }

    /**
     * Gerencia as views que representam os aplicativos instalados dentro de um layout pai.
     *
     * Esta função atualiza a interface do usuário de forma eficiente para refletir as mudanças na lista
     * de aplicativos instalados. Ela remove as views de aplicativos que não estão mais presentes e adiciona
     * novas views para aplicativos recém-instalados. As novas views são animadas ao serem adicionadas para
     * proporcionar uma experiência mais agradável.
     */
    private fun manageAppsViews(apps: Map<String, InstalledApp>) = lifecycleScope.launch {
        manageAppsViewsMutex.withLock {

            if (apps.size > maxAppsViews) {
                binding.tvExtraApps.text = getString(R.string.Mais_x_apps, apps.size - maxAppsViews)
            } else binding.tvExtraApps.text = ""

            val children = apps.values.map { app ->

                val itemBinding = ItemAppSmallBinding.inflate(layoutInflater).apply {
                    name.text = app.name
                    ivAppIcon.setImageDrawable(getInstalledAppIconUseCase(app.packageId))
                    ivRemove.setOnClickListener(AnimatedClickListener {
                        viewModel.deleteApp(app)
                    })
                }

                ContainerController.Child(app.packageId, app.name, itemBinding)
            }

            containerController.submitList(children)
        }
    }

    private fun manageRuleView(rule: Rule) = with(binding) {
        lifecycleScope.launch {

            llRuleContainer.removeAllViews()

            with(ItemRuleSmallBinding.inflate(layoutInflater)) {
                name.text = rule.nameOrDescription()
                ivAppIcon.setImageResource(rule.getAdequateIconReference())
                ivChange.setOnClickListener(AnimatedClickListener {
                    navigateToSelectRule()
                })
                llRuleContainer.addView(root)
            }
        }
    }

}