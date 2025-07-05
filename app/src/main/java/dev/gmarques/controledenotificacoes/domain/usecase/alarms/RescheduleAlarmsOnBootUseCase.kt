package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import android.util.Log
import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 19 de maio de 2025 as 12:13.
 */

class RescheduleAlarmsOnBootUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val scheduleAlarmForAppUseCase: ScheduleAlarmForAppUseCase,
) {

    private val ruleCache = HashMap<String, Rule>()

    /**
     * Reagenda todos os alarmes que estavam ativos antes do dispositivo ser reiniciado.
     * É executado em uma corrotina no dispatcher IO.
     */
    suspend operator fun invoke() = withContext(IO) {

        val activeSchedules = alarmScheduler.getAllSchedules()

        activeSchedules.map { pkg ->
            Log.d("USUK", "RescheduleAlarmsOnBootUseCase.invoke: rescheduling $pkg")
            async {

                val app = getApp(pkg)
                if (app == null) return@async

                val rule = getRule(app.ruleId)
                scheduleAlarmForAppUseCase(app, rule)
            }
        }.awaitAll()
    }

    /**
     * Obtém uma regra do cache ou, se não existir, do banco de dados.
     * @param ruleId O ID da regra a ser obtida.
     * @return A regra correspondente ao ID.
     * @throws IllegalStateException Se a regra não for encontrada no banco de dados.
     */
    private suspend fun getRule(ruleId: String): Rule {
        return ruleCache.getOrPut(ruleId) {
            getRuleByIdUseCase(ruleId) ?: error("A regra ${ruleId} não foi encontrada. Isso é um Bug.")
        }
    }

    /**
     * Obtém um aplicativo gerenciado pelo ID do pacote.
     * @param pkg O ID do pacote do aplicativo.
     * @return O objeto ManagedApp correspondente ao pacote, ou null se não encontrado.
     */
    private suspend fun getApp(pkg: String): ManagedApp? {
        return getManagedAppByPackageIdUseCase(pkg)
            .also {
                if (it == null) Log.d(
                    "USUK",
                    "RescheduleAlarmsOnBootUseCase.getApp: $pkg not found."
                )
            }
    }

}