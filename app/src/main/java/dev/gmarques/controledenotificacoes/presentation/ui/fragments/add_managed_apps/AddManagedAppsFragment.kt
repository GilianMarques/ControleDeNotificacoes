package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_managed_apps

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.databinding.FragmentAddManagedAppsBinding
import dev.gmarques.controledenotificacoes.databinding.ItemAppSmallBinding
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleNameUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps.SelectAppsFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule.SelectRuleFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.DomainRelatedExtFuns.getAdequateIconReference
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.addViewWithTwoStepsAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class AddManagedAppsFragment() : MyFragment() {

    companion object {
        fun newInstance(): AddManagedAppsFragment {
            return AddManagedAppsFragment()
        }
    }

    @Inject
    lateinit var generateRuleNameUseCase: GenerateRuleNameUseCase

    private val viewModel: AddManagedAppsViewModel by viewModels()
    private lateinit var binding: FragmentAddManagedAppsBinding

    private val manageAppsViewsMutex = Mutex()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddManagedAppsBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar(binding.toolbar)
        setupSelectAppsListener()
        setupSelectRuleListener()
        setupSelectAppsButton()
        setupSelectRuleButton()
        setupConcludeFab()
        observeViewModel()
    }

    private fun setupConcludeFab() = with(binding) {
        fabConclude.setOnClickListener(AnimatedClickListener {
            viewModel.validateSelection()
        })
    }

    private fun setupSelectAppsButton() = with(binding) {

        ivAddApp.setOnClickListener(AnimatedClickListener {


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

        @Suppress("UNCHECKED_CAST") setFragmentResultListener(SelectAppsFragment.RESULT_KEY) { _, bundle ->
            val selectedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(
                    SelectAppsFragment.BUNDLED_PACKAGES_KEY, ArrayList::class.java
                ) as ArrayList<InstalledApp>
            } else {
                @Suppress("DEPRECATION") bundle.getSerializable(SelectAppsFragment.BUNDLED_PACKAGES_KEY) as ArrayList<InstalledApp>
            }

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

    private fun setupSelectRuleButton() = with(binding) {

        ivAddRule.setOnClickListener(AnimatedClickListener {


            findNavController().navigate(
                AddManagedAppsFragmentDirections.toSelectRuleFragment(),
                FragmentNavigatorExtras(
                    fabConclude to fabConclude.transitionName,
                    llRule to llRule.transitionName,
                    tvRuleTittle to tvRuleTittle.transitionName,
                )
            )
        })

    }

    private fun setupSelectRuleListener() {

        setFragmentResultListener(SelectRuleFragment.RESULT_KEY) { key, bundle ->
            val rule = requireSerializableOf(bundle, key, Rule::class.java)
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
        }

        viewModel.successCloseFragment.observe(viewLifecycleOwner) {
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

            val parent = binding.llConteinerApps

            /* remova o `toList()` e veja sua vida se transformar em um inferno! Brincadeiras a parte, deve-se criar
            uma lista de views a remover primeiro e só depois remova-las pra evitar inconsistencias na ui */
            parent.children
                .filter { it.tag !in apps.keys }
                .toList()
                .forEach {
                    parent.removeView(it)
                }

            apps.values.sortedBy { it.name }.forEachIndexed { index, app ->
                if (!parent.children.none { it.tag == app.packageId }) return@forEachIndexed
                with(ItemAppSmallBinding.inflate(layoutInflater)) {
                    name.text = app.name
                    ivAppIcon.setImageDrawable(app.icon)
                    root.tag = app.packageId
                    ivRemove.setOnClickListener(AnimatedClickListener {
                        viewModel.removeApp(app)

                    })
                    parent.addView(root, min(index, parent.childCount))
                }
            }
        }
    }

    private fun manageRuleView(rule: Rule) = with(binding) {
        lifecycleScope.launch {
            tvSelectedRule.text = rule.name.ifBlank { generateRuleNameUseCase(rule) }
            tvSelectedRule.setCompoundDrawablesWithIntrinsicBounds(
                ResourcesCompat.getDrawable(resources, rule.getAdequateIconReference(), requireActivity().theme),
                null,
                null,
                null
            )
            tvSelectedRule.isVisible = true

        }
    }
}
