package br.com.financasemdia.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.financasemdia.data.repository.TransactionRepository
import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.domain.model.MonthlySummary
import br.com.financasemdia.domain.model.Transaction
import br.com.financasemdia.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth

enum class TypeFilter(val label: String) {
    ALL("Todas"),
    INCOME("Receitas"),
    EXPENSE("Despesas"),
}

sealed interface TransactionListContent {
    data object Loading : TransactionListContent
    data object Error : TransactionListContent
    data class Empty(val isFiltered: Boolean) : TransactionListContent
    data class Success(val transactions: List<Transaction>) : TransactionListContent
}

data class TransactionListUiState(
    val month: YearMonth,
    val filter: TypeFilter = TypeFilter.ALL,
    val content: TransactionListContent = TransactionListContent.Loading,
    val summary: MonthlySummary? = null,
    val topExpenseCategory: Category? = null,
)

private sealed interface MonthLoad {
    data object Loading : MonthLoad
    data object Failed : MonthLoad
    data class Loaded(val transactions: List<Transaction>) : MonthLoad
}

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModel(
    private val repository: TransactionRepository,
    initialMonth: YearMonth = YearMonth.now(),
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(initialMonth)
    private val selectedFilter = MutableStateFlow(TypeFilter.ALL)
    private val retryCount = MutableStateFlow(0)

    private val monthLoad: Flow<MonthLoad> =
        combine(selectedMonth, retryCount) { month, _ -> month }
            .flatMapLatest { month ->
                repository.observeByMonth(month)
                    .map<List<Transaction>, MonthLoad> { MonthLoad.Loaded(it) }
                    .onStart { emit(MonthLoad.Loading) }
                    .catch { emit(MonthLoad.Failed) }
            }

    val uiState: StateFlow<TransactionListUiState> =
        combine(selectedMonth, selectedFilter, monthLoad, ::buildState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransactionListUiState(month = initialMonth),
            )

    fun onPreviousMonth() = selectedMonth.update { it.minusMonths(1) }

    fun onNextMonth() = selectedMonth.update { it.plusMonths(1) }

    fun onFilterSelected(filter: TypeFilter) {
        selectedFilter.value = filter
    }

    fun onRetry() = retryCount.update { it + 1 }

    private fun buildState(month: YearMonth, filter: TypeFilter, load: MonthLoad): TransactionListUiState =
        when (load) {
            MonthLoad.Loading -> TransactionListUiState(month, filter, TransactionListContent.Loading)
            MonthLoad.Failed -> TransactionListUiState(month, filter, TransactionListContent.Error)
            is MonthLoad.Loaded -> {
                val all = load.transactions
                val visible = when (filter) {
                    TypeFilter.ALL -> all
                    TypeFilter.INCOME -> all.filter { it.type == TransactionType.INCOME }
                    TypeFilter.EXPENSE -> all.filter { it.type == TransactionType.EXPENSE }
                }
                TransactionListUiState(
                    month = month,
                    filter = filter,
                    content = if (visible.isEmpty()) {
                        TransactionListContent.Empty(isFiltered = filter != TypeFilter.ALL && all.isNotEmpty())
                    } else {
                        TransactionListContent.Success(visible)
                    },
                    summary = MonthlySummary.from(all),
                    topExpenseCategory = topExpenseCategory(all),
                )
            }
        }

    private fun topExpenseCategory(transactions: List<Transaction>): Category? =
        transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .maxByOrNull { (_, items) -> items.sumOf { it.amountCents } }
            ?.key

    companion object {
        fun factory(repository: TransactionRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { TransactionListViewModel(repository) }
        }
    }
}
