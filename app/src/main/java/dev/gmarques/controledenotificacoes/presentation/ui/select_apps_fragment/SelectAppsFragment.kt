package dev.gmarques.controledenotificacoes.presentation.ui.select_apps_fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentSelectAppsBinding
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
        const val BUNDLED_SELECTED_APPS_KEY = "bundled_selectAppsResult"
    }

    private var animatingFab = false

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
        getPreSelectedPackages()
        setupRecyclerView()
        setupSearch()
        setupObservers()
        setupFabConclude()
    }

    private fun getPreSelectedPackages() {
        viewModel.preSelectedPackages = args.preSelectedPackages.toHashSet()
        viewModel.searchApps()

    }

    private fun setupFabConclude() = with(binding) {
        fabConclude.setOnClickListener(AnimatedClickListener {

            if (viewModel.selectedApps.isEmpty()) {
                Snackbar.make(root, getString(R.string.Selecione_pelo_menos_um_aplicativo), Snackbar.LENGTH_SHORT).show()
                vibrator.error()
                return@AnimatedClickListener
            }
            vibrator.interaction()
            setResultAndClose()
        })


    }

    private fun setResultAndClose() {
        val result = Bundle().apply {
            putSerializable(
                BUNDLED_SELECTED_APPS_KEY,
                ArrayList(viewModel.selectedApps)
            )
        }

        setFragmentResult(RESULT_KEY, result)
        findNavController().popBackStack()
    }

    private fun setupSearch() {
        binding.tietSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.searchApps(text.toString())
        }
    }

    private fun setupRecyclerView() {

        adapter = AppsAdapter { app, checked ->
            viewModel.onAppChecked(app, checked)
            vibrator.interaction()
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

        if (animatingFab) return@with

        val translationY = if (show) 0f else (fabConclude.height * 2f)
        val alpha = if (show) 1f else 1f

        fabConclude.animate().translationY(translationY).alpha(alpha).setDuration(400L)
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

    private fun setupObservers() {
        viewModel.apps.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

}
