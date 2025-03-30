import dev.gmarques.controledenotificacoes.domain.model.TimeInterval

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 as 21:49.
 */
class TimeIntervalValidator {

    companion object {

        /**
         * Valida as horas e minutos de início e fim de um intervalo de tempo.
         *
         * Esta função verifica se os valores de hora e minuto para os tempos de início e fim
         * de um intervalo de tempo estão dentro de intervalos válidos. As horas devem estar entre
         * 0 e 23 (inclusive), e os minutos devem estar entre 0 e 59 (inclusive). Se as horas ou
         * os minutos estiverem fora desses intervalos, uma [IllegalArgumentException] será lançada
         * com uma mensagem de erro descritiva.
         *
         * @throws IllegalArgumentException se a hora de início ou fim não estiver no intervalo 0..23,
         *                                  ou se o minuto de início ou fim não estiver no intervalo 0..59.
         */
        fun validate(timeInterval: TimeInterval) {

            val hourRange = 0..23
            val minuteRange = 0..59

            if (timeInterval.startHour !in hourRange || timeInterval.endHour !in hourRange)
                throw IllegalArgumentException("Hora de inicio ou fim invalidas ${timeInterval.startHour}, ${timeInterval.endHour}")

            if (timeInterval.startMinute !in minuteRange || timeInterval.endMinute !in minuteRange)
                throw IllegalArgumentException(
                    "Minuto de inicio ou fim invalidos ${timeInterval.startMinute}, ${timeInterval.endMinute}"
                )

        }

    }
}