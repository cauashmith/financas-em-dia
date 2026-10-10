package br.com.financasemdia.data

import br.com.financasemdia.data.local.TransactionEntity
import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.domain.model.Transaction
import br.com.financasemdia.domain.model.TransactionDraft
import br.com.financasemdia.domain.model.TransactionType
import java.time.LocalDate

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    description = description,
    amountCents = amountCents,
    type = TransactionType.entries.firstOrNull { it.name == type } ?: TransactionType.EXPENSE,
    category = Category.entries.firstOrNull { it.name == category } ?: Category.OTHER,
    date = LocalDate.ofEpochDay(epochDay),
    note = note?.takeIf { it.isNotBlank() },
)

fun TransactionDraft.toEntity(id: Long, createdAt: Long): TransactionEntity = TransactionEntity(
    id = id,
    description = description,
    amountCents = amountCents,
    type = type.name,
    category = category.name,
    epochDay = date.toEpochDay(),
    note = note,
    createdAt = createdAt,
)
