package dev.gmarques.controledenotificacoes.domain.model.enums

import androidx.annotation.Keep

@Keep
enum class WeekDay(val dayNumber: Int) {

    SUNDAY(1),
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7),

}