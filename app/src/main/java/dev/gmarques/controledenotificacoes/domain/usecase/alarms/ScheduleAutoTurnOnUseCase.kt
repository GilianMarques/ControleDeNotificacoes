package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import android.util.Log
import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.at
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.withSecondsAndMillisSetToZero
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.joda.time.LocalDateTime
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 04/07/2025 as 10:55
 *
 * Usa o [ScheduleManager] para agendar o boot do app em  horarios distintos para garantir que o app volte a executar
 * caso seja fechado por erro, ou outro motivo qqer.
 */

class ScheduleAutoTurnOnUseCase @Inject constructor(
    private val scheduleManager: ScheduleManager,
) {


    suspend operator fun invoke() = withContext(IO) {


        val hours = listOf(
            LocalDateTime().at(13, 10).withSecondsAndMillisSetToZero(),
            LocalDateTime().at(0, 0).withSecondsAndMillisSetToZero().plusDays(1) // primeiro instante do dia seguinte
        )
        val now = LocalDateTime()

        for (time in hours) if (time.isAfter(now)) {
            scheduleManager.scheduleAutoBoot(time.toDate().time)
            Log.d("USUK", "ScheduleAutoTurnOnUseCase.invoke: scheduled to $time")
            break
        }

    }


}