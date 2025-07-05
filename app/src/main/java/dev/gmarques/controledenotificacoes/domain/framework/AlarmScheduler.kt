package dev.gmarques.controledenotificacoes.domain.framework

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 16 de maio de 2025 as 11:00.
 *
 * gerencia o agendamento e cancelamento de alarmes no sistema
 */
interface AlarmScheduler {

    /**
     * Agenda uma alarme para disparar em determinado horario e
     * escreve o dados do agendamento nas preferencias.
     */
    fun scheduleAlarm(packageId: String, millis: Long)
    fun scheduleAutoTurnOnAlarm(millis: Long)

    /**
     * Cancela o agendamento de um alarme alarme  e
     * remove o dados do agendamento das preferencias.
     */
    fun cancelAlarm(packageId: String)

    /**
     * Remove das preferências do pacote referente ao alarme.
     * Use essa função para remover o registro do agendamento depois que o alarme for disparado ou cancelado.
     */
    fun deleteScheduleData(packageId: String)

    /**
     * Verifica se existe algum alarme agendado para o aplicativo especificado.
     */
    fun isThereAnyAlarmSetForPackage(packageId: String): Boolean

    /**
     * Retorna todos os agendamentos ativos no momento
     */
    fun getAllSchedules(): List<String>
}