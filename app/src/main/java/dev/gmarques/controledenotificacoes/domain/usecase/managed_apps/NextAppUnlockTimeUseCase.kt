package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAllDayRule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.sortedRanges
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.NextAppUnlockTimeUseCase.Companion.INFINITE
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.NextAppUnlockTimeUseCase.Companion.REPEAT_COUNT
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.at
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.weekDayNumber
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.withSecondsAndMillisSetToZero
import org.joda.time.LocalDateTime
import javax.inject.Inject

/**
 *
 * Criado por Gilian Marques
 * Em terça-feira, 20 de maio de 2025 as 13:21.
 *
 *  Classe responsável por calcular o próximo horário em que um aplicativo será desbloqueado
 * com base nas regras de uso configuradas pelo usuário.
 *
 * As regras podem ser do tipo [dev.gmarques.controledenotificacoes.domain.model.enums.RuleType.RESTRICTIVE] (dias/horários de bloqueio)
 * ou [dev.gmarques.controledenotificacoes.domain.model.enums.RuleType.PERMISSIVE] (dias/horários de liberação).
 *
 */
class NextAppUnlockTimeUseCase @Inject constructor() {

    companion object {

        /**
         * Valor de retorno quando não há previsão de desbloqueio (bloqueio permanente).
         * Isso ocorre, por exemplo, quando a regra é do tipo [dev.gmarques.controledenotificacoes.domain.model.enums.RuleType.RESTRICTIVE]
         * e bloqueia 24h por dia, 7 dias por semana.
         */
        const val INFINITE = -1L

        /**
         * Quantidade máxima de dias que serão iterados ao buscar o próximo horário de desbloqueio.
         *
         * Pode ser necessário iterar até 8 dias em cenários onde o próximo desbloqueio ocorre
         * no mesmo dia da semana seguinte. Exemplo:
         * Uma regra permissiva válida às terças, das 08:00 às 18:00. Se hoje for terça-feira, 12:20,
         * será necessário iterar até a próxima terça-feira as 08:00 para encontrar o próximo horário válido.
         */
        const val REPEAT_COUNT = 8
    }

    private lateinit var baseDateTime: LocalDateTime

    /**
     * Calcula o timestamp (em milissegundos desde a Epoch) do próximo horário de desbloqueio de um aplicativo,
     * com base na [rule] informada.
     *
     * @param date [LocalDateTime] opcional que define o ponto de partida do cálculo (útil para testes).
     *             Em uso real, deixar em branco para usar o horário atual.
     * @param rule Regra que define os dias e horários permitidos ou bloqueados para o uso do aplicativo.
     * @return Timestamp do próximo horário de desbloqueio ou [INFINITE] se o app estiver bloqueado indefinidamente.
     */
    operator fun invoke(date: LocalDateTime = LocalDateTime.now(), rule: Rule): Long {
        baseDateTime = date.withSecondsAndMillisSetToZero()

        return when (rule.ruleType) {
            RuleType.RESTRICTIVE -> nextUnlockTimeRestrictive(rule)
            RuleType.PERMISSIVE -> nextUnlockTimePermissive(rule)
        }?.toDate()?.time ?: INFINITE
    }

