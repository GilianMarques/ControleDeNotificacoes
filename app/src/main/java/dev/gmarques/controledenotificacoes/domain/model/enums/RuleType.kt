package dev.gmarques.controledenotificacoes.domain.model.enums

import androidx.annotation.Keep

@Keep
enum class RuleType(val value: Int) { PERMISSIVE(1), RESTRICTIVE(0) }