package dev.gmarques.controledenotificacoes.presentation.ui.dialogs

import android.content.DialogInterface
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.Rule
import kotlinx.coroutines.Job

/**
 * Criado por Gilian Marques
 * Em terça-feira, 06 de maio de 2025 as 17:06.
 */
class ConfirmRuleRemovalDialog(
    private val fragment: Fragment,
    private val rule: Rule,
    private val callback: (Rule) -> Job,
) {
    init {
        confirmRemoveRule()
    }

    private fun confirmRemoveRule() {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(fragment.getString(R.string.Por_favor_confirme))
            .setMessage(fragment.getString(R.string.Ao_remover_um_regra_todos_os_aplicativos_gerenciados))
            .setPositiveButton(
                fragment.getString(R.string.Remover_regra),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        callback(rule)
                    }
                })
            .setNegativeButton(
                fragment.getString(R.string.Cancelar),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }
                })
            .setCancelable(false)
            .setIcon(R.drawable.vec_alert)
            .show()
    }

}