    /**
     * Calcula a próxima data e hora em que o aplicativo poderá ser desbloqueado com base em uma [Rule] do tipo [RuleType.RESTRICTIVE].
     *
     * A lógica percorre os próximos dias a partir da data base (`baseDateTime`), considerando que a regra define os
     * **intervalos em que o app está bloqueado** (ou seja, fora dos intervalos, o app está desbloqueado).
     *
     * ### Funcionamento detalhado:
     *
     * 1. Converte os dias da semana presentes na regra em inteiros, representando os dias em que há **bloqueio**.
     * 2. Itera sobre os próximos 8 dias (ou menos, se encontrar um resultado antes):
     *    - Para cada dia:
     *        - Verifica se o dia atual está na lista de dias de bloqueio.
     *          - Se estiver, marca que já passou por um dia bloqueado.
     *          - Se **não estiver**, e **já passou por pelo menos um dia bloqueado**, retorna esse dia como o próximo desbloqueio (dia inteiro desbloqueado).
     *        - Se o dia atual estiver na lista de bloqueio:
     *            - Verifica se há algum **intervalo configurado** no qual o app deixe de estar bloqueado **dentro desse dia**.
     *            - Se existir tal intervalo, retorna esse horário como o próximo momento de desbloqueio.
     * 3. Caso nenhum desbloqueio seja encontrado nos próximos 8 dias, retorna `null`.
     *
     * @param rule A regra com os dias e intervalos de tempo nos quais o app estará bloqueado.
     *
     * @return Uma instância de [LocalDateTime] representando o próximo horário em que o app poderá ser desbloqueado.
     *         Retorna `null` se todos os dias futuros forem completamente bloqueados ou encadeados.
     *
     * ### Exemplo:
     * - Regra bloqueia segunda a sexta das 08:00 às 18:00.
     * - Hoje é segunda, 17:50.
     * - Resultado: Segunda às 18:01.
     */

    private fun nextUnlockTimeRestrictive(rule: Rule): LocalDateTime? {
        val blockDays = rule.days.map { it.dayNumber }
        var goneTroughABlockDay = false

        daysOfNextWeek().forEach { today ->
            val weekDayInt = today.weekDayNumber()

            if (weekDayInt in blockDays) goneTroughABlockDay = true
            else {
                if (goneTroughABlockDay) return today
                return@forEach
            }

            val nextUnlockTime = getUnlockPeriodForRestrictiveDay(today, rule)
            if (nextUnlockTime != null) return nextUnlockTime
        }
        return null
    }

    /**
     * Calcula o próximo momento em que o aplicativo poderá ser desbloqueado com base em uma [Rule] do tipo [RuleType.PERMISSIVE].
     *
     * A lógica percorre os próximos 8 dias a partir da data base (`baseDateTime`) para encontrar o **próximo intervalo permitido de uso**,
     * definido na regra. Ou seja, busca o próximo horário dentro dos dias e intervalos permitidos.
     *
     * ### Funcionamento detalhado:
     *
     * 1. Converte os dias permitidos da regra em inteiros (ex: segunda = 2).
     * 2. Itera sobre os próximos 8 dias:
     *    - Para cada dia:
     *        - Se o dia **não estiver nos dias permitidos**, marca que já passou por um dia de bloqueio e segue para o próximo.
     *        - Se o dia **estiver nos dias permitidos**:
     *            - Se a regra for do tipo "permitido o dia inteiro" e **ainda não passou por um dia de bloqueio**,
     *              ignora o dia (evita falso positivo — ex: o app já está dentro de um dia de permissão).
     *            - Caso contrário, verifica se há **intervalos definidos nesse dia** em que o app será desbloqueado.
     *            - Se encontrar, retorna esse horário como o próximo momento de desbloqueio.
     * 3. Caso nenhum horário seja encontrado nos próximos dias, retorna `null`.
     *
     * @param rule A regra com os dias e intervalos nos quais o app está permitido (fora disso, está bloqueado).
     *
     * @return Uma instância de [LocalDateTime] representando o próximo horário em que o app poderá ser desbloqueado.
     *         Retorna `null` se nenhum período de permissão estiver disponível nos próximos 8 dias.
     *
     * ### Exemplo:
     * - Regra permite terça e quinta das 10:00 às 12:00.
     * - Hoje é segunda-feira, 18:00.
     * - Resultado: Terça-feira às 10:00.
     */
    private fun nextUnlockTimePermissive(rule: Rule): LocalDateTime? {
        val allowedDays = rule.days.map { it.dayNumber }
        var goneTroughABlockDay = false

        daysOfNextWeek().forEach { today ->
            val weekDayInt = today.weekDayNumber()

            if (weekDayInt in allowedDays) {
                if (rule.isAllDayRule() && !goneTroughABlockDay) return@forEach
            } else {
                goneTroughABlockDay = true
                return@forEach
            }

            val nextUnlockTime = getUnlockPeriodForPermissiveDay(today, rule)
            if (nextUnlockTime != null) return nextUnlockTime
        }
        return null
    }

