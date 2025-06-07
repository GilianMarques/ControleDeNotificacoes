package dev.gmarques.controledenotificacoes.framework.report_notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.GetAppNotificationByPkgUseCase
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 24 de maio de 2025 as 15:52.
 *
 * Constrói e exibe notificações de relatório de notificações recebidas durante o bloqueio.
 */
class ReportNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAppNotificationByPkgUseCase: GetAppNotificationByPkgUseCase,
) {

    private val channelId = "notification_report"

    fun showReportNotification(packageName: String) {
        createNotificationChannelIfNeeded()

        val id = packageName.hashCode()
        val notification = buildNotification(packageName, id)
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        notificationManager.notify(id, notification)
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.Relatorio_de_notificacoes)
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(packageName: String, notificationId: Int): Notification {

        val appName = getAppNameFromPackage(packageName)
        val appIcon = getAppIconFromPackage(packageName)

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(appName)
            .setContentText(context.getString(R.string.X_recebeu_notifica_es_durante_o_bloqueio, appName))
            .setLargeIcon(appIcon)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(false)
            .setAutoCancel(true)
            .setGroup("${System.currentTimeMillis()}")
            .setGroupSummary(false)
            .addAction(createOpenTargetAppAction(packageName, notificationId))
            .addAction(createOpenNotificationHistoryAction(packageName, notificationId))
            .build()
    }

    private fun getAppIconFromPackage(packageName: String): Bitmap? = try {
        val drawable = context.packageManager.getApplicationIcon(packageName)
        if (drawable is BitmapDrawable) drawable.bitmap
        else {
            val bitmap = createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1)
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    } catch (e: Exception) {
        null
    }

    private fun getAppNameFromPackage(packageName: String): String = try {
        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        context.packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        packageName
    }

    private fun createOpenTargetAppAction(packageName: String, notificationId: Int): NotificationCompat.Action {
        val intent = Intent(context, ReportNotificationReceiver::class.java).apply {
            putExtra(ReportNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ReportNotificationReceiver.EXTRA_TARGET_PACKAGE, packageName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            context.getString(R.string.Abrir_app),
            pendingIntent
        ).build()
    }

    private fun createOpenNotificationHistoryAction(packageName: String, notificationId: Int): NotificationCompat.Action {
        val targetIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.viewManagedAppFragment)
            .setArguments(bundleOf("packageId" to packageName))
            .createTaskStackBuilder()
            .intents
            .first()

        return createBroadcastIntentAction(context.getString(R.string.Ver_hist_rico), targetIntent, notificationId)
    }

    private fun createBroadcastIntentAction(label: String, targetIntent: Intent, notificationId: Int): NotificationCompat.Action {
        val broadcastIntent = Intent(context, ReportNotificationReceiver::class.java).apply {
            putExtra(ReportNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ReportNotificationReceiver.EXTRA_ORIGINAL_INTENT, targetIntent)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            label,
            pendingIntent
        ).build()
    }
}
