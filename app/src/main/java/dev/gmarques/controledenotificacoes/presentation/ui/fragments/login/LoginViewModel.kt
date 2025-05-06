package dev.gmarques.controledenotificacoes.presentation.ui.fragments.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.IdpResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.User
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getUserUseCase: GetUserUseCase,
) : ViewModel() {


    private val _eventFlow = MutableSharedFlow<LoginEvent>(replay = 1)
    val eventFlow: SharedFlow<LoginEvent> get() = _eventFlow

    /**
     * Lida com o resultado do fluxo de autenticação FirebaseUI.
     * Emite eventos de sucesso ou erro com base no resultado.
     */
    fun handleLoginResult(resultCode: Int, response: IdpResponse?) = viewModelScope.launch {

        if (resultCode == android.app.Activity.RESULT_OK) {
            val user = getUserUseCase() ?: error("user nao pode ser nulo se o login foi bem sucedido")
            _eventFlow.emit(LoginEvent.Success(user))
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

}

sealed class LoginEvent {
    object StartFlow : LoginEvent()
    data class Success(val user: User) : LoginEvent()
    data class Error(val message: String) : LoginEvent()
}

