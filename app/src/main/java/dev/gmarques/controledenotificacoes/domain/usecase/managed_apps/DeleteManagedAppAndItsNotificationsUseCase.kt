package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import android.util.Log
import androidx.room.withTransaction
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.DeleteAllAppNotificationsUseCase
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 02 de maio de 2025 as 22:34.
 *

 *
 */
class DeleteManagedAppAndItsNotificationsUseCase @Inject constructor(
    private val roomDb: RoomDatabase,
    private val repository: ManagedAppRepository,

    private val deleteAllAppNotificationsUseCase: DeleteAllAppNotificationsUseCase,
) {
    /**
     * Este UseCase pode ser utilizado tanto dentro quanto fora de uma transação do Room.
     *
     * Quando chamado por [dev.gmarques.controledenotificacoes.domain.usecase.DeleteRuleWithAppsUseCase], a operação já está
     * encapsulada em uma transação externa. No entanto, este UseCase também é reutilizado por outras classes que não iniciam
     * transações, o que exige que ele mesmo controle a atomicidade nesses casos.
     *
     * Para evitar transações aninhadas (que não são suportadas pelo Room) e duplicação de código,
     * a flag [doInsideTransaction] foi introduzida. Ela define se as operações devem ser executadas dentro de uma
     * transação interna ou assumem que o contexto já está transacional.
     */

    suspend operator fun invoke(packageId: String, doInsideTransaction: Boolean = true) {

        val action: suspend () -> Unit = {
            /*
                * Esta ordem limpa o cache de pendingIntents e bitmaps e depois apaga as notificaçoes do banco (com [DeleteAllAppNotificationsUseCase])
                * por fim apaga o app, isso garante que os dados em cache nao fiquem orfaos caso as notificações
                * sejam apagadas primeiro e um erro impeça que eles sejam removidos, permanecendo em cache por tempo indefinido.
                * Por fim se algo der errado, o cache foi limpo mas os dados do banco sao restaurados pela transação e aí é só tentar apagar de novo.
                */
            deleteAllAppNotificationsUseCase(packageId)
            repository.deleteManagedAppByPackageId(packageId)
        }

        try {
            if (doInsideTransaction) roomDb.withTransaction { action() }
            else action()
        } catch (e: Exception) {
            Log.e("USUK", "DeleteManagedAppAndItsNotificationsUseCase.invoke: Falha na transação: ${e.message}")
            false
        }

    }
}