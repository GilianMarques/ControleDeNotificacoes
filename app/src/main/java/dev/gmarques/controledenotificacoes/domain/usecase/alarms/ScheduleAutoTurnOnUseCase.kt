package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import android.util.Log
import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.at
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.withSecondsAndMillisSetToZero
import org.joda.time.LocalDateTime
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 05 de julho de 2025 as 19:26.
 */
class ScheduleAutoTurnOnUseCase @Inject constructor(private val scheduler: AlarmScheduler) {

    operator fun invoke() {

        val now = LocalDateTime()

        val times = listOf(
            LocalDateTime().at(12, 0).withSecondsAndMillisSetToZero(),
            LocalDateTime().at(0, 0).plusDays(1).withSecondsAndMillisSetToZero()// primeiro instante do dia seguinte
        )

        for (time in times) {
            if (time.isAfter(now)) {
                scheduler.scheduleAutoTurnOnAlarm(time.toDate().time).also {
                    Log.d("USUK", "ScheduleAutoTurnOnUseCase.invoke: scheduled for: $time")
                }
                break
            }
        }
    }
}