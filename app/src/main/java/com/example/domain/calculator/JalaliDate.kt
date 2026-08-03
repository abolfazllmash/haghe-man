package com.example.domain.calculator

data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    fun formatString(usePersianDigits: Boolean = true): String {
        val monthFormatted = if (month < 10) "0$month" else "$month"
        val dayFormatted = if (day < 10) "0$day" else "$day"
        val raw = "$year/$monthFormatted/$dayFormatted"
        return if (usePersianDigits) PersianNumberFormatter.toPersianDigits(raw) else raw
    }

    fun toDaysSinceEpoch(): Long {
        var days = 0L
        for (y in 1 until year) {
            days += if (isJalaliLeapYear(y)) 366 else 365
        }
        for (m in 1 until month) {
            days += getJalaliMonthDays(year, m)
        }
        days += day
        return days
    }

    companion object {
        fun getJalaliMonthDays(year: Int, month: Int): Int {
            return when {
                month in 1..6 -> 31
                month in 7..11 -> 30
                month == 12 -> if (isJalaliLeapYear(year)) 30 else 29
                else -> 30
            }
        }

        fun isJalaliLeapYear(year: Int): Boolean {
            val r = (year + 38) * 31 % 128
            return r < 31
        }

        fun daysBetween(startDate: JalaliDate, endDate: JalaliDate): Long {
            val diff = endDate.toDaysSinceEpoch() - startDate.toDaysSinceEpoch() + 1
            return if (diff < 1L) 1L else diff
        }

        fun parseString(dateStr: String): JalaliDate? {
            val clean = PersianNumberFormatter.toEnglishDigits(dateStr).replace("-", "/").trim()
            val parts = clean.split("/")
            if (parts.size == 3) {
                val y = parts[0].toIntOrNull()
                val m = parts[1].toIntOrNull()
                val d = parts[2].toIntOrNull()
                if (y != null && m != null && d != null && m in 1..12 && d in 1..31) {
                    return JalaliDate(y, m, d)
                }
            }
            return null
        }
    }
}
