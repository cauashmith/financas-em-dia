package br.com.financasemdia.data.repository

import br.com.financasemdia.data.local.TransactionDao
import br.com.financasemdia.data.toDomain
import br.com.financasemdia.data.toEntity
import br.com.financasemdia.domain.model.Transaction
import br.com.financasemdia.domain.model.TransactionDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

interface TransactionRepository {
    fun observeByMonth(month: YearMonth): Flow<List<Transaction>>
    fun observeById(id: Long): Flow<Transaction?>
    suspend fun getById(id: Long): Transaction?

    suspend fun save(id: Long?, draft: TransactionDraft): Long
    suspend fun delete(id: Long)
}

class OfflineTransactionRepository(
    private val dao: TransactionDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : TransactionRepository {

    override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> =
        dao.observeBetween(
            startDay = month.atDay(1).toEpochDay(),
            endDay = month.atEndOfMonth().toEpochDay(),
        ).map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Transaction?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: Long): Transaction? = dao.getById(id)?.toDomain()

    override suspend fun save(id: Long?, draft: TransactionDraft): Long {
        val existing = id?.let { dao.getById(it) }
        val entity = draft.toEntity(
            id = existing?.id ?: 0,
            createdAt = existing?.createdAt ?: clock(),
        )
        val rowId = dao.upsert(entity)
        return if (existing != null) existing.id else rowId
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)
}
