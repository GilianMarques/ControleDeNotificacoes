package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.databinding.ItemAppNotificationBinding
import dev.gmarques.controledenotificacoes.presentation.model.ActiveStatusBarNotification

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:17.
 */
class NotificationsAdapter(
    private val onItemClick: (ActiveStatusBarNotification) -> Unit,
) : ListAdapter<ActiveStatusBarNotification, NotificationsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAppNotificationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ActiveStatusBarNotification) = with(binding) {

            tvTitle.text = notification.title

            tvContent.text = notification.content
            tvContent.isVisible = true

            ivAppIcon.setImageIcon(notification.smallIcon)

            ivLargeIcon.setImageIcon(notification.largeIcon)
            ivLargeIcon.isVisible = notification.largeIcon != null

            tvOpenNotification.isVisible = false
            tvTime.isVisible = false

            parent.setOnClickListener {
                onItemClick(notification)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ActiveStatusBarNotification>() {
        override fun areItemsTheSame(oldItem: ActiveStatusBarNotification, newItem: ActiveStatusBarNotification): Boolean {
            return oldItem.postTime == newItem.postTime && oldItem.packageId == newItem.packageId
        }

        override fun areContentsTheSame(oldItem: ActiveStatusBarNotification, newItem: ActiveStatusBarNotification): Boolean {
            return oldItem.title == newItem.title &&
                    oldItem.content == newItem.content &&
                    oldItem.packageId == newItem.packageId &&
                    oldItem.postTime == newItem.postTime
        }
    }
}
