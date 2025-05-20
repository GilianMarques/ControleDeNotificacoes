package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import org.jetbrains.annotations.TestOnly
import org.joda.time.LocalDateTime
import java.util.Calendar

/**
 * Criado por Gilian Marques
 * Em terça-feira, 20 de maio de 2025 as 13:21.
 */
// TODO: arrumar um nome melhor
// TODO: colocar no pacote adequado
class IntervalCalculator {

    private val now = LocalDateTime.now()
    val actualDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    fun nextUnlockTime(rule: Rule) {
        if (rule.ruleType == RuleType.RESTRICTIVE) nextUnlockTimeFromNowRestricted(rule)
    }

    private fun nextUnlockTimeFromNowRestricted(rule: Rule) {

        getDaysRelativelySorted(rule.days, actualDayOfWeek)
    }


    /**
     * Essa função reordena os dias selecionados de uma regra de acordo com o dia da semana atual,
     * garantindo que o primeiro dia da lista retornada seja o dia da semana atual.
     * Exemplo: Se [actualDayOfWeek] = TERÇA e [ruleDays] = [ SEGUNDA, TERÇA, QUARTA, SEXTA],
     * a função deve retornar [TERÇA, QUARTA, SEXTA, SEGUNDA].
     * A função parte a lista no meio, movendo os dias atrás do dia atual para o fim da lista sem altrar sua ordem.
     */
    @TestOnly
    fun getDaysRelativelySorted(ruleDays: List<WeekDay>, actualDayOfWeek: Int): List<Int> {

        if (ruleDays.isEmpty()) error("Uma regra deve ter pelo menos um dia selecionado")
        if (ruleDays.size == 1) return ruleDays.map { it.dayNumber }

        val sortedRuleDaysInt = ruleDays.map { it.dayNumber }.sortedBy { it }
        if (actualDayOfWeek <= sortedRuleDaysInt.first()) return sortedRuleDaysInt

        val splitIndex = sortedRuleDaysInt.indexOfFirst { it >= actualDayOfWeek }

        return sortedRuleDaysInt.drop(splitIndex) + sortedRuleDaysInt.take(splitIndex)
    }


}