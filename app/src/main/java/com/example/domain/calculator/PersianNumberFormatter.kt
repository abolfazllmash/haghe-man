package com.example.domain.calculator

import com.example.data.model.CurrencyUnit
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PersianNumberFormatter {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ARABIC_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    fun toPersianDigits(numberStr: String): String {
        val sb = StringBuilder()
        for (ch in numberStr) {
            when (ch) {
                in '0'..'9' -> sb.append(PERSIAN_DIGITS[ch - '0'])
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toPersianDigits(numberStr: String, usePersianDigits: Boolean): String {
        return if (usePersianDigits) toPersianDigits(numberStr) else numberStr
    }

    fun toEnglishDigits(numberStr: String): String {
        val sb = StringBuilder()
        for (ch in numberStr) {
            when (ch) {
                in '۰'..'۹' -> sb.append((ch - '۰').toString())
                in '٠'..'٩' -> sb.append((ch - '٠').toString()) // Arabic digits
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun cleanNumberInput(input: String): String {
        val engDigits = toEnglishDigits(input)
        val digitsOnly = engDigits.replace("[^0-9]".toRegex(), "")
        return if (digitsOnly.length > 15) digitsOnly.substring(0, 15) else digitsOnly
    }

    fun parseToLong(input: String): Long {
        val cleaned = cleanNumberInput(input)
        return cleaned.toLongOrNull() ?: 0L
    }

    fun parseToDouble(input: String): Double {
        val engDigits = toEnglishDigits(input).replace(",", "").trim()
        return engDigits.toDoubleOrNull() ?: 0.0
    }

    fun formatNumber(
        number: Long,
        usePersianDigits: Boolean = true,
        includeCommas: Boolean = true
    ): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        val formatted = if (includeCommas) formatter.format(number) else number.toString()
        return if (usePersianDigits) toPersianDigits(formatted) else formatted
    }

    fun formatNumber(
        number: Double,
        usePersianDigits: Boolean = true,
        includeCommas: Boolean = true
    ): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val formatter = DecimalFormat("#,##0.##", symbols)
        val formatted = if (includeCommas) formatter.format(number) else number.toString()
        return if (usePersianDigits) toPersianDigits(formatted) else formatted
    }

    fun formatCurrency(
        amountInRial: Long,
        currencyUnit: CurrencyUnit,
        usePersianDigits: Boolean = true
    ): String {
        val displayAmount = if (currencyUnit == CurrencyUnit.TOMAN) {
            amountInRial / 10L
        } else {
            amountInRial
        }
        return "${formatNumber(displayAmount, usePersianDigits)} ${currencyUnit.title}"
    }

    private val units = arrayOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val teens = arrayOf("ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده")
    private val tens = arrayOf("", "ده", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val hundreds = arrayOf("", "صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد")
    private val scales = arrayOf("", "هزار", "میلیون", "میلیارد", "تریلیون")

    /**
     * Converts a number to its Persian words equivalent.
     * e.g. 12,500,000 -> "دوازده میلیون و پانصد هزار"
     */
    fun numberToPersianWords(number: Long): String {
        if (number == 0L) return "صفر"
        if (number < 0L) {
            if (number == Long.MIN_VALUE) return "منفی"
            return "منفی " + numberToPersianWords(-number)
        }

        var temp = number
        val parts = mutableListOf<String>()
        var scaleIndex = 0

        while (temp > 0) {
            val chunk = (temp % 1000).toInt()
            if (chunk > 0) {
                val chunkStr = convertChunkToWords(chunk)
                val scale = if (scaleIndex in scales.indices) scales[scaleIndex] else ""
                val fullChunk = if (scale.isNotEmpty()) "$chunkStr $scale" else chunkStr
                parts.add(0, fullChunk)
            }
            temp /= 1000
            scaleIndex++
        }

        return if (parts.isEmpty()) "صفر" else parts.joinToString(" و ")
    }

    private fun convertChunkToWords(chunk: Int): String {
        val h = chunk / 100
        val remainder = chunk % 100
        val t = remainder / 10
        val u = remainder % 10

        val words = mutableListOf<String>()

        if (h > 0) {
            words.add(hundreds[h])
        }

        if (remainder in 10..19) {
            words.add(teens[remainder - 10])
        } else {
            if (t > 0) {
                words.add(tens[t])
            }
            if (u > 0) {
                words.add(units[u])
            }
        }

        return words.joinToString(" و ")
    }

    fun currencyToWords(amountInRial: Long, currencyUnit: CurrencyUnit): String {
        val displayAmount = if (currencyUnit == CurrencyUnit.TOMAN) {
            amountInRial / 10L
        } else {
            amountInRial
        }
        if (displayAmount == 0L) return "صفر ${currencyUnit.title}"
        return "${numberToPersianWords(displayAmount)} ${currencyUnit.title}"
    }
}
