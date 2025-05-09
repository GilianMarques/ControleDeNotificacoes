package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.ViewDialogTimeIntervalBinding
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

    fun show() {
        val binding = ViewDialogTimeIntervalBinding.inflate(inflater)
        setupPicker(binding)
        setupListeners(binding)

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

        updateLabel(binding)
    }

    private fun setupPicker(binding: ViewDialogTimeIntervalBinding) {
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

    private fun setupListeners(binding: ViewDialogTimeIntervalBinding) {
        binding.picker.setOnTimeChangeListener(object : TimeRangePicker.OnTimeChangeListener {
            override fun onStartTimeChange(startTime: TimeRangePicker.Time) {
                startPeriod = startTime
                updateLabel(binding)
            }

            override fun onEndTimeChange(endTime: TimeRangePicker.Time) {
                endPeriod = endTime
                updateLabel(binding)
            }

            override fun onDurationChange(duration: TimeRangePicker.TimeDuration) {
                startPeriod = duration.start
                endPeriod = duration.end
                updateLabel(binding)
            }
        })
    }

    private fun updateLabel(binding: ViewDialogTimeIntervalBinding) {
        binding.tvIntervalInfo.text = context.getString(
            R.string.De_x_as_y,
            "%02d:%02d".format(startPeriod.hour, startPeriod.minute),
            "%02d:%02d".format(endPeriod.hour, endPeriod.minute)
        )
    }
}
