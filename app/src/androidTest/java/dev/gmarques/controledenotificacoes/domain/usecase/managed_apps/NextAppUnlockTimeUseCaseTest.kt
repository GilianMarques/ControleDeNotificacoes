package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import junit.framework.TestCase
import org.joda.time.LocalDateTime
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NextAppUnlockTimeUseCaseTest {


    @Suppress("PrivatePropertyName")
    private var tuesday_12_20__20_05_25_day_3_ofWeek = LocalDateTime.now()
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(12)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(5)
            .plusMinutes(1)// Adiciona-se um minuto ao fim dos períodos de bloqueio em regras restritivas apenas

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(6)



        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(18)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusMinutes(1)// Adiciona-se um minuto ao fim dos períodos de bloqueio em regras restritivas apenas

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(1)

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(1)

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(1)

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(11)
            .withMinuteOfHour(45)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(1)
            .plusMinutes(1)

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(2)

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .plusDays(3)
            .withHourOfDay(18)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusMinutes(1)// Adiciona-se um minuto ao fim dos períodos de bloqueio em regras restritivas apenas

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(4)

        TestCase.assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatBlocksAfterCurrentDayUntil2359() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.RESTRICTIVE,

            days = listOf(
                WeekDay.WEDNESDAY,
                WeekDay.SATURDAY,
            ),

            timeRanges = listOf(
                TimeRange(0, 0, 23, 59),
            ),
        )

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(2)

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = NextAppUnlockTimeUseCase.Companion.INFINITE
        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(8)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(5)

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(5)

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(8)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(7)

        TestCase.assertEquals(
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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(7)

        TestCase.assertEquals(
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
                TimeRange(13, 0, 18, 0),
                TimeRange(18, 1, 23, 59),
            ),
        )

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(18)
            .withMinuteOfHour(1)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)

        TestCase.assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatAllowsCurrentAndNextDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

            days = listOf(
                WeekDay.TUESDAY,
                WeekDay.WEDNESDAY,
            ),
            timeRanges = listOf(
                TimeRange(8, 0, 11, 45),
            ),
        )

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(8)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(1)

        TestCase.assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatAllowsCurrentAndNextDayAllDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

            days = listOf(
                WeekDay.TUESDAY,
                WeekDay.WEDNESDAY,
            ),

            timeRanges = listOf(
                TimeRange(true),
            ),
        )

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(7)

        TestCase.assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatAllowsAfterCurrentDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

            days = listOf(
                WeekDay.FRIDAY,
            ),
            timeRanges = listOf(
                TimeRange(8, 0, 18, 0),
            ),
        )

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(8)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(3)

        TestCase.assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatAllowsAfterCurrentDayAllDay() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

            days = listOf(
                WeekDay.FRIDAY,
            ),
            timeRanges = listOf(
                TimeRange(true),
            ),
        )

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(3)

        TestCase.assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatAllowsAfterCurrentDayUntil2359() {
        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

            days = listOf(
                WeekDay.FRIDAY,
                WeekDay.SATURDAY,
            ),
            timeRanges = listOf(
                TimeRange(0, 0, 23, 59),
            ),
        )

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = LocalDateTime(tuesday_12_20__20_05_25_day_3_ofWeek)
            .withHourOfDay(0)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
            .plusDays(3)

        TestCase.assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod.toDate().time,
            nextPeriodMillis
        )
    }

    @Test
    fun ruleThatAllowsSevenDaysTwentyFourHours() {

        val rule = Rule(
            name = "",
            ruleType = RuleType.PERMISSIVE,

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

        val nextPeriodMillis = NextAppUnlockTimeUseCase().invoke(tuesday_12_20__20_05_25_day_3_ofWeek, rule)

        val expectPeriod = NextAppUnlockTimeUseCase.Companion.INFINITE
        TestCase.assertEquals(
            "\n   expect: $expectPeriod, \nreceived:${LocalDateTime(nextPeriodMillis)}\n",
            expectPeriod,
            nextPeriodMillis
        )

    }


}