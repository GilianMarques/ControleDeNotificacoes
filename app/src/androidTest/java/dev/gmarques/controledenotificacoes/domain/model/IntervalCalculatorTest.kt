package dev.gmarques.controledenotificacoes.domain.model

import androidx.test.runner.AndroidJUnit4
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import junit.framework.TestCase.assertEquals
import org.joda.time.LocalDateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntervalCalculatorTest {


    /*
    * regra que bloqueia antes do dia atual
    * regra que bloqueia depois do dia atual
    * regra qeu bloquia no dia atual com perio livre
    * regra que bloqueia no dia atual o dia intero
    * regra que bloqueia 24/7
    * */
    var TuesdayAtTwelveAndTwenty: LocalDateTime = LocalDateTime.now()

    @Before
    fun setup() {
        // terça-feira 20/05/2025 as 12:20
        TuesdayAtTwelveAndTwenty = LocalDateTime.now()
            .withYear(2025)
            .withMonthOfYear(5)
            .withDayOfMonth(20)
            .withHourOfDay(12)
            .withMinuteOfHour(20)
            .withSecondOfMinute(0)
    }
    /*
    *  days = listOf(
                    WeekDay.TUESDAY,
                    WeekDay.WEDNESDAY,
                    WeekDay.THURSDAY,
                    WeekDay.FRIDAY,
                    WeekDay.SATURDAY,
                    WeekDay.SUNDAY,
                )*/

    @Test
    fun ruleThatBlocksBeforeCurrentDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.MONDAY,
            ),
            timeRanges = listOf(
                TimeRange(8, 0, 12, 0),
                TimeRange(13, 0, 18, 0),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(TuesdayAtTwelveAndTwenty, rule)

        val expectPeriod = LocalDateTime(TuesdayAtTwelveAndTwenty)
            .withHourOfDay(18)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusMinutes(1)// Adiciona-se um minuto ao fim dos períodos de bloqueio em regras restritivas apenas

        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )

    }

    @Test
    fun ruleThatBlocksAfterCurrentDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.WEDNESDAY,
            ),
            timeRanges = listOf(
                TimeRange(8, 0, 18, 0),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(TuesdayAtTwelveAndTwenty, rule)

        val expectPeriod = LocalDateTime(TuesdayAtTwelveAndTwenty)
            .plusDays(1)
            .withHourOfDay(18)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusMinutes(1)// Adiciona-se um minuto ao fim dos períodos de bloqueio em regras restritivas apenas

        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatBlocksCurrentDayWithFreePeriod() {
    }

    @Test
    fun ruleThatBlocksCurrentDayAllDay() {
    }

    @Test
    fun ruleThatBlocksSevenDaysTwentyFourHours() {
    }


}