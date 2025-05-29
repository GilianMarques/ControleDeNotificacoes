package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import android.util.Log
import androidx.room.withTransaction
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 02 de maio de 2025 as 22:34.
 */
class DeleteManagedAppAndItsNotificationsUseCase @Inject constructor(
    private val roomDb: RoomDatabase,
    private val repository: ManagedAppRepository,
    private val notificationRepository: AppNotificationRepository,
) {

    suspend operator fun invoke(packageId: String) {
        try {

            roomDb.withTransaction {
                repository.deleteManagedAppByPackageId(packageId)
                notificationRepository.deleteAll(packageId)
            }
        } catch (e: Exception) {
            Log.e("USUK", "DeleteManagedAppAndItsNotificationsUseCase.invoke: Falha na transação: ${e.message}")
            false
        }

    }
}