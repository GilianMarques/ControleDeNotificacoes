package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_managed_apps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule.AddOrUpdateRuleFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.SelectAppsFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule.SelectRuleFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule.SelectRuleFragment.Companion.BUNDLED_RULE_KEY
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.DomainRelatedExtFuns.getAdequateIconReference
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.addViewWithTwoStepsAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class AddManagedAppsFragment() : MyFragment() {
    private val maxAppsViews = 5

    companion object {
        fun newInstance(): AddManagedAppsFragment {
            return AddManagedAppsFragment()
        }
    }

    @Inject
    lateinit var getAllRulesUseCase: GetAllRulesUseCase

    @Inject
    lateinit var getRuleByIdUseCase: GetRuleByIdUseCase

    @Inject
    lateinit var getInstalledAppIconUseCase: GetInstalledAppIconUseCase

    private val viewModel: AddManagedAppsViewModel by viewModels()
    private val args: AddManagedAppsFragmentArgs by navArgs()
    private lateinit var binding: FragmentAddManagedAppsBinding

    private val manageAppsViewsMutex = Mutex()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentAddManagedAppsBinding.inflate(inflater, container, false).also {
            binding = it
            setupActionBar(binding.toolbar)
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar(binding.toolbar)
        setupFlowToChangeRule()
        setupSelectAppsListener()
        setupSelectRuleListener()
        setupSelectAppsButton()
        setupAddRuleButton()
        setupConcludeFab()
        observeViewModel()
        showHintDialog(PreferencesImpl.showHintHowRulesAndManagedAppsWork, getString(R.string.como_adicionar_o_primeiro_app))
        loadLastUsedOrAddedRule()
    }

    /**
     * O usuario pode apagar a regra que esta atualmente selecionada, se acontecer, esse função remove a seleçao
     * feita para impedir que o app salve uma regra que ja nao existe em um app, causando um problema gigantesco.
     */
    private fun removeSelectedRulIfItWasDeletedByUser() {
        viewModel.selectedRule.value?.id?.let {
            runBlocking {
                if (getRuleByIdUseCase(it) == null) viewModel.resetRule()
            }
        }
    }

    /**
     * Prepara o fluxo que é executado quando o usuário abre este fragmento para alterar a regra de um aplicativo específico.
     *
     * Esta função é chamada quando o fragmento é iniciado com um argumento `selectedAppPkg` (ID do pacote do aplicativo).
     * Se um aplicativo válido for fornecido, ele carrega o aplicativo no `viewModel` e, opcionalmente, navega automaticamente
     * para a tela de seleção ou adição de regra.
     */
    private fun setupFlowToChangeRule() {
        args.selectedAppPkg?.let {
            if (it == InstalledApp.NOT_FOUND_APP_PKG) {
                findNavController().popBackStack()
                return@let
            }

            viewModel.loadAppToChangeRule(it)

            if (viewModel.autoOpenSelectionRuleFragment) {
                viewModel.autoOpenSelectionRuleFragment = false
                navigateToAddOrSelectRule()
            }
        }
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

        removeSelectedRulIfItWasDeletedByUser()

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

    private fun setupConcludeFab() = with(binding) {
        fabConclude.setOnClickListener(AnimatedClickListener {
            fabConclude.isEnabled = false
            viewModel.validateSelection()

        })
    }

    private fun setupSelectAppsButton() = with(binding) {

        tvAddApp.setOnClickListener(AnimatedClickListener {


            findNavController().navigate(
                AddManagedAppsFragmentDirections.toSelectAppsFragment(viewModel.getSelectedPackages()),
                FragmentNavigatorExtras(
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

        setFragmentResultListener(SelectAppsFragment.RESULT_KEY) { _, bundle ->

            @Suppress("UNCHECKED_CAST") val selectedApps = requireSerializableOf(
                bundle,
                SelectAppsFragment.BUNDLED_PACKAGES_KEY,
                ArrayList::class.java
            ) as ArrayList<InstalledApp>

            lifecycleScope.launch {

                val preselection = viewModel.selectedApps.value?.size ?: 0
                var awaitUntilAllPreSelectedAppsAreLoadedOnUi = preselection > 0

                while (awaitUntilAllPreSelectedAppsAreLoadedOnUi) {
                    delay(10)
                    awaitUntilAllPreSelectedAppsAreLoadedOnUi = binding.llConteinerApps.childCount < preselection
                }

                viewModel.addNewlySelectedApps(selectedApps)
            }

        }
    }

    private fun setupAddRuleButton() = with(binding) {

        tvAddRule.setOnClickListener(AnimatedClickListener {
            navigateToAddRule()
        })
    }

    private fun navigateToAddOrSelectRule() = with(binding) {
        lifecycleScope.launch {
            if (getAllRulesUseCase().isEmpty()) navigateToAddRule()
            else navigateToSelectRule()
        }
    }

    private fun navigateToSelectRule() = with(binding) {
        findNavController().navigate(
            AddManagedAppsFragmentDirections.toSelectRuleFragment(),
            FragmentNavigatorExtras(
                fabConclude to fabConclude.transitionName,
                llRule to llRule.transitionName,
                tvRuleTittle to tvRuleTittle.transitionName,
            )
        )
    }

    private fun navigateToAddRule() = with(binding) {
        findNavController().navigate(
            AddManagedAppsFragmentDirections.toAddRuleFragment(),
            FragmentNavigatorExtras(
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
            /**Garante que a regra será imediatamenta aplicada ao pp recem adicionado*/
            NotificationListener.sendBroadcastToReadActiveNotifications()
            vibrator.success()
            goBack()
        }

    }

    /**
     * Gerencia as views que representam os aplicativos instalados dentro de um layout pai.
     *
     * Esta função atualiza a interface do usuário de forma eficiente para refletir as mudanças na lista
     * de aplicativos instalados. Ela remove as views de aplicativos que não estão mais presentes e adiciona
     * novas views para aplicativos recém-instalados. As novas views são animadas ao serem adicionadas para
     * proporcionar uma experiência mais agradável.
     *
     * @param apps Um mapa onde a chave é o ID do pacote do aplicativo instalado (String) e o valor é um
     *             objeto [InstalledApp] contendo os detalhes do aplicativo (nome, ícone, etc.).
     * @see InstalledApp
     * @see addViewWithTwoStepsAnimation
     *
     * **Fluxo da Função:**
     *
     * 1. **Remoção de Views Desatualizadas:**
     *    - Itera sobre as views filhas existentes dentro do layout `parent` (binding.llConteinerApps).
     *    - Identifica as views cujo `tag` (que representa o ID do pacote do aplicativo) *não* está presente nas chaves do mapa `apps`.
     *    - Remove essas views do layout `parent`, pois os aplicativos correspondentes não estão mais instalados.
     *
     * 2. **Adição de Novas Views:**
     *    - Itera sobre os valores do mapa `apps` (que representam os aplicativos instalados atualmente).
     *    - Para cada aplicativo, verifica se uma view correspondente já existe no layout `parent`, comparando o `tag` das views existentes com o ID do pacote do aplicativo.
     *    - Se uma view com o `tag` correspondente *não* for encontrada (o que significa que o aplicativo é novo ou foi removido anteriormente), uma nova view é criada.
     *    - Infla um layout `ItemAppSmallBinding`.
     *    - Define o nome e o ícone do aplicativo na nova view usando `app.name` e `app.icon`.
     *    - Define o ID do pacote do aplicativo como o `tag` da view para referência futura.
     *    - Adiciona a nova view ao layout `parent` usando a função de extensão `addViewWithTwoStepsAnimation` para uma adição visualmente atraente.
     */
    private fun manageAppsViews(apps: Map<String, InstalledApp>) = lifecycleScope.launch {
        manageAppsViewsMutex.withLock {
            Log.d("USUK", "AddManagedAppsFragment.manageAppsViews: ${binding.llConteinerApps.childCount} apps: ${apps.size}")
            binding.tvExtraApps.text = ""
            val parent = binding.llConteinerApps

            /* remova o `toList()` e veja sua vida se transformar em um inferno! Brincadeiras a parte, deve-se criar
            uma lista de views a remover primeiro e só depois remova-las pra evitar inconsistencias na ui */
            parent.children
                .filter { it.tag !in apps.keys }
                .toList().also { Log.d("USUK", "AddManagedAppsFragment.manageAppsViews: removing ${it.size} views from screen") }
                .forEach {
                    parent.removeView(it)
                }
            // TODO: nem sempre as views sao removidas

            apps.values.sortedBy { it.name }
                .forEachIndexed { index, app ->
                    if (index >= maxAppsViews) {
                        binding.tvExtraApps.text = getString(R.string.Mais_x_apps, apps.size - maxAppsViews)
                        return@forEachIndexed
                    }
                    if (!parent.children.none { it.tag == app.packageId }) return@forEachIndexed

                    with(ItemAppSmallBinding.inflate(layoutInflater)) {
                        name.text = app.name
                        ivAppIcon.setImageDrawable(getInstalledAppIconUseCase(app.packageId))
                        root.tag = app.packageId
                        if (viewModel.changingRule) ivRemove.isGone = true
                        else ivRemove.setOnClickListener(AnimatedClickListener {
                            viewModel.deleteApp(app)
                        })
                        parent.addView(root, min(index, parent.childCount))
                        Log.d("USUK", "AddManagedAppsFragment.manageAppsViews: adding index $index pkg ${app.name}")
                    }
                }
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