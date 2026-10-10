package br.com.financasemdia.domain.model

import java.time.LocalDate

enum class TransactionType { INCOME, EXPENSE }

enum class Category(val label: String, val allowedTypes: Set<TransactionType>) {
    SALARY("Salário", setOf(TransactionType.INCOME)),
    EXTRA_INCOME("Renda extra", setOf(TransactionType.INCOME)),
    INVESTMENTS("Investimentos", setOf(TransactionType.INCOME, TransactionType.EXPENSE)),
    FOOD("Alimentação", setOf(TransactionType.EXPENSE)),
    HOUSING("Moradia", setOf(TransactionType.EXPENSE)),
    TRANSPORT("Transporte", setOf(TransactionType.EXPENSE)),
    HEALTH("Saúde", setOf(TransactionType.EXPENSE)),
    EDUCATION("Educação", setOf(TransactionType.EXPENSE)),
    LEISURE("Lazer", setOf(TransactionType.EXPENSE)),
    BILLS("Contas e serviços", setOf(TransactionType.EXPENSE)),
    OTHER("Outros", setOf(TransactionType.INCOME, TransactionType.EXPENSE));

    companion object {
        fun forType(type: TransactionType): List<Category> = entries.filter { type in it.allowedTypes }
    }
}

data class Transaction(
    val id: Long,
    val description: String,
    val amountCents: Long,
    val type: TransactionType,
    val category: Category,
    val date: LocalDate,
    val note: String?,
)

data class TransactionDraft(
    val description: String,
    val amountCents: Long,
    val type: TransactionType,
    val category: Category,
    val date: LocalDate,
    val note: String?,
)

data class MonthlySummary(
    val incomeCents: Long,
    val expenseCents: Long,
) {
    val balanceCents: Long get() = incomeCents - expenseCents

    companion object {
        fun from(transactions: List<Transaction>) = MonthlySummary(
            incomeCents = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents },
            expenseCents = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents },
        )
    }
}
