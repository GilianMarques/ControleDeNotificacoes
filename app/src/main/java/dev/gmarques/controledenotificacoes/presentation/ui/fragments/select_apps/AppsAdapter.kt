package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.databinding.ItemAppSelectableBinding
import dev.gmarques.controledenotificacoes.presentation.model.SelectableApp

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 09:19.
 */
class AppsAdapter(
    private val onItemCheck: (SelectableApp, Boolean) -> Unit,
) : ListAdapter<SelectableApp, AppsAdapter.AppViewHolder>(DiffCallback()) {

    private var blockSelection = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppSelectableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setBlockSelection(block: Boolean) {
        this.blockSelection = block
    }

    fun submitList(apps: List<SelectableApp>, query: String) {
        submitList(apps.filter {
            it.installedApp.name.contains(query, ignoreCase = true)
        })

    }


    inner class AppViewHolder(private val binding: ItemAppSelectableBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(selectedApp: SelectableApp) = with(binding) {

            cbSelect.setOnCheckedChangeListener(null)

            tvStartDe.text = selectedApp.installedApp.name
            ivAppIcon.setImageDrawable(selectedApp.installedApp.icon)
            cbSelect.isChecked = selectedApp.isSelected

            parent.setOnClickListener {
                cbSelect.isChecked = !cbSelect.isChecked
            }

            cbSelect.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked && blockSelection) cbSelect.isChecked = false

                onItemCheck(selectedApp, isChecked)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SelectableApp>() {

        override fun areItemsTheSame(oldItem: SelectableApp, newItem: SelectableApp): Boolean {
            return oldItem.installedApp.packageId == newItem.installedApp.packageId
                    && oldItem.isSelected == newItem.isSelected

        }

        override fun areContentsTheSame(oldItem: SelectableApp, newItem: SelectableApp): Boolean {
            return oldItem == newItem
        }
    }
}