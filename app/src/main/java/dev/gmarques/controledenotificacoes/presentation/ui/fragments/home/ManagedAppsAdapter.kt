package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.databinding.ItemManagedAppBinding
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.setRuleDrawable

/**
 * Adapter responsável por exibir a lista de aplicativos controlados na RecyclerView.
 * Criado por Gilian Marques
 * Em sábado, 26 de abril de 2025 as 17:48.
 */
class ManagedAppsAdapter(
    private val iconPermissive: Drawable,
    private val iconRestrictive: Drawable,
    private val getName: (Rule) -> String,
    private val onItemClick: (ManagedAppWithRule) -> Unit,
) :
    ListAdapter<ManagedAppWithRule, ManagedAppsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemManagedAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(iconPermissive, iconRestrictive, getName, getItem(position), onItemClick)
    }

    fun submitList(apps: List<ManagedAppWithRule>, query: String) {
        submitList(apps.filter {
            it.name.contains(query, ignoreCase = true)
        })
    }

    class ViewHolder(private val binding: ItemManagedAppBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            iconPermissive: Drawable,
            iconRestrictive: Drawable,
            getName: (Rule) -> String,
            app: ManagedAppWithRule,
            onItemClick: (ManagedAppWithRule) -> Unit,
        ) {
            binding.tvAppName.text = app.name
            binding.tvRuleName.text = getName(app.rule)
            binding.tvRuleName.setRuleDrawable(if (app.rule.ruleType == RuleType.PERMISSIVE) iconPermissive else iconRestrictive)
            binding.ivAppIcon.setImageDrawable(app.icon)

            binding.root.setOnClickListener(AnimatedClickListener {
                onItemClick(app)
            })
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ManagedAppWithRule>() {
        override fun areItemsTheSame(oldItem: ManagedAppWithRule, newItem: ManagedAppWithRule) = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: ManagedAppWithRule, newItem: ManagedAppWithRule) = oldItem == newItem
    }
}
