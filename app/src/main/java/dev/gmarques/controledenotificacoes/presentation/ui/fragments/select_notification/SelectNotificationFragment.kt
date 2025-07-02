package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.databinding.FragmentSelectNotificationBinding
import dev.gmarques.controledenotificacoes.presentation.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:16.
 */
@AndroidEntryPoint
class SelectNotificationFragment : MyFragment() {
    companion object {
        const val RESULT_LISTENER_KEY = "select_notification_listener_key"
        const val BUNDLED_PACKAGE_NAME_KEY = "bundled_package_name"


    }

    private lateinit var binding: FragmentSelectNotificationBinding
    private val viewModel: SelectNotificationViewModel by viewModels()
    private lateinit var adapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return FragmentSelectNotificationBinding.inflate(inflater, container, false).also {
            binding = it
            setupActionBar(binding.actionbar)
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = NotificationsAdapter(::onNotificationSelected)

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SelectNotificationFragment.adapter
        }
    }

    private fun onNotificationSelected(notification: ActiveStatusBarNotification) {
        val result = bundleOf(BUNDLED_PACKAGE_NAME_KEY to notification.packageId)
        setFragmentResult(RESULT_LISTENER_KEY, result)
        goBack()
    }

    private fun observeViewModel() {
        collectFlow(viewModel.notificationsFlow) { list ->
            binding.progressBar.isVisible = false
            adapter.submitList(list)
        }
    }
}
