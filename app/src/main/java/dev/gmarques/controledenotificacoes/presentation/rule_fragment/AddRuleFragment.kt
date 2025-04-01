package dev.gmarques.controledenotificacoes.presentation.rule_fragment

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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentAddRuleBinding
import dev.gmarques.controledenotificacoes.databinding.ItemIntervalBinding
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.validators.RuleValidator
import dev.gmarques.controledenotificacoes.domain.utils.TimeIntervalExtensionFun.endIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.utils.TimeIntervalExtensionFun.startIntervalFormatted
import dev.gmarques.controledenotificacoes.framework.Vibrator
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
        observeTimeIntervals()

        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * Configura o comportamento dos chips dentro do `chipGroup` para habilitar uma animação visual quando seu estado de seleção é alterado.
     *
     * Quando o estado de seleção de um chip é modificado, ele é brevemente removido e, em seguida, readicionado ao `chipGroup` na sua posição original.
     * Esse processo dispara uma animação, fornecendo feedback visual ao usuário.
     *
     * Este méto.do itera sobre cada `Chip` do `chipGroup` e associa um `setOnCheckedChangeListener` a ele. O listener executa as seguintes ações:
     *
     * 1. **Obter Índice Original:** Determina o índice atual do chip dentro do `chipGroup`.
     * 2. **Remover Chip:** Remove temporariamente o chip do `chipGroup`.
     * 3. **Readicionar Chip:** Adiciona o chip de volta ao `chipGroup` no índice previamente determinado.
     *
     * Essa operação de remover e readicionar, no mesmo índice, força o layout a ser redesenhado, criando um efeito de animação visual sutil.
     *
     * Pressupostos:
     * - `binding` é um objeto `ViewBinding` válido.
     * - Todas as filhas do `chipGroup` são instâncias de `Chip`.
     */
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

    /**
     * Configura o listener de clique para o botão "Adicionar Intervalo" (ivAddInterval).
     *
     * Esta função configura o `ivAddInterval` (presumivelmente um ImageView ou similar) para
     * acionar a função `collectIntervalData()` quando clicado. Ela usa um `AnimatedClickListener`
     * para fornecer feedback visual ao evento de clique.
     *
     * O `AnimatedClickListener` fornece uma animação de escala padrão.
     *
     * @see AnimatedClickListener Para detalhes sobre a animação aplicada ao clique.
     * @see collectIntervalData A função chamada quando o botão é clicado.
     */
    private fun setupButtonAddInterval() = with(binding) {
        ivAddInterval.setOnClickListener(AnimatedClickListener {
            if ((viewModel.timeIntervalsLiveData.value?.size ?: 0) < RuleValidator.MAX_INTERVALS) {
                collectIntervalData()
            } else {
            showErrorSnackBar(getString(R.string.O_limite_m_ximo_de_intervalos_de_tempo_foi_atingido, RuleValidator.MAX_INTERVALS))
            }
        })
    }

    /**
     * Coleta e valida os dados de um intervalo de tempo do usuário através de um seletor de tempo (time picker).
     *
     * Esta função orquestra o processo de coleta dos horários de início e fim para um intervalo de tempo.
     * Ela utiliza um seletor de tempo para obter os valores de hora e minuto do usuário. Primeiramente,
     * solicita ao usuário que selecione o horário de início. Após a seleção do horário de início,
     * solicita ao usuário que selecione o horário de fim. Finalmente, após ambos os horários de início
     * e fim terem sido selecionados, ela valida se o intervalo selecionado é válido.
     *
     * Os dados de tempo são armazenados em um array de inteiros `data` com a seguinte estrutura:
     * - `data[0]`: Hora de início (0-23)
     * - `data[1]`: Minuto de início (0-59)
     * - `data[2]`: Hora de fim (0-23)
     * - `data[3]`: Minuto de fim (0-59)
     *
     * A função utiliza duas funções lambda:
     * - `collectEndValues`: Coleta o horário de fim (hora e minuto) usando o seletor de tempo e atualiza os elementos correspondentes no array `data`. Após a coleta do horário de fim, ela chama `validateInterval` para validar o intervalo completo.
     * - `collectStartValues`: Coleta o horário de início (hora e minuto) usando o seletor de tempo e atualiza os elementos correspondentes no array `data`. Após a coleta do horário de início, ela então chama `collectEndValues` para coletar o horário de fim.
     *
     * Finalmente, a função chama `collectStartValues` para iniciar o processo.
     *
     * @throws IllegalStateException Se a função `showTimePicker` não estiver devidamente definida ou implementada na classe circundante.
     * @see showTimePicker
     * @see validateInterval
     * @see TimeInterval
     */
    private fun collectIntervalData() {
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

    /**
     * Exibe um diálogo Material Time Picker.
     *
     * Esta função exibe um diálogo de seletor de hora no formato de 24 horas, permitindo que o usuário
     * selecione um horário. Ela também define um título que depende se o usuário está selecionando a hora de início ou de fim.
     *
     * @param hour A hora inicial a ser exibida no seletor de hora.
     * @param minute O minuto inicial a ser exibido no seletor de hora.
     * @param isStartTime Um booleano que indica se este seletor de hora é para selecionar a hora de início (true) ou a hora de fim (false).
     * @param callback Uma função lambda que será chamada quando o usuário confirmar sua seleção.
     *                 Ela recebe dois parâmetros:
     *                 - A hora selecionada (Int).
     *                 - O minuto selecionado (Int).
     *
     * @throws IllegalStateException se a activity for nula. Isso pode acontecer se o fragmento não estiver anexado a uma activity.
     * @sample
     * ```kotlin
     *   showTimePicker(10, 30, true) { horaSelecionada, minutoSelecionado ->
     *       // Lidar com o horário selecionado (horaSelecionada:minutoSelecionado)
     *       Log.d("TimePicker", "Horário Selecionado: $horaSelecionada:$minutoSelecionado")
     *   }
     * ```
     * @see MaterialTimePicker
     * @see TimeFormat
     */
    private fun showTimePicker(
        hour: Int,
        minute: Int,
        isStartTime: Boolean,
        callback: (Int, Int) -> Unit,
    ) {

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(
                if (isStartTime) getString(R.string.Selecione_o_in_cio_do_intervalo_de_tempo) else getString(
                    R.string.Selecione_o_fim_do_intervalo_de_tempo
                )
            )
            .build()

        picker.isCancelable = false

        picker.addOnPositiveButtonClickListener {
            callback(picker.hour, picker.minute)
        }

        activity?.supportFragmentManager?.let { picker.show(it, "TimePicker") }
    }

    private fun validateInterval(interval: TimeInterval) {
        val validationResult = TimeIntervalValidator.validate(interval)
        if (validationResult.isSuccess) {
            viewModel.addTimeInterval(interval)
        } else {
            showErrorSnackBar(getString(R.string.O_intervalo_selecionado_era_inv_lido))
        }
    }

    /**
     * Exibe um Snackbar de erro com a mensagem de erro fornecida e aciona uma vibração como feedback.
     *
     * Esta função é uma utilidade para mostrar mensagens de erro não críticas ao usuário. Ela utiliza
     * o Snackbar do Android para uma exibição temporária da mensagem e a combina com uma breve vibração
     * para fornecer feedback adicional.
     *
     * @param errorMsg A mensagem de erro a ser exibida no Snackbar. Esta deve ser uma string concisa
     *                 explicando a natureza do erro ao usuário.
     *
     * @see Snackbar
     * @see Vibrator
     */
    private fun showErrorSnackBar(errorMsg: String) {
        Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
        Vibrator.error()
    }

    /**
     * Observa as mudanças nos dados de intervalos de tempo provenientes da ViewModel.
     *
     * Esta função se inscreve na `timeIntervalsLiveData` na ViewModel. Sempre que a
     * LiveData emite uma nova lista de intervalos de tempo, ela aciona a função
     * `manageIntervalViews` para atualizar a UI de acordo.
     *
     * Este método é tipicamente chamado durante a configuração do ciclo de vida do
     * Fragment/Activity, como no método `onViewCreated` de um Fragment ou no
     * método `onCreate` de uma Activity.
     *
     * Ele usa `viewLifecycleOwner` como o LifecycleOwner, garantindo que o observador
     * seja removido automaticamente quando a view do Fragment for destruída,
     * evitando vazamentos de memória.
     *
     * @see viewModel.timeIntervalsLiveData O objeto LiveData na ViewModel que contém os intervalos de tempo.
     * @see manageIntervalViews A função responsável por lidar com as atualizações da UI com base nos novos intervalos de tempo.
     * @see viewLifecycleOwner O proprietário do ciclo de vida (LifecycleOwner) vinculado ao ciclo de vida da view do Fragment.
     */
    private fun observeTimeIntervals() {
        viewModel.timeIntervalsLiveData.observe(viewLifecycleOwner) {
            manageIntervalViews(it)
        }
    }

    /**
     * Tentei usar um RecyclerView para lidar com o dinamismo das views de TimeIntervals, mas ele não anima bem quando seu tamanho não é fixo.
     * Afim de favorecer a estética do app por meio de animações, retornei a essa abordagem manual para lidar com as views.
     *
     * Gerencia a exibição das views de TimeInterval na UI.
     *
     * Esta função adiciona e remove dinamicamente views representando objetos TimeInterval, com base no mapa `timeIntervals`.
     * Otimiza as transições visuais, manipulando manualmente a criação e remoção de views, em vez de usar um RecyclerView.
     *
     * Comportamento:
     * 1. **Remoção de Views Obsoletas:** Itera pelas views existentes em `onScreenViewsParent` (um LinearLayout) e verifica se o
     *    TimeInterval correspondente ainda existe no mapa `timeIntervals`. Se uma view não tiver um TimeInterval associado no mapa,
     *    ela é removida do layout.
     *
     * 2. **Adição de Novas Views:** Itera sobre o mapa `timeIntervals`. Para cada TimeInterval, verifica se já existe uma view
     *    correspondente na tela. Se não, uma nova view é criada e adicionada ao layout.
     *
     * 3. **Criação e Configuração de Views:** Novas views são criadas usando data binding (`ItemIntervalBinding`). Os horários de
     *    início e fim do TimeInterval são exibidos na view. Uma ação de "remover" é definida na view. Cada view é marcada com o
     *    ID do TimeInterval para fácil identificação.
     *
     * 4. **Adição Animada:** Novas views são adicionadas ao `onScreenViewsParent` usando a função personalizada
     *    `addViewWithTwoStepsAnimation`, que fornece um efeito de animação visualmente atraente.
     *
     * @param timeIntervals Um HashMap onde a chave é o ID do TimeInterval (String) e o valor é o objeto TimeInterval
     *                      correspondente. Este mapa representa o conjunto atual de TimeIntervals a serem exibidos.
     */
    private fun manageIntervalViews(timeIntervals: HashMap<String, TimeInterval>) {

        val onScreenViewsParent = this@AddRuleFragment.binding.llConteinerIntervals

        //removo as views cujo timeinterval foi removido
        onScreenViewsParent.children.forEach {

            if (timeIntervals[it.tag] == null) {
                this@AddRuleFragment.binding.llConteinerIntervals.removeView(it)
            }
        }

        // adiciono views para os timeintervals que nao tem uma view na tela
        timeIntervals.values.forEach { interval ->

            var addView = true
            for (child in onScreenViewsParent.children) {
                if (child.tag == interval.id) {
                    addView = false
                    break
                }
            }

            if (addView) with(ItemIntervalBinding.inflate(layoutInflater)) {

                tvStart.text = interval.startIntervalFormatted()
                tvEnd.text = interval.endIntervalFormatted()

                ivRemove.setOnClickListener(AnimatedClickListener {
                    viewModel.removeTimeIntervals(interval)
                })
                root.tag = interval.id

                this@AddRuleFragment.binding.llConteinerIntervals
                    .addViewWithTwoStepsAnimation(root)


            }
        }
    }

}
