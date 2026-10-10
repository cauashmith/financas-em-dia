package br.com.financasemdia.ui.detail

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.financasemdia.domain.model.Transaction
import br.com.financasemdia.domain.model.TransactionType
import br.com.financasemdia.ui.components.CategoryIcon
import br.com.financasemdia.ui.components.EmptyState
import br.com.financasemdia.ui.components.ErrorState
import br.com.financasemdia.ui.components.LoadingState
import br.com.financasemdia.ui.format.label
import br.com.financasemdia.ui.format.signedAmount
import br.com.financasemdia.ui.format.toLongText
import br.com.financasemdia.ui.format.toShareText
import br.com.financasemdia.ui.theme.FinancasTheme

@Composable
fun TransactionDetailRoute(
    viewModel: TransactionDetailViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    TransactionDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onEdit = onEdit,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        onErrorMessageShown = viewModel::onErrorMessageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    uiState: TransactionDetailUiState,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onErrorMessageShown: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val loaded = (uiState.content as? DetailContent.Loaded)?.transaction

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (loaded != null) {
                        IconButton(onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, loaded.toShareText())
                            }
                            context.startActivity(Intent.createChooser(send, "Compartilhar transação"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Compartilhar")
                        }
                        IconButton(onClick = { onEdit(loaded.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog = true }, enabled = !uiState.isDeleting) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            AnimatedContent(
                targetState = uiState.content,
                contentKey = { it::class },
                label = "conteudo-detalhe",
                modifier = Modifier.widthIn(max = 640.dp),
            ) { content ->
                when (content) {
                    DetailContent.Loading -> LoadingState("Carregando transação…")
                    DetailContent.Error -> ErrorState(
                        title = "Erro ao carregar",
                        message = "Não foi possível ler esta transação.",
                        onRetry = onBack,
                    )
                    DetailContent.NotFound -> EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = "Transação não encontrada",
                        message = "Ela pode ter sido excluída.",
                        actionLabel = "Voltar",
                        onAction = onBack,
                    )
                    is DetailContent.Loaded -> TransactionDetailContent(content.transaction, uiState.isDeleting)
                }
            }
        }
    }

    if (showDeleteDialog && loaded != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir transação?") },
            text = { Text("\"${loaded.description}\" será removida permanentemente.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteConfirmed()
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun TransactionDetailContent(transaction: Transaction, isDeleting: Boolean) {
    val amountColor = if (transaction.type == TransactionType.INCOME) {
        FinancasTheme.financeColors.income
    } else {
        FinancasTheme.financeColors.expense
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.semantics(mergeDescendants = true) {},
        ) {
            CategoryIcon(transaction.category, size = 56.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = transaction.type.label(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = transaction.signedAmount(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = amountColor,
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow("Categoria", transaction.category.label)
                HorizontalDivider()
                DetailRow("Data", transaction.date.toLongText())
                HorizontalDivider()
                DetailRow(
                    label = "Observação",
                    value = transaction.note ?: "Sem observação",
                    isPlaceholder = transaction.note == null,
                )
            }
        }

        if (isDeleting) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text("Excluindo…")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isPlaceholder: Boolean = false) {
    Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = if (isPlaceholder) FontStyle.Italic else FontStyle.Normal,
        )
    }
}
