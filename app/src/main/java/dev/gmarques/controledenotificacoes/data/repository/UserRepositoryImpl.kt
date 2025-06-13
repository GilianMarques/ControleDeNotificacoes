package dev.gmarques.controledenotificacoes.data.repository

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.data.repository.UserRepository
import dev.gmarques.controledenotificacoes.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 11:03.
 */
class UserRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) : UserRepository {

    @Suppress("unused")
    private val defaultForTestsOnlyUser by lazy {
        User(
            "Test user",
            "in.test@gmail.com",
            "https://picsum.photos/200"
        )
    }

    /**
     * Retorna o usuário logado ou nulo.
     * Quando em depuração pode retornar um usuário padrão para testes
     */
    override fun getUser(): User? {

        // remova as barras  abaixo para usar o usuário padrão para testes
        //  return defaultForTestsOnlyUser

        val fUser = FirebaseAuth.getInstance().currentUser
        if (fUser == null) return null

        /*Não vejo como pode ser possível uma pessoa logar com sua conta do Google sem ter um nome e email definidos,
        acredito que nem a foto terá URL nulo, mas por via das dúvidas é melhor mostrar informações nulas na tela do que fechar o app
        na cara do usuário, uma vez que a autenticação serve apenas para personalizar a interface */
        return User(fUser!!.displayName ?: "null", fUser!!.email ?: "null", fUser.photoUrl?.toString() ?: "null")
    }

    override suspend fun logOff() {
        AuthUI.getInstance().signOut(context).await()
        FirebaseAuth.getInstance().signOut()
    }

    override fun deleteAccount() {
        TODO("Not yet implemented")
    }
}

