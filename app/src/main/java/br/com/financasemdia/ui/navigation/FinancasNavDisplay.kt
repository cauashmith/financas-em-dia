package br.com.financasemdia.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import br.com.financasemdia.di.AppContainer
import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.ui.detail.TransactionDetailRoute
import br.com.financasemdia.ui.detail.TransactionDetailViewModel
import br.com.financasemdia.ui.form.TransactionFormRoute
import br.com.financasemdia.ui.form.TransactionFormViewModel
import br.com.financasemdia.ui.transactions.TransactionListRoute
import br.com.financasemdia.ui.transactions.TransactionListViewModel

@Composable
fun FinancasNavDisplay(
    container: AppContainer,
    onOpenTips: (Category?) -> Unit,
) {
    val backStack = rememberNavBackStack(AppRoute.Transactions)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<AppRoute.Transactions> {
                TransactionListRoute(
                    viewModel = viewModel(factory = TransactionListViewModel.factory(container.transactionRepository)),
                    onAddTransaction = { backStack.add(AppRoute.TransactionForm()) },
                    onTransactionClick = { id -> backStack.add(AppRoute.TransactionDetail(id)) },
                    onOpenTips = onOpenTips,
                )
            }

            entry<AppRoute.TransactionDetail> { route ->
                TransactionDetailRoute(
                    viewModel = viewModel(
                        factory = TransactionDetailViewModel.factory(route.transactionId, container.transactionRepository),
                    ),
                    onBack = { backStack.pop(route) },
                    onEdit = { id -> backStack.add(AppRoute.TransactionForm(id)) },
                    onDeleted = { backStack.pop(route) },
                )
            }

            entry<AppRoute.TransactionForm> { route ->
                TransactionFormRoute(
                    viewModel = viewModel(
                        factory = TransactionFormViewModel.factory(route.transactionId, container.transactionRepository),
                    ),
                    onClose = { backStack.pop(route) },
                    onSaved = { backStack.pop(route) },
                )
            }
        },
    )
}

private fun NavBackStack<NavKey>.pop(route: NavKey) {
    if (lastOrNull() == route) removeAt(lastIndex)
}
