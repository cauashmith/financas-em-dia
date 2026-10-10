package br.com.financasemdia.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.domain.model.MonthlySummary
import br.com.financasemdia.domain.model.Transaction
import br.com.financasemdia.domain.model.TransactionType
import br.com.financasemdia.ui.components.EmptyState
import br.com.financasemdia.ui.components.ErrorState
import br.com.financasemdia.ui.components.LoadingState
import br.com.financasemdia.ui.components.SummaryCard
import br.com.financasemdia.ui.components.TransactionItem
import br.com.financasemdia.ui.format.toTitle
import br.com.financasemdia.ui.theme.FinancasTheme
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun TransactionListRoute(
    viewModel: TransactionListViewModel,
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onOpenTips: (Category?) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionListScreen(
        uiState = uiState,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onFilterSelected = viewModel::onFilterSelected,
        onRetry = viewModel::onRetry,
        onAddTransaction = onAddTransaction,
        onTransactionClick = onTransactionClick,
        onOpenTips = { onOpenTips(uiState.topExpenseCategory) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    uiState: TransactionListUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onFilterSelected: (TypeFilter) -> Unit,
    onRetry: () -> Unit,
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onOpenTips: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finanças em Dia") },
                actions = {
                    IconButton(onClick = onOpenTips) {
                        Icon(Icons.Outlined.Lightbulb, contentDescription = "Dicas financeiras")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nova transação") },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "month") {
                    MonthSelector(month = uiState.month, onPrevious = onPreviousMonth, onNext = onNextMonth)
                }
                uiState.summary?.let { summary ->
                    item(key = "summary") { SummaryCard(summary = summary, modifier = Modifier.animateItem()) }
                }
                item(key = "filters") {
                    TypeFilterRow(selected = uiState.filter, onFilterSelected = onFilterSelected)
                }
                when (val content = uiState.content) {
                    TransactionListContent.Loading -> item(key = "loading") {
                        LoadingState("Carregando transações…", Modifier.animateItem())
                    }

                    TransactionListContent.Error -> item(key = "error") {
                        ErrorState(
                            title = "Não foi possível carregar",
                            message = "Ocorreu um erro ao ler suas transações.",
                            onRetry = onRetry,
                            modifier = Modifier.animateItem(),
                        )
                    }

                    is TransactionListContent.Empty -> item(key = "empty") {
                        if (content.isFiltered) {
                            EmptyState(
                                icon = Icons.Outlined.FilterAltOff,
                                title = "Nada encontrado com esse filtro",
                                message = "Não há ${uiState.filter.label.lowercase()} em ${uiState.month.toTitle()}.",
                                actionLabel = "Mostrar todas",
                                onAction = { onFilterSelected(TypeFilter.ALL) },
                                modifier = Modifier.animateItem(),
                            )
                        } else {
                            EmptyState(
                                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                                title = "Nenhuma transação em ${uiState.month.toTitle()}",
                                message = "Registre suas receitas e despesas para acompanhar o saldo do mês.",
                                actionLabel = "Adicionar transação",
                                onAction = onAddTransaction,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    is TransactionListContent.Success -> items(
                        items = content.transactions,
                        key = { it.id },
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior")
        }
        Text(
            text = month.toTitle(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo mês")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeFilterRow(selected: TypeFilter, onFilterSelected: (TypeFilter) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TypeFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else {
                    null
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionListScreenPreview() {
    val transactions = listOf(
        Transaction(1, "Salário", 500_000, TransactionType.INCOME, Category.SALARY, LocalDate.now(), null),
        Transaction(2, "Mercado", 35_090, TransactionType.EXPENSE, Category.FOOD, LocalDate.now(), "Compra do mês"),
    )
    FinancasTheme {
        TransactionListScreen(
            uiState = TransactionListUiState(
                month = YearMonth.now(),
                content = TransactionListContent.Success(transactions),
                summary = MonthlySummary.from(transactions),
            ),
            onPreviousMonth = {}, onNextMonth = {}, onFilterSelected = {}, onRetry = {},
            onAddTransaction = {}, onTransactionClick = {}, onOpenTips = {},
        )
    }
}
