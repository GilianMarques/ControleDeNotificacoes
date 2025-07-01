package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.usecase.GetActiveNotificationsUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ActiveStatusBarNotification
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:17.
 */
@HiltViewModel
class SelectNotificationViewModel @Inject constructor(
    getActiveNotificationsUseCase: GetActiveNotificationsUseCase,
) : ViewModel() {

    val notificationsFlow: StateFlow<List<ActiveStatusBarNotification>> = getActiveNotificationsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
