package dev.gmarques.controledenotificacoes.presentation.ui.select_apps_fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.databinding.ItemAppBinding
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 09:19.
 */
class AppsAdapter(
    private val onItemCheck: (InstalledApp, Boolean) -> Unit,
) : ListAdapter<InstalledApp, AppsAdapter.AppViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(installedApp: InstalledApp) = with(binding) {
            tvStartDe.text = installedApp.name
            ivAppIcon.setImageDrawable(installedApp.icon) // usar o glide para carregar
            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.isChecked = false

            parent.setOnClickListener {
                cbSelect.isChecked = !cbSelect.isChecked
            }

            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                onItemCheck(installedApp, isChecked)
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