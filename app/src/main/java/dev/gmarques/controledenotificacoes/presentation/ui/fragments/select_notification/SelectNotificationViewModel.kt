package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_notification

import android.service.notification.StatusBarNotification
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.usecase.GetActiveNotificationsUseCase
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
    // TODO: verificar o que isso faz e se nao da pra simplificar
    val notificationsFlow: StateFlow<List<StatusBarNotification>> = getActiveNotificationsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
