package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.gmarques.controledenotificacoes.databinding.BottomsheetAppListBinding
import dev.gmarques.controledenotificacoes.domain.usecase.GetInstalledAppsUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 09:50.
 */
class AppListBottomSheet() : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(
            getInstalledAppsUseCase: GetInstalledAppsUseCase,
            onAppChecked: (InstalledApp, Boolean) -> Unit,
        ): AppListBottomSheet {
            return AppListBottomSheet().apply {
                this.getInstalledAppsUseCase = getInstalledAppsUseCase
                this.onAppChecked = onAppChecked
                this.initialized = true
            }
        }
    }


    private lateinit var getInstalledAppsUseCase: GetInstalledAppsUseCase
    private lateinit var onAppChecked: (InstalledApp, Boolean) -> Unit

    private var initialized = false

    private var _binding: BottomsheetAppListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AppAdapter

    private var loaderJob = Job()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomsheetAppListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!initialized) {
            dismiss()
            return
        }

        setupRecyclerView()
        setupSearchView()
        loadApps() // todo   onviewcreated sendo chamado 2x - converer isso em um fragmento convencional

    }

    private fun setupSearchView() = with(binding) {
        tietSearch.doOnTextChanged { text, _, _, _ ->
            loadApps(binding.tietSearch.text.toString())
        }
    }

    private fun loadApps(name: String = "") {
        loaderJob.cancel()
        loaderJob = Job()
        lifecycleScope.launch(loaderJob) {
            Log.d("USUK", "AppListBottomSheet.".plus("loadApps() name: '$name' "))
            val apps = getInstalledAppsUseCase(binding.tietSearch.text.toString())
            withContext(Main) { adapter.submitList(apps) }
        }
    }

    private fun setupRecyclerView() {
        adapter = AppAdapter { app, checked ->
            onAppChecked(app, checked)
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvApps.layoutManager = layoutManager
        binding.rvApps.adapter = adapter
        binding.rvApps.addOnScrollListener(object : RecyclerView.OnScrollListener() {

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

    private fun toggleFabVisibility(show: Boolean) = with(binding) {
        val translationY = if (show) 0f else (btnConclude.height * 2f)
        val alpha = if (show) 1f else 1f

        btnConclude.animate()
            .translationY(translationY)
            .alpha(alpha)
            .setDuration(400L)
            .setInterpolator(android.view.animation.AnticipateOvershootInterpolator())
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}