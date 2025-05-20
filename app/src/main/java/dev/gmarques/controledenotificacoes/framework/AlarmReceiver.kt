package dev.gmarques.controledenotificacoes.framework

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import dagger.hilt.android.EntryPointAccessors
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.di.entry_points.ScheduleManagerEntryPoint

/**
 * É executado mediante agendamento no sistema para informar ao usuário que um app recém-desbloqueado
 * recebeu notificações durante o bloqueio.
 */
class AlarmReceiver : BroadcastReceiver() {

    private val channelId = "notification_report"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val pkg = intent.getStringExtra("packageId")
        if (pkg.isNullOrBlank()) return
        Log.d("USUK", "AlarmReceiver.onReceive: alarm received for $pkg")

        showReportNotification(context, pkg)
        clearPreferenceForPackage(context, pkg)

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

    private fun getAppNameFromPackage(context: Context, packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName // Fallback: mostra o nome do pacote
        }
    }

    private fun showReportNotification(context: Context, pkg: String) {
        createNotificationChannelIfNeeded(context)

        val notification = buildNotification(context, pkg)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(pkg.hashCode(), notification)
    }

    /**
     * Constrói o objeto [Notification] a ser exibido para informar o usuário sobre as notificações
     * recebidas por um aplicativo específico durante o período de bloqueio.
     *
     * A notificação inclui:
     * - Título: O nome do aplicativo que recebeu as notificações.
     * - Texto: Uma mensagem informativa indicando que o app recebeu notificações enquanto estava bloqueado.
     * - Ícone pequeno: O ícone do launcher
     * - Ações:
     *     - Abrir App: Tenta abrir o aplicativo que recebeu as notificações.
     *     - Ver Histórico: Abre o aplicativo "Controle de Notificações".
     *
     * @param context O contexto usado para criar a notificação e acessar recursos.
     * @param pkg O nome do pacote do aplicativo que recebeu as notificações.
     * @param appName O nome amigável do aplicativo que recebeu as notificações.
     */
    private fun buildNotification(context: Context, pkg: String): Notification {

        val appName = getAppNameFromPackage(context, pkg)
        val appIcon = getAppIconFromPackage(context, pkg)


        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(appName)
            .setContentText(context.getString(R.string.X_recebeu_notifica_es_durante_o_bloqueio, appName))
            .setLargeIcon(appIcon)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(false)
            .setAutoCancel(true)
            .addAction(createOpenTargetAppAction(context, pkg))
            .addAction(createOpenMyAppAction(context))
            .build()
    }

    private fun getAppIconFromPackage(context: Context, packageName: String): Bitmap? {
        return try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                // Converte Drawable para Bitmap se necessário
                val bitmap = createBitmap(drawable.intrinsicWidth.coerceAtLeast(1), drawable.intrinsicHeight.coerceAtLeast(1))
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (e: Exception) {
            null // fallback em caso de erro
        }
    }

    private fun createNotificationChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.Relatorio_de_notificacoes)
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /**
     * Cria uma ação para a notificação que, quando clicada, tentará abrir o aplicativo
     * especificado pelo nome do pacote. Esta ação é útil para permitir que o usuário
     * acesse diretamente o aplicativo que gerou as notificações bloqueadas.
     *
     * Se não for possível obter o Intent de lançamento para o pacote (por exemplo, se o app
     * não estiver instalado), uma ação de fallback será retornada, exibindo um rótulo indicando
     * que o app está indisponível.
     *
     * @param context O contexto usado para obter o gerenciador de pacotes e criar o Intent.
     * @param packageName O nome do pacote do aplicativo a ser aberto.
     * @return Uma ação [NotificationCompat.Action] configurada para abrir o aplicativo alvo
     * ou uma ação de fallback caso o Intent não possa ser criado.
     */
    private fun createOpenTargetAppAction(context: Context, packageName: String): NotificationCompat.Action {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return createFallbackAction(context, context.getString(R.string.App_indispon_vel))

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            context.getString(R.string.Abrir_app),
            pendingIntent
        ).build()
    }

    /**
     * Cria uma ação para a notificação que, quando clicada, abrirá o aplicativo "Controle de Notificações".
     * Esta ação permite que o usuário acesse diretamente o histórico de notificações bloqueadas
     * ou gerencie as configurações do app após ser informado sobre notificações recebidas durante o bloqueio.
     *
     * @param context O contexto usado para obter o pacote do aplicativo e criar o Intent.
     */
    private fun createOpenMyAppAction(context: Context): NotificationCompat.Action {

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return createFallbackAction(context, context.getString(R.string.App_indispon_vel))

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
// TODO: deve ir direto pro historico do app emquestao
        return NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            context.getString(R.string.Ver_hist_rico),
            pendingIntent
        ).build()
    }

    /**
     * Cria uma ação de notificação de fallback com um PendingIntent vazio.
     * Isso é útil para casos em que a ação principal (abrir um app, por exemplo) não pode ser criada,
     * mas ainda queremos mostrar um rótulo na notificação para indicar um estado ou fornecer informações.
     * Ação vazia para evitar crashes caso o intent principal não possa ser criado
     * @param label O texto a ser exibido no botão da ação.
     */
    private fun createFallbackAction(context: Context, label: String): NotificationCompat.Action {

        val emptyIntent = PendingIntent.getActivity(
            context,
            2,
            Intent(), // Vazio
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            label,
            emptyIntent
        ).build()
    }
}
