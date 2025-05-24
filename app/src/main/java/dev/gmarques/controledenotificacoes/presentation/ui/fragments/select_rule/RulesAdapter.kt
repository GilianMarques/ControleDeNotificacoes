package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_rule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.databinding.ItemRuleBinding
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nameOrDescription
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.DomainRelatedExtFuns.getAdequateIconReference

/**
 * Criado por Gilian Marques
 * Em sábado, 19 de abril de 2025 as 15:14.
 */
class RulesAdapter(
    private val onRuleSelected: (Rule) -> Unit,
    private val onRuleMenuClick: (View, Rule) -> Unit,
) : ListAdapter<Rule, RulesAdapter.AppViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class AppViewHolder(private val binding: ItemRuleBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rule: Rule) = with(binding) {

            tvName.text = rule.nameOrDescription()
            ivIcon.setImageResource(rule.getAdequateIconReference())

            parent.setOnClickListener(AnimatedClickListener {
                onRuleSelected(rule)
            })

            ivMenu.setOnClickListener(AnimatedClickListener {
                onRuleMenuClick(binding.ivMenu, rule)
            })

        }


    }

    class DiffCallback : DiffUtil.ItemCallback<Rule>() {

        override fun areItemsTheSame(oldItem: Rule, newItem: Rule): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: Rule, newItem: Rule): Boolean {
            return oldItem == newItem
        }
    }
}