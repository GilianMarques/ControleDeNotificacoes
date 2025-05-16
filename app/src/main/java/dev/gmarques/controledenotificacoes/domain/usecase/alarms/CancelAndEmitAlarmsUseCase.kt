package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.domain.usecase.settings.ReadPreferenceUseCase
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 16 de maio de 2025 as 12:33.
 */

class CancelAndEmitAlarmsUseCase @Inject constructor(
    private val x: ReadPreferenceUseCase,
    private val y: ScheduleManager,

    ) {

    suspend operator fun invoke() {

    }
}