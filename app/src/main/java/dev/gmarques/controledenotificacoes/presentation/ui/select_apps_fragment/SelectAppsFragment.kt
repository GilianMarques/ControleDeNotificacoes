package dev.gmarques.controledenotificacoes.presentation.ui.select_apps_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentSelectAppsBinding
import dev.gmarques.controledenotificacoes.presentation.ui.ManagedAppsSharedViewModel
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import kotlin.getValue

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 09:50.
 */
@AndroidEntryPoint
class SelectAppsFragment : MyFragment() {

    private lateinit var binding: FragmentSelectAppsBinding

    private val viewModel: SelectAppsViewModel by viewModels()
    private val sharedViewModel: ManagedAppsSharedViewModel by navGraphViewModels(R.id.nav_graph_manage_apps_xml)

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
        setupRecyclerView()
        setupSearch()
        setupObservers()
    }

    private fun setupSearch() {
        binding.tietSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.searchApps(text.toString())
        }
    }

    private fun setupRecyclerView() {
        adapter = AppsAdapter { app, checked ->
            viewModel.onAppChecked(app, checked)
        }

        binding.rvApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SelectAppsFragment.adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var isFabVisible = true

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

    private fun toggleFabVisibility(show: Boolean) = with(binding) {
        val translationY = if (show) 0f else (btnConclude.height * 2f)
        val alpha = if (show) 1f else 1f

        btnConclude.animate().translationY(translationY).alpha(alpha).setDuration(400L)
            .setInterpolator(android.view.animation.AnticipateOvershootInterpolator()).start()
    }

    private fun setupObservers() {
        viewModel.apps.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

}
