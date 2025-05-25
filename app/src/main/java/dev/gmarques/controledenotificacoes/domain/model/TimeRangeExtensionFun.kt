package dev.gmarques.controledenotificacoes.domain.model

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 31 de março de 2025 as 23:20.
 *
 * Classe utilitária para adicionar funcionalidades ao [TimeRange]
 *
 */
object TimeRangeExtensionFun {

    /**
     * Formata horas e minutos garantindo que sempre tenham dois dígitos (ex: 08:00).
     *
     * @return String no formato "HH:MM".
     */
    fun TimeRange.startIntervalFormatted() = "%02d:%02d".format(this.startHour, this.startMinute)

    /**
     * Formata horas e minutos garantindo que sempre tenham dois dígitos (ex: 08:00).
     *
     * @return String no formato "HH:MM".
     */
    fun TimeRange.endIntervalFormatted() = "%02d:%02d".format(this.endHour, this.endMinute)

    /**
     * Calcula o horário de início de um [TimeRange] em minutos totais.
     *
     * Esta função recebe um objeto [TimeRange], que possui as propriedades `startHour` (hora de início) e
     * `startMinute` (minuto de início), e retorna o número total de minutos correspondente ao horário de início.
     *
     * @return O horario de iniciodo intervalo em  minutos
     */
    fun TimeRange.startInMinutes() = this.startHour * 60 + this.startMinute

    /**
     * Calcula o horário de término de um TimeRange em minutos totais.
     *
     * Esta função recebe um objeto TimeRange, que presumivelmente possui as propriedades `endHour` (hora de término) e
     * `endMinute` (minuto de término), e retorna o número total de minutos representado pelo horário de término.
     *
     * Por exemplo, se `endHour` for 2 e `endMinute` for 30, a função retornará 150 (2 * 60 + 30).
     *
     * @return O número total de minutos representado pelo horário de término do TimeRange.
     */
    fun TimeRange.endInMinutes() = this.endHour * 60 + this.endMinute

    /**
     * Converte um TimeRange em um intervalo fechado de inteiros representando os minutos de início e fim.
     *
     * Esta função recebe um TimeRange e extrai seus horários de início e fim,
     * expressos em minutos a partir do início do dia. Em seguida, ela constrói um
     * intervalo fechado de inteiros usando esses valores.
     *
     * O intervalo retornado inclui tanto o minuto de início quanto o de fim.
     *
     * @return Um intervalo fechado de inteiros (IntRange) onde o valor de início é o início
     *         do intervalo de tempo em minutos e o valor de fim é o fim do
     *         intervalo de tempo em minutos.
     *
     * @sample
     *   val range = TimeRange(LocalTime.of(9, 0), LocalTime.of(10, 30))
     *   val range = range.periodsAsRange()
     *   println(range) // Imprime: 540..630
     *
     *   val range2 = TimeRange(LocalTime.of(0, 0), LocalTime.of(0, 1))
     *   val range2 = range2.periodsAsRange()
     *   println(range2) // Imprime 0..1
     */
    fun TimeRange.asRange() = this.startInMinutes()..this.endInMinutes()

}