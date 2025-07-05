package dev.gmarques.controledenotificacoes.framework.report_notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints

/**
 * É executado mediante agendamento no sistema para  emitir notificações ao usuario.
 * Usa [ReportNotificationManager] para exibir a notificação de relatório.e [dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler]
 * para limpar os dados de agendamento após a emissao das notificações.
 */
class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val pkg = intent.getStringExtra("packageId") ?: return
        Log.d("USUK", "AlarmReceiver.onReceive: alarm received for $pkg")

        getReportNotificationManager().showReportNotification(pkg)

        clearPreferenceForPackage(pkg)
    }

    private fun getReportNotificationManager(): ReportNotificationManager {
        return HiltEntryPoints.reportNotificationManager()
    }

    /**
     *Remove das preferências ou o nome de pacote do aplicativo que acabou de ter a notificação de relatório exibida garantindo
     * que os registros nas preferências  estejam sempre atualizados em relação aos alarmes agendados no sistema e prevenindo que
     * um alarme que já foi disparado seja reagendado por acidente causando inconsistências.
     *
     * @param pkg O nome do pacote do aplicativo cujos dados de agendamento devem ser limpos.
     */
    private fun clearPreferenceForPackage(pkg: String) {

        val scheduleManager = HiltEntryPoints.scheduleManager()

        scheduleManager.deleteScheduleData(pkg)
    }
}