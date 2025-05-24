package dev.gmarques.controledenotificacoes.framework

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.Preferences
import dev.gmarques.controledenotificacoes.domain.framework.ScheduleManager
import dev.gmarques.controledenotificacoes.domain.usecase.settings.DeletePreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.settings.ReadPreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.settings.SavePreferenceUseCase
import dev.gmarques.controledenotificacoes.framework.report_notification.AlarmReceiver
import org.joda.time.LocalDateTime
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 16 de maio de 2025 as 11:07.
 *
 * Gerencia o agendamento e cancelamento de alarmes no sistema usados para emitir notificações
 */
class ScheduleManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savePreferenceUseCase: SavePreferenceUseCase,
    private val deletePreferenceUseCase: DeletePreferenceUseCase,
    private val readPreferenceUseCase: ReadPreferenceUseCase,
) : ScheduleManager {

    private val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

    /**
     * Agenda um alarme para um pacote específico em um determinado horário.
     * Se um agendamento ja existir, será cancelado e um novo será criado,
     * garantindo que apenas um alarme seja agendado para cada pacote.
     *
     * @param packageId O ID do pacote para o qual o alarme será agendado.
     * @param millis O horário em milissegundos em que o alarme deve disparar.
     */
    override fun scheduleAlarm(packageId: String, millis: Long) {
        Log.d("USUK", "ScheduleManagerImpl.scheduleAlarm: $packageId scheduled at ${LocalDateTime(millis)}")

        cancelAlarm(packageId) // avoid multiple schedules for the same package

        val pIntent = createPendingIntent(packageId)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pIntent)

        saveScheduleData(packageId)
    }

    /**
     * Cancela um alarme agendado para um pacote específico.
     *
     * @param packageId O ID do pacote para o qual o alarme será cancelado.
     */
    override fun cancelAlarm(packageId: String) {
        Log.d("USUK", "ScheduleManagerImpl.cancelAlarm: $packageId cancelled (this does not mean the alarm was set)")
        val pIntent = createPendingIntent(packageId)

        alarmManager.cancel(pIntent)

        deleteScheduleData(packageId)
    }

    /**
     * Verifica se existe algum alarme agendado para um pacote específico.
     * Lê a preferência que armazena a lista de pacotes com alarmes agendados e verifica se o `packageId` está presente.
     *
     * @param packageId O ID do pacote a ser verificado.
     * @return `true` se houver um alarme agendado para o pacote, `false` caso contrário.
     */
    override fun isThereAnyAlarmSetForPackage(packageId: String): Boolean {
        val json = readPreferenceUseCase(Preferences.SCHEDULED_ALARMS, "")
        return (MoshiListConverter.fromJson(json) ?: mutableListOf()).contains(packageId)
    }

    /**
     * Recupera a lista de todos os pacotes que possuem alarmes agendados.
     * Lê a preferência que armazena a lista de pacotes com alarmes agendados e a retorna.
     *
     * @return Uma lista de strings contendo os IDs dos pacotes com alarmes agendados. Retorna uma lista vazia se nenhum alarme
     * estiver agendado ou se a preferência não existir.
     */
    override fun getAllSchedules(): List<String> {
        val json = readPreferenceUseCase(Preferences.SCHEDULED_ALARMS, "")
        return MoshiListConverter.fromJson(json) ?: mutableListOf()
    }

    /**
     * Cria um [PendingIntent] para ser usado com o [AlarmManager].
     * Este [PendingIntent] será acionado quando o alarme disparar, enviando um broadcast para o [dev.gmarques.controledenotificacoes.framework.report_notification.AlarmReceiver].
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
     * Este [Intent] é configurado para iniciar o [dev.gmarques.controledenotificacoes.framework.report_notification.AlarmReceiver] e inclui o `packageId` como um extra.
     *
     * @param packageId O ID do pacote a ser incluído como extra no [Intent].
     * @return Um [Intent] configurado para o [dev.gmarques.controledenotificacoes.framework.report_notification.AlarmReceiver].
     */
    private fun createIntent(packageId: String): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra("packageId", packageId)
        }
    }

    /**
     * Salva o pacote  indicando que um alarme foi agendado para o aplicativo especificado.
     * Garante que os pacotes na lista não se repitam.
     * Utiliza o [SavePreferenceUseCase] para persistir essa informação.
     *
     * @param packageId O ID do pacote para o qual o dado de agendamento será salvo.
     */
    private fun saveScheduleData(packageId: String) {

        val json = readPreferenceUseCase(Preferences.SCHEDULED_ALARMS, "")
        val list = (MoshiListConverter.fromJson(json) ?: mutableListOf())
            .apply { if (!this.contains(packageId)) add(packageId) }

        val updateJson = MoshiListConverter.toJson(list)

        savePreferenceUseCase(Preferences.SCHEDULED_ALARMS, updateJson)
    }

    /**
     * Remove o dado que indica que um alarme foi agendado para o pacote especificado.
     *
     * @param packageId O ID do pacote para o qual o dado de agendamento será removido.
     */
    override fun deleteScheduleData(packageId: String) {

        val json = readPreferenceUseCase(Preferences.SCHEDULED_ALARMS, "")
        val list = (MoshiListConverter.fromJson(json) ?: mutableListOf())
            .apply { remove(packageId) }

        val updateJson = MoshiListConverter.toJson(list)

        Log.d(
            "USUK",
            "ScheduleManagerImpl.".plus(
                "deleteScheduleData() packageId = $packageId deleted from preferences:\n${
                    list.joinToString(",")
                }"
            )
        )
        savePreferenceUseCase(Preferences.SCHEDULED_ALARMS, updateJson)

    }

    /**
     * Objeto utilitário para converter listas de strings de e para JSON usando a biblioteca Moshi.
     * Este objeto é usado para serializar e desserializar a lista de pacotes com alarmes agendados.
     */
    object MoshiListConverter {

        private val moshi = Moshi.Builder().build()
        private val type = Types.newParameterizedType(MutableList::class.java, String::class.java)
        private val adapter = moshi.adapter<MutableList<String>>(type)

        /**
         * Converte uma lista de strings em uma string JSON.
         *
         * @param list A lista de strings a ser convertida.
         * @return A representação JSON da lista.
         */
        fun toJson(list: MutableList<String>): String {
            return adapter.toJson(list)
        }

        /**
         * Converte uma string JSON em uma lista de strings.
         *
         * @param json A string JSON a ser convertida.
         * @return A lista de strings desserializada, uma lista vazia se a string estivee vazia ou `null` se a conversão falhar.
         */
        fun fromJson(json: String): MutableList<String>? {
            return if (json.isEmpty()) mutableListOf<String>() else adapter.fromJson(json)!!
        }
    }

}