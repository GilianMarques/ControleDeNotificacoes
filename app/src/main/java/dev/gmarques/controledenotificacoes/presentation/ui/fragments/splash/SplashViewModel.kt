package dev.gmarques.controledenotificacoes.presentation.ui.fragments.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsável por gerenciar o estado do processo de login
 * e fornecer informações relevantes para a interface de usuário.
 *
 * Criado por Gilian Marques
 * Em domingo, 27 de abril de 2025 às 19:32.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getUserUseCase: GetUserUseCase,
) : ViewModel() {

    private val _navigationFlow = MutableStateFlow<NavigationRequirements>(NavigationRequirements())
    val navigationFlow: StateFlow<NavigationRequirements> get() = _navigationFlow

    private val _eventFlow = MutableSharedFlow<SplashEvent>(replay = 1)
    val eventFlow: SharedFlow<SplashEvent> get() = _eventFlow

    /**
     * Verifica se o usuário já está autenticado no Firebase.
     * Emite um evento para iniciar o fluxo de login caso não esteja.
     * Emite um evento e atualiza o estado de navegação caso já esteja autenticado.
     */
    fun checkLoginState() = viewModelScope.launch {

        val currentUser = getUserUseCase()
        if (currentUser == null) _eventFlow.tryEmit(SplashEvent.NavigateToLoginFragment)
        else addNavigationRequirement(NavigationRequirements.Requirement.USER_LOGGED_IN)

    }

    /**
     * Indica que um requisito de navegação foi satisfeito.
     * Quando todos os requisitos em [NavigationRequirements] forem satisfeitos o [SplashFragment] navegara para o [HomeFragment]
     * @param NavigationRequirements Qualquer valor dentro de [NavigationRequirements.Requirement]
     */
    fun addNavigationRequirement(requirement: NavigationRequirements.Requirement) {
        _navigationFlow.value = _navigationFlow.value.copy(requirement)
    }

}

/**Representa os eventos da UI*/
sealed class SplashEvent {
    object NavigateToLoginFragment : SplashEvent()
}

/**
 * Representa uma lista de requerimentos necessários para que o [SplashFragment] possa navegar para o [HomeFragment]
 */
data class NavigationRequirements(
    private val requirement: Requirement = Requirement.NONE,
    private val satisfiedRequirements: HashSet<Requirement> = hashSetOf<Requirement>(),
) {

    init {
        satisfiedRequirements.add(requirement)
    }

    fun canNavigateHome() = satisfiedRequirements.containsAll(Requirement.entries.toSet())

    enum class Requirement {
        NONE,
        DATA_LOADED,
        APP_NOT_BLOCKED,
        USER_LOGGED_IN;
    }
}

