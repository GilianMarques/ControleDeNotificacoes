package dev.gmarques.controledenotificacoes.presentation.ui.fragments.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.MessageFormat
import javax.inject.Inject

/**
 * ViewModel responsável por gerenciar o estado do processo de login
 * e fornecer informações relevantes para a interface de usuário.
 *
 * Criado por Gilian Marques
 * Em domingo, 27 de abril de 2025 às 19:32.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(@ApplicationContext private val context: Context) : ViewModel() {

    private val _navigationFlow = MutableStateFlow<NavigationRequirements>(NavigationRequirements())
    val navigationFlow: StateFlow<NavigationRequirements> get() = _navigationFlow

    private val _eventFlow = MutableSharedFlow<LoginEvent>(replay = 1)
    val eventFlow: SharedFlow<LoginEvent> get() = _eventFlow

    /**
     * Verifica se o usuário já está autenticado no Firebase.
     * Emite um evento para iniciar o fluxo de login caso não esteja.
     * Emite um evento e atualiza o estado de navegação caso já esteja autenticado.
     */
    fun checkUserLoggedIn() = viewModelScope.launch {

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) _eventFlow.tryEmit(LoginEvent.StartFlow)
        else {
            _navigationFlow.emit(_navigationFlow.value.copy(userLoggedIn = true))
        }
    }

    /**
     * Lida com o resultado do fluxo de autenticação FirebaseUI.
     * Emite eventos de sucesso ou erro com base no resultado.
     */
    fun handleLoginResult(resultCode: Int, response: IdpResponse?) = viewModelScope.launch {

        if (resultCode == android.app.Activity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            _eventFlow.emit(LoginEvent.Success(user))
            delay(2500) // Permite carregar os dados na conta do usuário na tela
            _navigationFlow.emit(_navigationFlow.value.copy(userLoggedIn = true))
        } else {
            _eventFlow.emit(LoginEvent.Error(response.toErrorMessage()))
        }
    }

    /**
     * Converte um objeto IdpResponse em uma mensagem de erro legível.
     */
    private fun IdpResponse?.toErrorMessage(): String {
        if (this == null) return context.getString(R.string.Voce_cancelou_o_login)

        return when (error?.errorCode) {

            com.firebase.ui.auth.ErrorCodes.NO_NETWORK -> context.getString(R.string.voce_n_o_est_conectado_internet)
            com.firebase.ui.auth.ErrorCodes.DEVELOPER_ERROR -> context.getString(
                R.string.Erro_de_desenvolvimento_c_digo, error?.errorCode, error?.message
            )

            com.firebase.ui.auth.ErrorCodes.PROVIDER_ERROR -> context.getString(
                R.string.Erro_no_provedor_c_digo, error?.errorCode, error?.message
            )

            else -> MessageFormat.format(
                context.getString(R.string.O_login_falhou_c_digo_0_mensagem_1), error?.errorCode, error?.message
            )
        }
    }

    /**
     * Essa função é chamada quando o listener do viewmodel do homefragment indica que os dados do usuário foram carregados
     * Essa função atualiza o flow que resulta na interface navegando para o próximo fragmento apenas quando os dados forem carregados
     * Existem outras condições para que este fragmento navegue para o próximo mas esta função é responsável apenas  por informar que o requisito de
     * carregamento de dados foi concluído
     */
    fun localDataLoaded() {
        _navigationFlow.value = _navigationFlow.value.copy(dataLoaded = true)
    }
}

sealed class LoginEvent {
    object StartFlow : LoginEvent()
    data class Error(val message: String) : LoginEvent()
    data class Success(val user: FirebaseUser?) : LoginEvent()
}

data class NavigationRequirements(
    private val dataLoaded: Boolean = false,
    private val userLoggedIn: Boolean = false,
) {

    fun canNavigateHome() = dataLoaded && userLoggedIn
}
