package br.com.financasemdia.ui.format

import br.com.financasemdia.domain.Money
import br.com.financasemdia.domain.model.Transaction
import br.com.financasemdia.domain.model.TransactionType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
private val shortDate = DateTimeFormatter.ofPattern("dd/MM/yyyy", ptBr)
private val longDate = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", ptBr)
private val monthTitle = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", ptBr)

fun LocalDate.toShortText(): String = format(shortDate)

fun LocalDate.toLongText(): String = format(longDate)

fun YearMonth.toTitle(): String = format(monthTitle).replaceFirstChar { it.titlecase(ptBr) }

fun TransactionType.label(): String = when (this) {
    TransactionType.INCOME -> "Receita"
    TransactionType.EXPENSE -> "Despesa"
}

fun Transaction.signedAmount(): String = when (type) {
    TransactionType.INCOME -> "+ ${Money.format(amountCents)}"
    TransactionType.EXPENSE -> "− ${Money.format(amountCents)}"
}

fun Transaction.toShareText(): String = buildString {
    appendLine("${type.label()}: $description")
    appendLine("Valor: ${Money.format(amountCents)}")
    appendLine("Categoria: ${category.label}")
    append("Data: ${date.toShortText()}")
    note?.let { append("\nObservação: $it") }
}
