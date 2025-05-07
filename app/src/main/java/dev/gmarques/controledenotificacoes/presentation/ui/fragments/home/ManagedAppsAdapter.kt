package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dev.gmarques.controledenotificacoes.databinding.ItemManagedAppBinding
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.setRuleDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adapter responsável por exibir a lista de aplicativos controlados na RecyclerView.
 * Criado por Gilian Marques
 * Em sábado, 26 de abril de 2025 as 17:48.
 */
class ManagedAppsAdapter(
    private val iconPermissive: Drawable,
    private val iconRestrictive: Drawable,
    private val getInstalledAppIconUseCase: GetInstalledAppIconUseCase,
    private val getName: (Rule) -> String,
    private val onItemClick: (ManagedAppWithRule) -> Unit,
) :
    ListAdapter<ManagedAppWithRule, ManagedAppsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemManagedAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(iconPermissive, iconRestrictive, getInstalledAppIconUseCase, getName, getItem(position), onItemClick)
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
            getInstalledAppIconUseCase: GetInstalledAppIconUseCase,
            getName: (Rule) -> String,
            app: ManagedAppWithRule,
            onItemClick: (ManagedAppWithRule) -> Unit,
        ) {
            binding.tvAppName.text = app.name
            binding.tvRuleName.text = getName(app.rule)
            binding.tvRuleName.setRuleDrawable(if (app.rule.ruleType == RuleType.PERMISSIVE) iconPermissive else iconRestrictive)

            CoroutineScope(Main).launch {
                Glide.with(binding.ivAppIcon.context)
                    .load(getInstalledAppIconUseCase(app.packageId))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivAppIcon)

            }

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
