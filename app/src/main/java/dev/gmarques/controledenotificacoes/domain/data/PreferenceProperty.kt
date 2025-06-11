package dev.gmarques.controledenotificacoes.domain.data

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 28 de maio de 2025 as 21:19.
 *
 * Esse objeto tem como objetivo facilitar o acesso  ao conteúdo das preferências sem texto usar use cases ou repositórios
 * através deste objeto é possível ler os dados de uma preferência no armazenamento do dispositivo assim como escrever além
 * de verificar se o valor salvo é o valor padrão.
 */
class PreferenceProperty<T>(
    private val key: String,
    private val defaultValue: T,
    private val preferenceReader: (String, T) -> T,
    private val preferenceSaver: (String, T) -> Unit,
) {

    val value: T
        get() = preferenceReader(key, defaultValue)

    /**
     * Verifica se o valor da preferenia é o valor padrão
     * @return true se o valor for padrao, false se for um valor personalizado
     */
    fun isDefault(): Boolean = value == defaultValue

    /**Salva o valor padrao da preferencia no armazenamento*/
    fun reset() {
        preferenceSaver(key, defaultValue)
    }

    /**Atualiza a preferencia com o valore recebido*/
    operator fun invoke(value: T) {
        preferenceSaver(key, value)
    }
}