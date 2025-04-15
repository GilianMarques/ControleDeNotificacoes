package dev.gmarques.controledenotificacoes.presentation.ui.managedapp_fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.databinding.ItemAppBinding
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 09:19.
 */
class AppAdapter(
    private val onItemCheck: (InstalledApp) -> Unit,
) : ListAdapter<InstalledApp, AppAdapter.AppViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(installedApp: InstalledApp) {
            binding.tvStartDe.text = installedApp.name
            binding.ivAppIcon.setImageDrawable(installedApp.icon)
            binding.cbSelect.setOnCheckedChangeListener(null)
            binding.cbSelect.isChecked = false

            binding.cbSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) onItemCheck(installedApp)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<InstalledApp>() {
        override fun areItemsTheSame(oldItem: InstalledApp, newItem: InstalledApp): Boolean {
            return oldItem.packageId == newItem.packageId
        }

        override fun areContentsTheSame(oldItem: InstalledApp, newItem: InstalledApp): Boolean {
            return oldItem == newItem
        }
    }
}