package dev.gmarques.controledenotificacoes.framework

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.domain.usecase.settings.DeletePreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.settings.ReadPreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.settings.SavePreferenceUseCase
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 16 de maio de 2025 as 11:07.
 *
 * gerencia o agendamento e cancelamento de alarmes no sistema
 */
class ScheduleManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savePreferenceUseCase: SavePreferenceUseCase,
    private val deletePreferenceUseCase: DeletePreferenceUseCase,
    private val readPreferenceUseCase: ReadPreferenceUseCase,
) : ScheduleManager {


    /**
     * Agenda um alarme para um pacote específico em um determinado horário.
     *
     * @param packageId O ID do pacote para o qual o alarme será agendado.
     * @param millis O horário em milissegundos em que o alarme deve disparar.
     */
    override fun scheduleAlarm(packageId: String, millis: Long) {

        val pIntent = createPendingIntent(packageId)

        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pIntent)

        saveScheduleData(packageId)
        Log.d("USUK", "ScheduleManagerImpl.scheduleAlarm: $packageId scheduled")
    }

    /**
     * Cancela um alarme agendado para um pacote específico.
     *
     * @param packageId O ID do pacote para o qual o alarme será cancelado.
     */
    override fun cancelAlarm(packageId: String) {
        val pIntent = createPendingIntent(packageId)

        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pIntent)

        deleteScheduleData(packageId)
        Log.d("USUK", "ScheduleManagerImpl.cancelAlarm: $packageId cancelled")
    }

    override fun isAnyAlarmSetForPackage(packageId: String): Boolean {
        val defValue = "no_active_schedule"
        val pref = readPreferenceUseCase(packageId.scheduleKey(), defValue)
        return pref != defValue

    }

    /**
     * Cria um [PendingIntent] para ser usado com o [AlarmManager].
     * Este [PendingIntent] será acionado quando o alarme disparar, enviando um broadcast para o [AlarmReceiver].
     *
     * @param packageId O ID do pacote a ser incluído como extra no [Intent] do [PendingIntent].
     * @return Um [PendingIntent] configurado para enviar um broadcast.
     */
    private fun createPendingIntent(packageId: String): PendingIntent {
        return PendingIntent.getBroadcast(
            context, 0, createIntent(packageId), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Cria um [Intent] para ser usado na criação do [PendingIntent].
     * Este [Intent] é configurado para iniciar o [AlarmReceiver] e inclui o `packageId` como um extra.
     *
     * @param packageId O ID do pacote a ser incluído como extra no [Intent].
     * @return Um [Intent] configurado para o [AlarmReceiver].
     */
    private fun createIntent(packageId: String): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra("packageId", packageId)
        }
    }

    /**
     * Salva um dado indicando que um alarme foi agendado para o pacote especificado.
     * Utiliza o [SavePreferenceUseCase] para persistir essa informação.
     * A chave usada para salvar é prefixada com "schedule_".
     *
     * @param packageId O ID do pacote para o qual o dado de agendamento será salvo.
     */
    private fun saveScheduleData(packageId: String) {
        val value = "scheduled"
        savePreferenceUseCase(packageId.scheduleKey(), value)
    }

    /**
     * Remove o dado que indica que um alarme foi agendado para o pacote especificado.
     *
     * @param packageId O ID do pacote para o qual o dado de agendamento será removido.
     */
    private fun deleteScheduleData(packageId: String) {
        deletePreferenceUseCase(packageId.scheduleKey())
    }
// TODO: salvar uma lista de agendamentos...
    /**
     * Cria uma chave que sera usada para identificar pacotes com agendamento de alarme
     * Era pra ser uma variavel global mas é dinamica e depende do pacote e eu nao quero deixar que
     * as varias funçoes quea cessa essa funcionalidadem fiquem responsaveis pela concatenação.
     *
     * Essa funçao é tão específica pra esse contexto que optei por deixa-la isolada aqui nessa classe.
     */
    private fun String.scheduleKey() = "scheduled_pkg_$this"

}