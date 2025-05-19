package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import android.util.Log
import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextUnlockPeriodFromNow
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
    private val scheduleManager: ScheduleManager,
    private val getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
) {

    private val ruleCache = HashMap<String, Rule>()

    /**
     * Reagenda todos os alarmes que estavam ativos antes do dispositivo ser reiniciado.
     * É executado em uma corrotina no dispatcher IO.
     */
    // TODO: chamar no boot
    suspend operator fun invoke() = withContext(IO) {

        val activeSchedules = scheduleManager.getAllSchedules()

        activeSchedules.map { pkg ->
            async {

                val app = getApp(pkg)
                if (app == null) return@async

                val rule = getRule(app.ruleId)

                scheduleAlarmForApp(app, rule)
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

    /**
     * Agenda um alarme para um aplicativo específico com base em sua regra.
     * Se o aplicativo estiver em período de bloqueio, o alarme é agendado para o próximo período de desbloqueio.
     * Caso contrário, o alarme é agendado para 5 segundos a partir do momento atual.
     * @param app O aplicativo para o qual o alarme será agendado.
     * @param rule A regra associada ao aplicativo.
     */
    private fun scheduleAlarmForApp(app: ManagedApp, rule: Rule) {

        val scheduleTimeMillis =
            if (rule.isAppInBlockPeriod()) rule.nextUnlockPeriodFromNow().millis
            else System.currentTimeMillis() + 5_000L

        scheduleManager.scheduleAlarm(app.packageId, scheduleTimeMillis)

    }
}