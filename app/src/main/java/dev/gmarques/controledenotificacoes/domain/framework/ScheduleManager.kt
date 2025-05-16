package dev.gmarques.controledenotificacoes.domain.framework

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 16 de maio de 2025 as 11:00.
 *
 * gerencia o agendamento e cancelamento de alarmes no sistema
 */
interface ScheduleManager {

    /**
     * Agenda uma alarme para disparar em determinado horario e
     * escreve o dados do agendamento nas preferencias.
     */
    fun scheduleAlarm(packageId: String, millis: Long)

    /**
     * Cancela o agendamento de um alarme alarme  e
     * remove o dados do agendamento das preferencias.
     */
    fun cancelAlarm(packageId: String)

    /**
     * Verifica se existe algum alarme agendado para o aplicativo especificado.
     */
    fun isAnyAlarmSetForPackage(packageId: String): Boolean
}