package dev.gmarques.controledenotificacoes.domain.model.enums

enum class WeekDay(val dayNumber: Int) {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6);

    companion object {
        fun fromNumber(number: Int): WeekDay {
            return entries.first { it.dayNumber == number }
        }

        fun fromString(day: String): WeekDay {
            return valueOf(day.uppercase())
        }
    }
}