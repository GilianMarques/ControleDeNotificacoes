package dev.gmarques.controledenotificacoes.plataform.usecase

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenerateRuleNameUseCaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val useCase = GenerateRuleNameUseCase(context)

    @Test
    fun tesReturnedName() {
        val rulesPair = listOf(
            Rule(
                name = "",
                ruleType = RuleType.RESTRICTIVE,
                days = listOf(WeekDay.MONDAY, WeekDay.FRIDAY),
                timeIntervals = listOf(
                    TimeInterval(8, 0, 12, 0),
                    TimeInterval(13, 0, 18, 0),
                )
            ) to "Bloq. Seg, Sex 08:00-18:00",

            Rule( // TODO: ajustar esse teste 
                name = "",
                ruleType = RuleType.PERMISSIVE,
                days = listOf(WeekDay.MONDAY, WeekDay.SUNDAY, WeekDay.FRIDAY),
                timeIntervals = listOf(
                    TimeInterval(8, 0, 12, 0),
                    TimeInterval(13, 0, 18, 0),
                    TimeInterval(19, 0, 19, 10),
                )
            ) to "Perm. Seg, Dom 08:00-19:10",
        )



        rulesPair.forEach { assertEquals(useCase.execute(it.first), it.second) }
    }
}
