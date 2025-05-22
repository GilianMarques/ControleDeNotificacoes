package dev.gmarques.controledenotificacoes.domain.model

import androidx.test.runner.AndroidJUnit4
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import junit.framework.TestCase.assertEquals
import org.joda.time.LocalDateTime
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntervalCalculatorTest {


    var tuesday_12_20__20_05_25_day_3_ofWeek = LocalDateTime.now()
        .withYear(2025)
        .withMonthOfYear(5)
        .withDayOfMonth(20)
        .withHourOfDay(12)
        .withMinuteOfHour(20)
        .withSecondOfMinute(0)

    @Test
    fun ruleThatBlocksBeforeCurrentDay() {

        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.SUNDAY,
            ),
            timeRanges = listOf(
                TimeRange(8, 0, 12, 0),
                TimeRange(13, 0, 18, 0),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(12)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(5)
            .plusMinutes(1)// Adiciona-se um minuto ao fim dos períodos de bloqueio em regras restritivas apenas

        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )

    }

    @Test
    fun ruleThatBlocksCurrentDayWithFreePeriod() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.TUESDAY,
            ),
            timeRanges = listOf(
                TimeRange(8, 0, 18, 0),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
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
    fun ruleThatBlocksCurrentDayAllDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.TUESDAY,
            ),
            timeRanges = listOf(
                TimeRange(0, 0, 23, 59),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(1)

        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatBlocksCurrentAndNextDayAllDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.TUESDAY,
                WeekDay.WEDNESDAY,
            ),
            timeRanges = listOf(
                TimeRange(true),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(2)

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
                WeekDay.FRIDAY,
            ),
            timeRanges = listOf(
                TimeRange(8, 0, 18, 0),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .plusDays(3)
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
    fun ruleThatBlocksSevenDaysTwentyFourHours() {

        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.MONDAY,
                WeekDay.TUESDAY,
                WeekDay.WEDNESDAY,
                WeekDay.THURSDAY,
                WeekDay.FRIDAY,
                WeekDay.SATURDAY,
                WeekDay.SUNDAY,
            ),

            timeRanges = listOf(
                TimeRange(allDay = true)
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = IntervalCalculator.INFINITE
        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod,
            nextPeriodMillis
        )

    }

}