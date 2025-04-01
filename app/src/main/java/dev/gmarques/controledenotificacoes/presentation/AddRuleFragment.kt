package dev.gmarques.controledenotificacoes.presentation

import TimeIntervalValidator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentAddRuleBinding
import dev.gmarques.controledenotificacoes.databinding.ItemIntervalBinding
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.utils.TimeIntervalExtensionFun.endIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.utils.TimeIntervalExtensionFun.startIntervalFormatted
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.addViewWithTwoStepsAnimation
import kotlinx.coroutines.launch

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
        setupButtonAddInterval()

        // TODO: remover isso
        addIntervalView(TimeInterval(1,2,3,4))
        addIntervalView(TimeInterval(1,2,3,4))
        addIntervalView(TimeInterval(1,2,3,4))

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


            if (tvRuleTypeInfo.isGone) {
                val parent = tvRuleTypeInfo.parent as ViewGroup
                lifecycleScope.launch { parent.addViewWithTwoStepsAnimation(tvRuleTypeInfo) }
            }

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

    private fun setupButtonAddInterval() = with(binding) {
        ivAddInterval.setOnClickListener(AnimatedClickListener {
            addNewInterval()
        })
    }

    private fun addNewInterval() {
        val data = intArrayOf(8, 0, 18, 0)

        val collectEndValues = {
            showTimePicker(data[2], data[3], false) { hour, minute ->
                data[2] = hour
                data[3] = minute
                validateInterval(TimeInterval(data[0], data[1], data[2], data[3]))
            }
        }

        val collectStartValues = {
            showTimePicker(data[0], data[1], true) { hour, minute ->
                data[0] = hour
                data[1] = minute
                collectEndValues()
            }
        }

        collectStartValues()
    }

    private fun validateInterval(interval: TimeInterval) {
        val validationResult = TimeIntervalValidator.validate(interval)
        if (validationResult.isSuccess) {
            viewModel.timeIntervals.add(interval)
            // TODO: salvar o valor no viewmodel que deve disparar um update pra atualizar a ui reativamente
            // setar a ide do timeinterval como tag na view pra facilicar a remoçao quando necessario

                        addIntervalView(interval)

        }
    }

    private fun addIntervalView(interval: TimeInterval) {

        with(ItemIntervalBinding.inflate(layoutInflater)) {

            tvStart.text = interval.startIntervalFormatted()
            tvEnd.text = interval.endIntervalFormatted()

            ivRemove.setOnClickListener(AnimatedClickListener {
                viewModel.timeIntervals.remove(interval)
                this@AddRuleFragment.binding.llConteinerIntervals.removeView(root)
            })

            lifecycleScope.launch { this@AddRuleFragment.binding.llConteinerIntervals.addViewWithTwoStepsAnimation(root) }
        }
    }

    private fun showTimePicker(hour: Int, minute: Int, isStartTime: Boolean, callback: (Int, Int) -> Unit) {

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(
                if (isStartTime) getString(R.string.Selecione_o_in_cio_do_intervalo_de_tempo) else getString(R.string.Selecione_o_fim_do_intervalo_de_tempo)
            )
            .build()

        picker.isCancelable = false

        picker.addOnPositiveButtonClickListener {
            callback(picker.hour, picker.minute)
        }

        activity?.supportFragmentManager?.let { picker.show(it, "TimePicker") }
    }
}
