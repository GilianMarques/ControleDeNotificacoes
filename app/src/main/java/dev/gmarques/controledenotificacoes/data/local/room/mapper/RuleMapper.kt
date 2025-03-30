package  dev.gmarques.controledenotificacoes.data.local.room.mapper

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.gmarques.controledenotificacoes.data.local.room.entities.RuleEntity
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import dev.gmarques.controledenotificacoes.domain.model.validators.RuleValidator

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 as 21:49.
 */
class RuleMapper {

    companion object {

        private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        private val weekDayType = Types.newParameterizedType(List::class.java, WeekDay::class.java)
        private val weekDayAdapter: JsonAdapter<List<WeekDay>> = moshi.adapter(weekDayType)

        private val timeIntervalType = Types.newParameterizedType(List::class.java, TimeInterval::class.java)
        private val timeIntervalAdapter: JsonAdapter<List<TimeInterval>> = moshi.adapter(timeIntervalType)


        fun mapToEntity(rule: Rule): RuleEntity {

            RuleValidator.validate(rule)

            return RuleEntity(
                id = rule.id,
                name = rule.name,
                days = daysToString(rule.days),
                timeIntervals = hoursToString(rule.timeIntervals),
            )
        }

        /**
         * Converte uma lista de objetos [TimeInterval] em uma string JSON.
         *
         * Esta função usa um adaptador Moshi para serializar uma lista de objetos
         * [TimeInterval] em sua representação de string JSON.
         *
         * @param timeIntervals A lista de objetos [TimeInterval] a ser convertida.
         * @return Uma string JSON representando a lista de [TimeInterval]s.
         */
        private fun hoursToString(timeIntervals: List<TimeInterval>): String {
            return timeIntervalAdapter.toJson(timeIntervals)
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

        /**
         * Converte uma entidade de banco de dados [RuleEntity] em um objeto de domínio [Rule].
         *
         * Esta função recebe um objeto [RuleEntity] e mapeia suas propriedades para um
         * novo objeto [Rule]. As propriedades `days` e `timeIntervals`, que são
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
                days = stringToDays(entity.days),
                timeIntervals = stringToTimeInterval(entity.timeIntervals),
            )
        }

        /**
         * Converte uma string JSON representando uma lista de [TimeInterval]s em uma lista de objetos [TimeInterval].
         *
         * Esta função usa um adaptador Moshi para desserializar uma string JSON de volta
         * em uma lista de objetos [TimeInterval].
         *
         * @param timeIntervals A string JSON a ser convertida.
         * @return Uma lista de objetos [TimeInterval].
         * @throws NullPointerException se a string de entrada não puder ser desserializada
         * em uma lista de [TimeInterval].
         */
        private fun stringToTimeInterval(timeIntervals: String): List<TimeInterval> {
            return timeIntervalAdapter.fromJson(timeIntervals)!!
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

    }
}