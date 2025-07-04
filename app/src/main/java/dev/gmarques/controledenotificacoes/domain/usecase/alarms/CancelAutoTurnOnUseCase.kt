package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 04/07/2025 as 10:55
 *
 * Usa o [ScheduleManager] para cancelar o boot do app
 */

class CancelAutoTurnOnUseCase @Inject constructor(
    private val scheduleManager: ScheduleManager,
) {

    suspend operator fun invoke() = withContext(IO) {
        scheduleManager.cancelAutoBoot()
    }


}