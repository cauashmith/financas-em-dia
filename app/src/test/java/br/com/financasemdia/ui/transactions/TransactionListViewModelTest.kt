package br.com.financasemdia.ui.transactions

import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.domain.model.Transaction
import br.com.financasemdia.domain.model.TransactionType
import br.com.financasemdia.testing.FakeTransactionRepository
import br.com.financasemdia.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val october = YearMonth.of(2026, 10)

    private val transactions = listOf(
        Transaction(1, "Salário", 500_000, TransactionType.INCOME, Category.SALARY, october.atDay(5), null),
        Transaction(2, "Mercado", 40_000, TransactionType.EXPENSE, Category.FOOD, october.atDay(6), null),
        Transaction(3, "Restaurante", 15_000, TransactionType.EXPENSE, Category.FOOD, october.atDay(7), null),
        Transaction(4, "Ônibus", 20_000, TransactionType.EXPENSE, Category.TRANSPORT, october.atDay(8), null),
        Transaction(5, "Aluguel set.", 120_000, TransactionType.EXPENSE, Category.HOUSING, LocalDate.of(2026, 9, 1), null),
    )

    @Test
    fun `calcula resumo e maior categoria de gasto do mes`() = runTest {
        val viewModel = TransactionListViewModel(FakeTransactionRepository(transactions), october)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        val content = state.content as TransactionListContent.Success
        assertEquals(4, content.transactions.size)
        assertEquals(500_000L, state.summary?.incomeCents)
        assertEquals(75_000L, state.summary?.expenseCents)
        assertEquals(425_000L, state.summary?.balanceCents)
        assertEquals(Category.FOOD, state.topExpenseCategory)
    }

    @Test
    fun `filtro de receitas mostra apenas receitas e mantem o resumo completo`() = runTest {
        val viewModel = TransactionListViewModel(FakeTransactionRepository(transactions), october)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onFilterSelected(TypeFilter.INCOME)

        val state = viewModel.uiState.value
        val content = state.content as TransactionListContent.Success
        assertTrue(content.transactions.all { it.type == TransactionType.INCOME })
        assertEquals(75_000L, state.summary?.expenseCents)
    }

    @Test
    fun `mes sem transacoes resulta em estado vazio`() = runTest {
        val viewModel = TransactionListViewModel(FakeTransactionRepository(transactions), october)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onNextMonth()

        val state = viewModel.uiState.value
        assertEquals(YearMonth.of(2026, 11), state.month)
        assertEquals(TransactionListContent.Empty(isFiltered = false), state.content)
    }

    @Test
    fun `filtro sem resultados indica estado vazio filtrado`() = runTest {
        val viewModel = TransactionListViewModel(FakeTransactionRepository(transactions), october.minusMonths(1))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onFilterSelected(TypeFilter.INCOME)

        assertEquals(TransactionListContent.Empty(isFiltered = true), viewModel.uiState.value.content)
    }
}
