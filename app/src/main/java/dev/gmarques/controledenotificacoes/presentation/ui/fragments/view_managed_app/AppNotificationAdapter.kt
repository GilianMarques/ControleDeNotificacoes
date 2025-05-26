package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.databinding.ItemAppNotificationBinding
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.timeFormatted

/**
 * Criado por Gilian Marques
 * Em terça-feira, 13 de maio de 2025 as 16:06.
 */
class AppNotificationAdapter : ListAdapter<AppNotification, AppNotificationAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemAppNotificationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: AppNotification) = with(binding) {
            tvTitle.text = notification.title
            tvContent.text = notification.content
            tvTime.text = notification.timeFormatted()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AppNotification>() {
        override fun areItemsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.packageId == newItem.packageId
        }

        override fun areContentsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean {
            return oldItem == newItem
        }
    }
}
