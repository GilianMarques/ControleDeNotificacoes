package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.gmarques.controledenotificacoes.databinding.BottomsheetAppListBinding
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 09:50.
 */
class AppListBottomSheet() : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(apps: List<InstalledApp>, onAppSelected: (InstalledApp) -> Unit): AppListBottomSheet {
            return AppListBottomSheet().apply {
                setDependencies(apps, onAppSelected)
            }
        }
    }

    private var initialized = false
    private lateinit var apps: List<InstalledApp>
    private lateinit var onAppSelected: (InstalledApp) -> Unit

    private fun setDependencies(apps: List<InstalledApp>, onAppSelected: (InstalledApp) -> Unit) {
        this.apps = apps
        this.onAppSelected = onAppSelected
        initialized = true // previne crashes por recriação após mudança de configuração da ui
    }

    private var _binding: BottomsheetAppListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AppAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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

        adapter = AppAdapter { app ->
            onAppSelected(app)
            //  dismiss()
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

        adapter.submitList(apps)

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