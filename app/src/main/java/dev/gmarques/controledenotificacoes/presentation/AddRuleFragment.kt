package dev.gmarques.controledenotificacoes.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentAddRuleBinding
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType

class AddRuleFragment : Fragment() {


    private val viewModel: AddRuleViewModel by viewModels()
    private lateinit var binding: FragmentAddRuleBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddRuleBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupButtonTypeRule()
        setupChipDays()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupChipDays() = with(binding) {

        for (view in chipGroup.children) {
            val chip = view as Chip
            chip.setOnCheckedChangeListener { buttonView, _ ->
                val index = chipGroup.indexOfChild(buttonView)
                chipGroup.removeView(buttonView)
                chipGroup.addView(buttonView, index)
            }
        }
    }

    private fun setupButtonTypeRule() = with(binding) {
        mbtTypeRule.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            tvRuleTypeInfo.visibility = VISIBLE

            when (toggleButton.checkedButtonId) {
                R.id.btn_permissive -> {
                    viewModel.ruleType = RuleType.PERMISSIVE
                    tvRuleTypeInfo.text =
                        getString(R.string.Permite_mostrar_as_notifica_es_nos_dias_e_horarios_selecionados)
                }

                R.id.btn_restritive -> {
                    viewModel.ruleType = RuleType.RESTRITIVE
                    tvRuleTypeInfo.text =
                        getString(R.string.As_notifica_es_ser_o_bloqueadas_nos_dias_e_hor_rios_selecionados)
                }
            }
        }
    }


}
