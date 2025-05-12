package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.gmarques.controledenotificacoes.databinding.DialogTimeIntervalBinding
import dev.gmarques.controledenotificacoes.domain.framework.VibratorInterface
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import nl.joery.timerangepicker.TimeRangePicker

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 09 de maio de 2025 as 14:20.
 */
class TimeRangeDialogManager(
    private val context: Context,
    private val inflater: LayoutInflater,
    private val onRangeSelected: (TimeRange) -> Unit,

) {

    private var startPeriod = TimeRangePicker.Time(8, 0)
    private var endPeriod = TimeRangePicker.Time(18, 0)
    private lateinit var binding: DialogTimeIntervalBinding
    private var doNothingOnTextChanged = false

    fun show() {
        binding = DialogTimeIntervalBinding.inflate(inflater)
        setupPicker()
        setupListeners()
        setupEditTexts()

        MaterialAlertDialogBuilder(context).setView(binding.root).show().also { dialog ->
            binding.fabAdd.setOnClickListener(
                AnimatedClickListener {
                    onRangeSelected(
                        TimeRange(
                            startPeriod.hour, startPeriod.minute, endPeriod.hour, endPeriod.minute
                        )
                    )
                    dialog.dismiss()
                })
        }

        updateLabel()
    }

    @SuppressLint("SetTextI18n")
    private fun setupEditTexts() = with(binding) {
// TODO: isso pode ser otimizado

        edtStart.doOnTextChanged { text, _, _, _ ->

            if (doNothingOnTextChanged) return@doOnTextChanged

            if (text.toString().length == 2) {
                edtStart.setText("${text}:")
                edtStart.setSelection(3)
            }

            if (text.toString().length == 5) {
                val (hour, min) = text.toString().split(":").map { it.toInt() }
                binding.picker.startTime = TimeRangePicker.Time(hour, min)
                startPeriod = TimeRangePicker.Time(hour, min)
                edtEnd.requestFocus()
            }
        }

        edtEnd.doOnTextChanged { text, _, _, _ ->
            if (doNothingOnTextChanged) return@doOnTextChanged

            if (text.toString().length == 2) {
                edtEnd.setText("${text}:")
                edtEnd.setSelection(3)
            }

            if (text.toString().length == 5) {
                val (hour, min) = text.toString().split(":").map { it.toInt() }
                binding.picker.endTime = TimeRangePicker.Time(hour, min)
                endPeriod = TimeRangePicker.Time(hour, min)
                edtEnd.clearFocus()
            }

        }

    }

    private fun setupPicker() {
        try {
            val hourFormatClass = Class.forName("nl.joery.timerangepicker.TimeRangePicker\$HourFormat")
            val format24Field = hourFormatClass.getField("FORMAT_24")
            val format24Value = format24Field.get(null)

            val field = binding.picker.javaClass.getDeclaredField("_hourFormat")
            field.isAccessible = true
            field.set(binding.picker, format24Value)

            binding.picker.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        binding.picker.setOnTimeChangeListener(object : TimeRangePicker.OnTimeChangeListener {
            override fun onStartTimeChange(startTime: TimeRangePicker.Time) {
                startPeriod = startTime
                updateLabel()
            }

            override fun onEndTimeChange(endTime: TimeRangePicker.Time) {
                endPeriod = endTime
                updateLabel()
            }

            override fun onDurationChange(duration: TimeRangePicker.TimeDuration) {
                startPeriod = duration.start
                endPeriod = duration.end
                updateLabel()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateLabel() = with(binding) {
        doNothingOnTextChanged = true
        edtStart.setText("%02d:%02d".format(startPeriod.hour, startPeriod.minute))
        edtEnd.setText("%02d:%02d".format(endPeriod.hour, endPeriod.minute))
        doNothingOnTextChanged = false
    }
}
