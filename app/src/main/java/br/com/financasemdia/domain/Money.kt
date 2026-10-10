package br.com.financasemdia.domain

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object Money {

    private val ptBr: Locale = Locale.forLanguageTag("pt-BR")

    private val validNumber = Regex("""\d{1,10}(\.\d{1,2})?""")

    fun format(cents: Long): String =
        NumberFormat.getCurrencyInstance(ptBr).format(BigDecimal.valueOf(cents, 2))

    fun parseToCents(raw: String): Long? {
        val cleaned = raw.trim()
            .removePrefix("R$")
            .replace(" ", "")
            .replace(" ", "")
        if (cleaned.isEmpty()) return null

        val normalized = if (cleaned.contains(',')) {
            cleaned.replace(".", "").replace(',', '.')
        } else {
            cleaned
        }
        if (!validNumber.matches(normalized)) return null

        return BigDecimal(normalized).movePointRight(2).toLong()
    }

    fun toInputText(cents: Long): String =
        BigDecimal.valueOf(cents, 2).toPlainString().replace('.', ',')
}
