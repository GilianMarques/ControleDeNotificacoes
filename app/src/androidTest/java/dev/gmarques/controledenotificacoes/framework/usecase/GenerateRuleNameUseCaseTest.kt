package dev.gmarques.controledenotificacoes.framework.usecase

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleDescriptionUseCase
import dev.gmarques.controledenotificacoes.framework.RuleStringsProviderImpl
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenerateRuleDescriptionUseCaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val ruleStringsProvider = RuleStringsProviderImpl(context)
    private val useCase = GenerateRuleDescriptionUseCase(ruleStringsProvider)

    @Test
    fun testReturnedName() {
        val rulesPair = listOf(

            Rule(
                name = "",
                ruleType = RuleType.RESTRICTIVE,
                days = listOf(WeekDay.MONDAY, WeekDay.FRIDAY),
                timeRanges = listOf(
                    TimeRange(8, 0, 12, 0),
                    TimeRange(13, 0, 18, 0),
                )
            ) to "Bloq. Seg/Sex 08:00-18:00",


            Rule(
                name = "",
                ruleType = RuleType.PERMISSIVE,
                days = listOf(WeekDay.MONDAY, WeekDay.SUNDAY, WeekDay.FRIDAY),
                timeRanges = listOf(
                    TimeRange(8, 0, 12, 0),
                    TimeRange(13, 0, 18, 0),
                    TimeRange(19, 0, 19, 10),
                )
            ) to "Perm. Dom-Seg/Sex 08:00-19:10",

            )


        rulesPair.forEach {
            val name = useCase(it.first)
            val name2 = it.second

            Log.d("USUK", "GenerateRuleDescriptionUseCaseTest.tesReturnedName: \n$name\n$name2")
            assertEquals("valores '$name' '$name2'", name, name2)
        }
    }
}