    /**
     * Calcula o próximo instante de liberação em um dia específico para regras [RuleType.RESTRICTIVE].
     *
     * A liberação ocorre se o horário atual já ultrapassou o final do último intervalo de bloqueio no dia.
     *
     * @param day Dia em que a regra será avaliada.
     * @param rule Regra contendo os intervalos de bloqueio.
     * @return [LocalDateTime] do próximo desbloqueio ou null se não houver desbloqueio neste dia.
     */
    private fun getUnlockPeriodForRestrictiveDay(day: LocalDateTime, rule: Rule): LocalDateTime? {
        val sortedTimeRanges = rule.sortedRanges()

        sortedTimeRanges.forEachIndexed { index, timeRange ->
            if (rule.isAllDayRule()) return null
            if (chainedRanges(timeRange, index, sortedTimeRanges)) return@forEachIndexed

            val timeRangeRelative = day.at(timeRange.endHour, timeRange.endMinute)

            val isEqualOrAfter = !timeRangeRelative.isBefore(day)
            if (isEqualOrAfter) return timeRangeRelative.plusMinutes(1)
        }

        return null
    }

    /**
     * Calcula o próximo instante de liberação em um dia específico para regras [RuleType.PERMISSIVE].
     *
     * A liberação ocorre no início do primeiro intervalo de permissão futuro que ainda não começou.
     * Se a regra for válida o dia to.do, retorna o início do dia (00:00).
     *
     * @param day Dia em que a regra será avaliada.
     * @param rule Regra contendo os intervalos de permissão.
     * @return [LocalDateTime] do próximo desbloqueio ou null se não houver um horário aplicável.
     */
    private fun getUnlockPeriodForPermissiveDay(day: LocalDateTime, rule: Rule): LocalDateTime? {
        val sortedTimeRanges = rule.sortedRanges()

        sortedTimeRanges.forEachIndexed { index, timeRange ->
            if (rule.isAllDayRule()) return day.withMillisOfDay(0)
            if (chainedRanges(timeRange, index, sortedTimeRanges)) return@forEachIndexed

            val timeRangeRelative = day.at(timeRange.startHour, timeRange.startMinute)
            val isEqualOrAfter = !timeRangeRelative.isBefore(day)
            if (isEqualOrAfter) return timeRangeRelative
        }

        return null
    }

    /**
     * Gera uma lista de [LocalDateTime] representando os dias que devem ser verificados
     * em busca do próximo período de desbloqueio. O primeiro dia é a data base original,
     * os demais são os 7 dias seguintes iniciando à meia-noite (00:00).
     *
     * @return Lista de [LocalDateTime] com [REPEAT_COUNT] elementos.
     */
    private fun daysOfNextWeek(): List<LocalDateTime> {
        return (0 until REPEAT_COUNT).map {
            if (it > 0) baseDateTime.plusDays(it).withMillisOfDay(0)
            else baseDateTime
        }
    }

    /**
     * Verifica se dois intervalos de tempo estão encadeados, ou seja,
     * se o fim de um intervalo coincide imediatamente com o início do próximo.
     *
     * Isso impede falsos positivos no cálculo de desbloqueios para regras com múltiplos períodos contínuos.
     *
     * @param timeRange Intervalo atual.
     * @param index Índice do intervalo atual na lista.
     * @param sortedTimeRanges Lista ordenada de intervalos.
     * @return `true` se os intervalos estiverem encadeados, `false` caso contrário.
     */
    private fun chainedRanges(
        timeRange: TimeRange,
        index: Int,
        sortedTimeRanges: List<TimeRange>,
    ): Boolean {
        return timeRange.endInMinutes() + 1 == sortedTimeRanges.getOrNull(index + 1)?.startInMinutes()
    }

}