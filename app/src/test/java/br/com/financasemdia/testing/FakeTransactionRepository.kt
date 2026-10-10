package br.com.financasemdia.testing

import br.com.financasemdia.data.repository.TransactionRepository
import br.com.financasemdia.domain.model.Transaction
import br.com.financasemdia.domain.model.TransactionDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.YearMonth

class FakeTransactionRepository(initial: List<Transaction> = emptyList()) : TransactionRepository {

    private val transactions = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    var failOnSave = false
    val savedDrafts = mutableListOf<Pair<Long?, TransactionDraft>>()

    override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> =
        transactions.map { list -> list.filter { YearMonth.from(it.date) == month } }

    override fun observeById(id: Long): Flow<Transaction?> =
        transactions.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getById(id: Long): Transaction? = transactions.value.firstOrNull { it.id == id }

    override suspend fun save(id: Long?, draft: TransactionDraft): Long {
        if (failOnSave) error("falha simulada")
        savedDrafts += id to draft
        val savedId = id ?: nextId++
        val transaction = Transaction(
            id = savedId,
            description = draft.description,
            amountCents = draft.amountCents,
            type = draft.type,
            category = draft.category,
            date = draft.date,
            note = draft.note,
        )
        transactions.update { list -> list.filterNot { it.id == savedId } + transaction }
        return savedId
    }

    override suspend fun delete(id: Long) = transactions.update { list -> list.filterNot { it.id == id } }
}
