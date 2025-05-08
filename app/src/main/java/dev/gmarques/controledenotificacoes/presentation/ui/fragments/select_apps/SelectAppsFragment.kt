package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.zawadz88.materialpopupmenu.popupMenu
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentSelectAppsBinding
import dev.gmarques.controledenotificacoes.databinding.ViewActivityHeaderBinding
import dev.gmarques.controledenotificacoes.domain.Preferences
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 09:50.
 */
@AndroidEntryPoint
class SelectAppsFragment : MyFragment() {

    companion object {
        const val RESULT_KEY = "selectAppsResult"
        const val BUNDLED_PACKAGES_KEY = "bundled_packages"
    }

    private var animatingFab = false
    private var isFabVisible = true

    @Inject
    lateinit var getInstalledAppIconUseCase: GetInstalledAppIconUseCase

    @Inject
    lateinit var getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase

    private lateinit var binding: FragmentSelectAppsBinding
    private val viewModel: SelectAppsViewModel by viewModels()
    private val args: SelectAppsFragmentArgs by navArgs()

    private lateinit var adapter: AppsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentSelectAppsBinding.inflate(inflater, container, false).also {
        binding = it
        setupActionBar(binding.toolbar)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getPreSelectedPackagesAndLoad()
        setupRecyclerView()
        setupSearch()
        observeStates()
        observeEvents()
        setupFabConclude()


    }

    override fun setupActionBar(binding: ViewActivityHeaderBinding) {
        super.setupActionBar(binding)
        binding.ivMenu.isVisible = true
        binding.ivMenu.setOnClickListener(AnimatedClickListener {

            showPopUpMenu(binding.ivMenu)
        })

    }

    /**
     * Carrega os pacotes de apps que devem ser excluidos da busca em
     * um hashset para otimizar o tempo de consulta. (O(1) ou O(n))
     * Por fim, dispara a busca de aplicativos para aplicar a seleção inicial.
     */
    private fun getPreSelectedPackagesAndLoad() {
        viewModel.preSelectedAppsToHide = args.excludePackages.toHashSet()
        viewModel.searchApps()
    }

    private fun setupFabConclude() = with(binding) {
        fabConclude.setOnClickListener(AnimatedClickListener {
            viewModel.validateSelection()
        })

    }

    private fun setupSearch() {
        binding.tietSearch.doOnTextChanged { text, _, _, _ ->
            adapter.submitList(viewModel.installedApps.value, text.toString().trim())
        }
    }

    private fun setupRecyclerView() {

        adapter = AppsAdapter(getInstalledAppIconUseCase) { app, checked ->

            viewModel.onAppChecked(app, checked)

            isFabVisible = true
            toggleFabVisibility(true)
        }

        binding.rvApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SelectAppsFragment.adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && isFabVisible) {
                        isFabVisible = false
                        toggleFabVisibility(false)
                    } else if (dy < 0 && !isFabVisible) {
                        isFabVisible = true
                        toggleFabVisibility(true)
                    }
                }
            })
        }

    }

    /**
     * Alterna a visibilidade do Floating Action Button (FAB) com uma animação de transição.
     *
     * Se o FAB já estiver em processo de animação (`animatingFab` é `true`), a função retorna
     * imediatamente para evitar animações concorrentes.  Caso contrário, define a translação Y
     * do FAB para mostrar ou esconder o botão e inicia a animação.
     *
     * @param show `true` para mostrar o FAB, `false` para escondê-lo.
     */
    private fun toggleFabVisibility(show: Boolean) = with(binding) {

        if (animatingFab) return@with

        val translationY = if (show) 0f else (fabConclude.height * 2f)

        fabConclude.animate().translationY(translationY).setDuration(400L)
            .setInterpolator(android.view.animation.AnticipateOvershootInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    animatingFab = true
                    super.onAnimationStart(animation)
                }

                override fun onAnimationEnd(animation: Animator) {
                    animatingFab = false
                    super.onAnimationEnd(animation)
                }
            }).start()
    }

    /**
     * Observa os estados da UI disparados pelo viewmodel chamando a função adequada para cada estado.
     * Utiliza a função collectFlow para coletar os estados do flow de forma segura e sem repetições de código.
     */
    private fun observeStates() {

        collectFlow(viewModel.statesFlow) { state ->
            when (state) {
                State.Idle -> {
                    binding.progressBar.isVisible = false

                }

                State.Loading -> {
                    binding.progressBar.isVisible = true
                }
            }
        }


        collectFlow(viewModel.installedApps) {
            lifecycleScope.launch {
                adapter.submitList(it, binding.tietSearch.text.toString().trim())
            }
        }

    }

    /**
     * Observa os estados da UI disparados pelo viewmodel chamando a função adequada para cada estado.
     * Utiliza a função collectFlow para coletar os estados do flow de forma segura e sem repetições de código.
     */
    private fun observeEvents() {
        collectFlow(viewModel.eventsFlow) { event ->
            when (event) {
                is Event.BlockSelection -> {
                    adapter.setBlockSelection(event.block)
                }

                is Event.NavigateHome -> {
                    setResultAndClose(event.apps)
                }

                Event.SelectedAlreadyManagedApp -> {
                    showHintDialog(
                        Preferences.SELECTED_APPS_ALREADY_MANAGED,
                        getString(R.string.Um_ou_mais_dos_apps_selecionados_ja_estao_sendo_gerenciados),
                        100
                    )
                }

                is Event.SimpleErrorMessage -> {
                    showErrorSnackBar(event.error, binding.fabConclude)

                }
            }
        }
    }

    private fun setResultAndClose(apps: List<InstalledApp>) {
        val result = Bundle().apply {
            putSerializable(
                BUNDLED_PACKAGES_KEY, ArrayList(apps)
            )
        }

        setFragmentResult(RESULT_KEY, result)
        goBack()
    }

    private fun showPopUpMenu(view: View) {
        val popupMenu = popupMenu {

            section {

                item {
                    label = getString(R.string.Selecionar_todos)
                    icon = R.drawable.vec_select_all
                    callback = {
                        viewModel.selectAppsAllOrNone(true)
                    }
                }

                item {
                    label = getString(R.string.Desselecionar_todos)
                    icon = R.drawable.vec_select_none
                    callback = {
                        viewModel.selectAppsAllOrNone(false)
                    }
                }
                item {
                    label = getString(R.string.Inverter_sele_o)
                    icon = R.drawable.vec_invert_selection
                    callback = {
                        viewModel.invertSelection()
                    }
                }

            }

            section {

                item {
                    label = if (viewModel.includeSystemApps) getString(R.string.Excluir_apps_do_sistema)
                    else getString(R.string.Incluir_apps_do_sistema)
                    icon = R.drawable.vec_app
                    callback = {
                        viewModel.toggleIncludeSystemApps()
                    }
                }

                item {
                    label = if (viewModel.includeManagedApps)
                        getString(R.string.Excluir_apps_gerenciados)
                    else getString(R.string.Incluir_apps_gerenciados)
                    icon = R.drawable.vec_app
                    callback = {
                        viewModel.toggleIncludeManagedApps()
                        Toast.makeText(requireContext(), "Em desenvolvimento", Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }

        popupMenu.show(this@SelectAppsFragment.requireContext(), view)
    }

}
