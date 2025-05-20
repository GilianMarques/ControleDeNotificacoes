package dev.gmarques.controledenotificacoes.domain.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay.FRIDAY
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay.MONDAY
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay.SATURDAY
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay.SUNDAY
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay.THURSDAY
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay.TUESDAY
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay.WEDNESDAY
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntervalCalculatorTest {

    @Test
    fun getDaysRelativelySorted() {
        val actualDayOfWeek = TUESDAY.dayNumber

        val cases = listOf(

            listOf<WeekDay>(MONDAY, WEDNESDAY, FRIDAY) to listOf(
                WEDNESDAY.dayNumber, FRIDAY.dayNumber, MONDAY.dayNumber
            ),

            listOf<WeekDay>(MONDAY, TUESDAY, WEDNESDAY, FRIDAY) to listOf(
                TUESDAY.dayNumber,
                WEDNESDAY.dayNumber,
                FRIDAY.dayNumber,
                MONDAY.dayNumber
            ),

            listOf<WeekDay>(SATURDAY, SUNDAY) to listOf(
                SATURDAY.dayNumber, SUNDAY.dayNumber
            ),

            listOf<WeekDay>(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY) to listOf(
                TUESDAY.dayNumber,
                WEDNESDAY.dayNumber,
                THURSDAY.dayNumber,
                FRIDAY.dayNumber,
                SATURDAY.dayNumber,
                SUNDAY.dayNumber,
                MONDAY.dayNumber,
            ),

            )

        cases.forEachIndexed { index, (listDays, expectedResult) ->
            val result = IntervalCalculator().getDaysRelativelySorted(listDays, actualDayOfWeek)
            assertEquals("erro no indice $index", result, expectedResult)
        }

    }


}