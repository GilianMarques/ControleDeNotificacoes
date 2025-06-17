package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_update_rule

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.gmarques.controledenotificacoes.databinding.DialogTimeIntervalBinding
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.showKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 * Criado por Gilian Marques
 * Em sexta-feira, 09 de maio de 2025 as 14:20.
 *
 * Gerencia um diálogo para seleção de intervalo de tempo.
 *
 * @property context Contexto da aplicação.
 * @property inflater Inflater usado para inflar o layout do diálogo.
 * @property onRangeSelected Callback disparado ao confirmar o intervalo de tempo.
 */
class TimeRangeDialogManager(
    private val context: Context,
    private val inflater: LayoutInflater,
    private val onRangeSelected: (TimeRange) -> Unit,
) {

    private var startHour = 8
    private var startMinute = 0
    private var endHour = 18
    private var endMinute = 0

    private lateinit var binding: DialogTimeIntervalBinding
    private lateinit var dialog: AlertDialog
    private var ignoreTextChange = false

    /** Exibe o diálogo configurado com os campos e listeners. */
    fun show() {
        binding = DialogTimeIntervalBinding.inflate(inflater)
        dialog = MaterialAlertDialogBuilder(context).setView(binding.root).show()

        setupButtons()
        setupEditTexts()
        updateEditTexts()
        requestKeyboardFocus()
    }

    /** Define o comportamento dos botões do diálogo. */
    private fun setupButtons() = with(binding) {

        fabAdd.setOnClickListener(AnimatedClickListener {
            clearFocusFromInputs()

            if (startHour == 0
                && startMinute == 0
                && endHour == 23
                && endMinute == 59
            ) onRangeSelected(TimeRange(true))
            else onRangeSelected(TimeRange(startHour, startMinute, endHour, endMinute))

            dialog.dismiss()
        })

        fab24h.setOnClickListener(AnimatedClickListener {
            onRangeSelected(TimeRange(true))
            dialog.dismiss()
        })
    }

    /** Configura os campos de entrada de horário para responder a eventos de texto e foco. */
    @SuppressLint("SetTextI18n")
    private fun setupEditTexts() = with(binding) {
        setupTimeEditText(edtStart) { hour, min ->
            startHour = hour
            startMinute = min
            edtEnd.requestFocus()
        }

        setupTimeEditText(edtEnd) { hour, min ->
            endHour = hour
            endMinute = min
            edtStart.clearFocus()
        }
    }

    /**
     * Configura o EditText para lidar com formatação e parse do horário.
     * @param field EditText a ser configurado.
     * @param onComplete Callback chamado quando o horário completo é digitado.
     */
    @SuppressLint("SetTextI18n")
    private fun setupTimeEditText(
        field: android.widget.EditText,
        onComplete: (Int, Int) -> Unit,
    ) {
        field.doOnTextChanged { text, _, _, _ ->
            if (ignoreTextChange) return@doOnTextChanged
            val str = text.toString()
            if (str.length == 2 && !str.contains(":")) {
                field.setText("$str:")
                field.setSelection(3)
            } else if (str.length == 5 && str.contains(":")) {
                val (hour, min) = str.split(":").mapNotNull { it.toIntOrNull() }
                onComplete(hour, min)
            }
        }

        field.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val text = field.text.toString()
                if (text.length == 1) field.setText("0${text}:00")
                if (text.length == 3) field.setText("${text}00")
                if (text.length == 4) field.setText("${text}0")
            }
        }
    }

    /** Atualiza os EditTexts com os valores atuais do período. */
    @SuppressLint("SetTextI18n")
    private fun updateEditTexts() = with(binding) {
        ignoreTextChange = true
        edtStart.setText("%02d:%02d".format(startHour, startMinute))
        edtEnd.setText("%02d:%02d".format(endHour, endMinute))
        ignoreTextChange = false
    }

    /** Solicita o foco e exibe o teclado para o campo inicial após breve atraso. */
    private fun requestKeyboardFocus() {
        CoroutineScope(Main).launch {
            delay(200)
            binding.edtStart.showKeyboard()
        }
    }

    /** Remove o foco dos campos de horário. */
    private fun clearFocusFromInputs() = with(binding) {
        edtStart.clearFocus()
        edtEnd.clearFocus()
    }
}
