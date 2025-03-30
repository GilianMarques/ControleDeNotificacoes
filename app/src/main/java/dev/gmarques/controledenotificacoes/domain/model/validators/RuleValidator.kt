package dev.gmarques.controledenotificacoes.domain.model.validators

import TimeIntervalValidator
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeInterval
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import java.util.Locale

/**
 *
 *  Criado por Gilian Marques
 *  Em sábado, 29 de março de 2025 as 22:19.
 *
 *
 * Valida as propriedades de um objeto [Rule].
 *
 * Esta classe fornece um ponto centralizado para validar os diferentes atributos de uma
 * [Rule], como seu nome, os dias da semana em que se aplica e os intervalos de tempo que
 * abrange. Ela garante que esses atributos atendam a critérios específicos antes que a regra
 * seja usada ou persistida.
 */
class RuleValidator {

    companion object {

        /**
         * Valida um objeto [Rule].
         *
         * Esta função executa uma validação completa de um objeto [Rule] chamando as
         * funções de validação individuais para seu nome, dias e horas. Se alguma das
         * validações falhar, uma [IllegalArgumentException] será lançada.
         *
         * @param rule O objeto [Rule] a ser validado.
         * @throws IllegalArgumentException se alguma das propriedades da regra for inválida.
         *                                  A mensagem da exceção indicará a validação
         *                                  específica que falhou.
         */
        fun validate(rule: Rule) {
            validateName(rule.name)
            validateDays(rule.days)
            validateHours(rule.timeIntervals)
        }

        /**
         * Valida o nome de uma regra.
         *
         * Esta função verifica se o nome da regra atende aos seguintes critérios:
         *  - Não está em branco (vazio ou contém apenas espaços em branco).
         *  - Após remover espaços em branco iniciais/finais e substituir vários espaços por
         *    espaços únicos, tem um comprimento entre 3 e 50 caracteres (inclusive).
         *  - Cada palavra no nome é capitalizada (title case).
         *
         * Se o nome for inválido, uma [IllegalArgumentException] será lançada.
         *
         * @param name O nome da regra a ser validado.
         * @return O nome validado e formatado (sem espaços extras, com espaços únicos e capitalizado).
         * @throws IllegalArgumentException se o nome estiver em branco ou não atender aos requisitos
         *                                  de comprimento. A mensagem da exceção descreverá a
         *                                  falha de validação específica.
         */
        private fun validateName(name: String): String {

            if (name.isBlank()) {
                throw IllegalArgumentException("O nome não pode ser vazio.")
            }

            val trimmedName = name.trim().replace("\\s+".toRegex(), " ")

            val capitalizedName = trimmedName.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault())
                    else char.toString()
                }
            }

            if (capitalizedName.length < 3 || capitalizedName.length > 50) {
                throw IllegalArgumentException("O nome deve ter entre 3 e 50 caracteres.")
            }


            return capitalizedName
        }

        private fun validateDays(days: List<WeekDay>) {
            if (days.size !in 1..7) throw IllegalArgumentException("Regra deve ter entre 1 e 7 dias")
        }

        private fun validateHours(hours: List<TimeInterval>) {
            hours.forEach { timeInterval ->
                try {
                    TimeIntervalValidator.validate(timeInterval)
                } catch (ex: Exception) {
                    throw ex
                }
            }
        }

    }
}