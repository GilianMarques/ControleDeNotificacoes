package  dev.gmarques.controledenotificacoes.data.local.room.mapper

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.gmarques.controledenotificacoes.data.local.room.entities.RuleEntity
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 as 21:49.
 */
object RuleMapper {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val weekDayType = Types.newParameterizedType(List::class.java, WeekDay::class.java)
    private val weekDayAdapter: JsonAdapter<List<WeekDay>> = moshi.adapter(weekDayType)

    private val timeRangeType = Types.newParameterizedType(List::class.java, TimeRange::class.java)
    private val timeRangeAdapter: JsonAdapter<List<TimeRange>> = moshi.adapter(timeRangeType)

    private val conditionAdapter: JsonAdapter<Condition> = moshi.adapter(Condition::class.java)

    fun mapToEntity(rule: Rule): RuleEntity {

        RuleValidator.validate(rule)

        return RuleEntity(
            id = rule.id,
            name = rule.name,
            ruleType = rule.ruleType,
            days = daysToString(rule.days),
            condition = conditionToString(rule.condition),
            timeRanges = hoursToString(rule.timeRanges),
        )
    }

    /**
     * Converte uma lista de objetos [TimeRange] em uma string JSON.
     *
     * Esta função usa um adaptador Moshi para serializar uma lista de objetos
     * [TimeRange] em sua representação de string JSON.
     *
     * @param timeRanges A lista de objetos [TimeRange] a ser convertida.
     * @return Uma string JSON representando a lista de [TimeRange]s.
     */
    private fun hoursToString(timeRanges: List<TimeRange>): String {
        return timeRangeAdapter.toJson(timeRanges)
    }

    /**
     * Converte uma lista de enums [WeekDay] em uma string JSON.
     *
     * Esta função usa um adaptador Moshi para serializar uma lista de enums [WeekDay]
     * em sua representação de string JSON.
     *
     * @param days A lista de enums [WeekDay] a ser convertida.
     * @return Uma string JSON representando a lista de [WeekDay]s.
     */
    private fun daysToString(days: List<WeekDay>): String {
        return weekDayAdapter.toJson(days)
    }

    private fun conditionToString(condition: Condition?): String? {
        return if (condition == null) null else conditionAdapter.toJson(condition)
    }

    /**
     * Converte uma entidade de banco de dados [RuleEntity] em um objeto de domínio [Rule].
     *
     * Esta função recebe um objeto [RuleEntity] e mapeia suas propriedades para um
     * novo objeto [Rule]. As propriedades `days` e `timeRanges`, que são
     * armazenadas como strings na entidade, são desserializadas de volta em listas de
     * tipos complexos usando adaptadores Moshi.
     *
     * @param entity O objeto [RuleEntity] a ser convertido.
     * @return Um objeto [Rule] representando a mesma regra.
     */
    fun mapToModel(entity: RuleEntity): Rule {

        return Rule(
            id = entity.id,
            name = entity.name,
            ruleType = entity.ruleType,
            days = stringToDays(entity.days),
            condition = stringToCondition(entity.condition),
            timeRanges = stringToTimeRange(entity.timeRanges),
        )
    }

    /**
     * Converte uma string JSON representando uma lista de [TimeRange]s em uma lista de objetos [TimeRange].
     *
     * Esta função usa um adaptador Moshi para desserializar uma string JSON de volta
     * em uma lista de objetos [TimeRange].
     *
     * @param timeRanges A string JSON a ser convertida.
     * @return Uma lista de objetos [TimeRange].
     * @throws NullPointerException se a string de entrada não puder ser desserializada
     * em uma lista de [TimeRange].
     */
    private fun stringToTimeRange(timeRanges: String): List<TimeRange> {
        return timeRangeAdapter.fromJson(timeRanges)!!
    }

    /**
     * Converte uma string JSON representando uma lista de [WeekDay]s em uma lista de enums [WeekDay].
     *
     * Esta função usa um adaptador Moshi para desserializar uma string JSON de volta
     * em uma lista de enums [WeekDay].
     *
     * @param days A string JSON a ser convertida.
     * @return Uma lista de enums [WeekDay].
     * @throws NullPointerException se a string de entrada não puder ser desserializada
     * em uma lista de [WeekDay].
     */
    private fun stringToDays(days: String): List<WeekDay> {
        return weekDayAdapter.fromJson(days)!!
    }

    private fun stringToCondition(condition: String?): Condition? {
        return if (condition == null) null else conditionAdapter.fromJson(condition)
    }

}