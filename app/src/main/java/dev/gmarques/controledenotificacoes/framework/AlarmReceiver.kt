package dev.gmarques.controledenotificacoes.framework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import dev.gmarques.controledenotificacoes.di.entry_points.ReportNotificationManagerEntryPoint
import dev.gmarques.controledenotificacoes.di.entry_points.ScheduleManagerEntryPoint

/**
 * É executado mediante agendamento no sistema para informar ao usuário que um app recém-desbloqueado
 * recebeu notificações durante o bloqueio.
 */
class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val pkg = intent.getStringExtra("packageId") ?: return
        Log.d("USUK", "AlarmReceiver.onReceive: alarm received for $pkg")

        getReportNotificationManager(context).showReportNotification(pkg)

        clearPreferenceForPackage(context, pkg)
    }

    private fun getReportNotificationManager(context: Context): ReportNotificationManager {
        return EntryPointAccessors
            .fromApplication(context, ReportNotificationManagerEntryPoint::class.java)
            .getReportNotificationManager()
    }

    /**
     *Remove das preferências ou o nome de pacote do aplicativo que acabou de ter a notificação de relatório exibida garantindo
     * que os registros nas preferências  estejam sempre atualizados em relação aos alarmes agendados no sistema e prevenindo que
     * um alarme que já foi disparado seja reagendado por acidente causando inconsistências.
     *
     * @param context O contexto da aplicação, usado para acessar o `ScheduleManager`.
     * @param pkg O nome do pacote do aplicativo cujos dados de agendamento devem ser limpos.
     */
    private fun clearPreferenceForPackage(context: Context, pkg: String) {

        val scheduleManager = EntryPointAccessors
            .fromApplication(context, ScheduleManagerEntryPoint::class.java)
            .getScheduleManager()

        scheduleManager.deleteScheduleData(pkg)
    }
}