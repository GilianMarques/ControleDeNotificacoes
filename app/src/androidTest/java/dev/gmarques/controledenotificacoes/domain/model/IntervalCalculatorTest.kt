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
    fun ruleThatBlocksBeforeCurrentDayAllDay() {

        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.SUNDAY,
            ),
            timeRanges = listOf(
                TimeRange(allDay = true)
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(6)



        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )

    }

    @Test
    fun ruleThatBlocksCurrentDay() {
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
    fun ruleThatBlocksBeforeAndCurrentDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.MONDAY,
                WeekDay.TUESDAY,
            ),

            timeRanges = listOf(
                TimeRange(8, 0, 11, 45),
                TimeRange(13, 0, 18, 0),
                TimeRange(18, 1, 23, 59),
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
    fun ruleThatBlocksBeforeAndCurrentDayAllDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.MONDAY,
                WeekDay.TUESDAY,
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
            .plusDays(1)

        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatBlocksCurrentAndNextDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.TUESDAY,
                WeekDay.WEDNESDAY,
            ),
            timeRanges = listOf(
                TimeRange(8, 0, 11, 45),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(11)
            .withMinuteOfHour(45)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(1)
            .plusMinutes(1)

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
    fun ruleThatBlocksAfterCurrentDayAllDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.FRIDAY,
            ),
            timeRanges = listOf(
                TimeRange(true),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .plusDays(0)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(4)

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

    //---------------------------- teste de regras permissivas

    @Test
    fun ruleThatAllowsBeforeCurrentDay() {

        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

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
            .withHourOfDay(8)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(5)

        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )

    }

    @Test
    fun ruleThatAllowsBeforeCurrentDayAllDay() {

        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

            days = listOf(
                WeekDay.SUNDAY,
            ),
            timeRanges = listOf(
                TimeRange(allDay = true)
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(5)

        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )

    }

    @Test
    fun ruleThatAllowsCurrentDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

            days = listOf(
                WeekDay.TUESDAY,
            ),
            timeRanges = listOf(
                TimeRange(8, 0, 18, 0),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(8)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(7)

        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatAllowsCurrentDayAllDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

            days = listOf(
                WeekDay.TUESDAY,
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
            .plusDays(7)

        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatAllowsBeforeAndCurrentDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

            days = listOf(
                WeekDay.MONDAY,
                WeekDay.TUESDAY,
            ),

            timeRanges = listOf(
                TimeRange(8, 0, 11, 45),
                TimeRange(13, 0, 18, 0),// TODO: acho que o teste ta errado, veja o retorno
                TimeRange(18, 1, 23, 59),
            ),
        )

        val nextPeriodMillis = IntervalCalculator().nextUnlockTime(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(18)
            .withMinuteOfHour(1)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)

        assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

}