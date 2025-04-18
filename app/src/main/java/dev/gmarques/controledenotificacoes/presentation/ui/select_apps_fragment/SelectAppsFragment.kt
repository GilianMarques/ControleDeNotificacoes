package dev.gmarques.controledenotificacoes.presentation.ui.select_apps_fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.databinding.FragmentSelectAppsBinding
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener

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

    private lateinit var binding: FragmentSelectAppsBinding
    private val viewModel: SelectAppsViewModel by viewModels()
    private val args: SelectAppsFragmentArgs by navArgs()

    private lateinit var adapter: AppsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSelectAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPreSelectedPackagesAndLoad()
        setupRecyclerView()
        setupSearch()
        observeViewModel()
        observeEvents()
        setupFabConclude()
    }

    /**
     * Carrega os pacotes de apps que devem ser excluidos da busca em
     * um hashset para otimizar o tempo de consulta. (O(1) ou O(n))
     * Por fim, dispara a busca de aplicativos para aplicar a seleção inicial.
     */
    private fun getPreSelectedPackagesAndLoad() {
        viewModel.preSelectedAppsToHide = args.excludePackages.toHashSet()
        viewModel.loadAllApps()
    }

    private fun setupFabConclude() = with(binding) {
        fabConclude.setOnClickListener(AnimatedClickListener {
            viewModel.validateSelection()
        })

    }

    private fun setupSearch() {
        binding.tietSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.searchApps(text.toString())
        }
    }

    private fun setupRecyclerView() {

        adapter = AppsAdapter { app, checked ->

            viewModel.onAppChecked(app, checked)

            when (viewModel.canSelectMoreApps()) {
                true -> vibrator.interaction()
                false -> vibrator.error()
            }

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

    private fun observeViewModel() {

        viewModel.installedApps.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = false
            adapter.submitList(it)
        }

        viewModel.blockUiSelection.observe(viewLifecycleOwner) {
            adapter.setBlockSelection(it)
        }

    }

    private fun observeEvents() {
        viewModel.uiEvents.observe(viewLifecycleOwner) { event ->

            with(event.cantSelectMoreApps.consume()) {
                if (this != null) {
                    showErrorSnackBar(this)
                }
            }

            with(event.navigateHomeEvent.consume()) {
                if (this != null) {
                    setResultAndClose(this)
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
        vibrator.success()
        goBack()
    }

}
