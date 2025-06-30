package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_notification

import android.app.Notification
import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.databinding.ItemAppNotificationBinding

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:17.
 */
class NotificationsAdapter(
    private val onItemClick: (StatusBarNotification) -> Unit,
) : ListAdapter<StatusBarNotification, NotificationsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAppNotificationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sbn: StatusBarNotification) = with(binding) {
            val extras = sbn.notification.extras

            tvTitle.text = sbn.packageName
            tvContent.text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            tvTime.text = DateUtils.getRelativeTimeSpanString(sbn.postTime)

            ivAppIcon.setImageDrawable(sbn.notification.smallIcon?.loadDrawable(binding.root.context))

            val largeIcon = extras.getParcelable<Icon>(Notification.EXTRA_LARGE_ICON)
            if (largeIcon != null) {
                ivLargeIcon.isVisible = true
                ivLargeIcon.setImageIcon(largeIcon)
            } else {
                ivLargeIcon.isVisible = false
            }

            tvOpenNotification.visibility = View.GONE

            parent.setOnClickListener {
                onItemClick(sbn)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StatusBarNotification>() {
        override fun areItemsTheSame(oldItem: StatusBarNotification, newItem: StatusBarNotification): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: StatusBarNotification, newItem: StatusBarNotification): Boolean {
            return oldItem.postTime == newItem.postTime
                    && oldItem.packageName == newItem.packageName
        }
    }
}
