package br.com.financasemdia.ui.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.financasemdia.domain.FieldError
import br.com.financasemdia.domain.TransactionValidator
import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.domain.model.TransactionType
import br.com.financasemdia.ui.components.EmptyState
import br.com.financasemdia.ui.components.LoadingState
import br.com.financasemdia.ui.format.label
import br.com.financasemdia.ui.format.toShortText
import java.time.LocalDate

private const val MILLIS_PER_DAY = 86_400_000L

@Composable
fun TransactionFormRoute(
    viewModel: TransactionFormViewModel,
    onClose: () -> Unit,
    onSaved: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    TransactionFormScreen(
        uiState = uiState,
        onClose = onClose,
        onDescriptionChange = viewModel::onDescriptionChange,
        onAmountChange = viewModel::onAmountChange,
        onTypeChange = viewModel::onTypeChange,
        onCategoryChange = viewModel::onCategoryChange,
        onDateChange = viewModel::onDateChange,
        onNoteChange = viewModel::onNoteChange,
        onSave = viewModel::onSave,
        onSaveErrorShown = viewModel::onSaveErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    uiState: TransactionFormUiState,
    onClose: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onSaveErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveErrorMessage) {
        uiState.saveErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSaveErrorShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Editar transação" else "Nova transação") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Fechar sem salvar")
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
            when {
                uiState.isLoading -> LoadingState("Carregando transação…")
                uiState.loadFailed -> EmptyState(
                    icon = Icons.Outlined.ErrorOutline,
                    title = "Transação não encontrada",
                    message = "Não foi possível carregar os dados para edição.",
                    actionLabel = "Voltar",
                    onAction = onClose,
                )
                else -> FormContent(
                    uiState = uiState,
                    onDescriptionChange = onDescriptionChange,
                    onAmountChange = onAmountChange,
                    onTypeChange = onTypeChange,
                    onCategoryChange = onCategoryChange,
                    onDateChange = onDateChange,
                    onNoteChange = onNoteChange,
                    onSave = onSave,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FormContent(
    uiState: TransactionFormUiState,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val errors = uiState.errors

    Column(
        modifier = Modifier
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle("Tipo")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val types = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
            types.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = uiState.type == type,
                    onClick = { onTypeChange(type) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                ) { Text(type.label()) }
            }
        }

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Descrição") },
            singleLine = true,
            isError = errors.description != null,
            supportingText = {
                Text(errors.description?.message() ?: "${uiState.description.trim().length}/${TransactionValidator.DESCRIPTION_MAX}")
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .errorSemantics(errors.description),
        )

        OutlinedTextField(
            value = uiState.amountText,
            onValueChange = onAmountChange,
            label = { Text("Valor") },
            prefix = { Text("R$ ") },
            placeholder = { Text("0,00") },
            singleLine = true,
            isError = errors.amount != null,
            supportingText = { Text(errors.amount?.message() ?: "Use vírgula para os centavos. Ex.: 125,90") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .errorSemantics(errors.amount),
        )

        SectionTitle("Categoria")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.errorSemantics(errors.category),
        ) {
            uiState.availableCategories.forEach { category ->
                FilterChip(
                    selected = uiState.category == category,
                    onClick = { onCategoryChange(category) },
                    label = { Text(category.label) },
                )
            }
        }
        FieldErrorText(errors.category)

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
            Text(
                text = "Data: ${uiState.date.toShortText()}",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        FieldErrorText(errors.date)

        OutlinedTextField(
            value = uiState.note,
            onValueChange = onNoteChange,
            label = { Text("Observação (opcional)") },
            minLines = 2,
            maxLines = 5,
            isError = errors.note != null,
            supportingText = {
                Text(errors.note?.message() ?: "${uiState.note.trim().length}/${TransactionValidator.NOTE_MAX}")
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier
                .fillMaxWidth()
                .errorSemantics(errors.note),
        )

        AnimatedVisibility(visible = errors.hasErrors) {
            Text(
                text = "Corrija os campos destacados para salvar.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        Button(
            onClick = onSave,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) {
            if (uiState.isSaving) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Salvando…", modifier = Modifier.padding(start = 12.dp))
                }
            } else {
                Text(if (uiState.isEditing) "Salvar alterações" else "Salvar transação")
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date.toEpochDay() * MILLIS_PER_DAY,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onDateChange(LocalDate.ofEpochDay(it / MILLIS_PER_DAY)) }
                    showDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun FieldErrorText(error: FieldError?) {
    AnimatedVisibility(visible = error != null) {
        Text(
            text = error?.message().orEmpty(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun Modifier.errorSemantics(error: FieldError?): Modifier =
    if (error == null) this else semantics { error(error.message()) }

fun FieldError.message(): String = when (this) {
    FieldError.DESCRIPTION_REQUIRED -> "Informe uma descrição."
    FieldError.DESCRIPTION_TOO_SHORT -> "A descrição deve ter pelo menos ${TransactionValidator.DESCRIPTION_MIN} caracteres."
    FieldError.DESCRIPTION_TOO_LONG -> "A descrição deve ter no máximo ${TransactionValidator.DESCRIPTION_MAX} caracteres."
    FieldError.AMOUNT_REQUIRED -> "Informe o valor."
    FieldError.AMOUNT_INVALID -> "Valor inválido. Use o formato 1234,56."
    FieldError.AMOUNT_NOT_POSITIVE -> "O valor deve ser maior que zero."
    FieldError.AMOUNT_TOO_LARGE -> "O valor máximo é R$ 10.000.000,00."
    FieldError.CATEGORY_REQUIRED -> "Escolha uma categoria."
    FieldError.CATEGORY_INCOMPATIBLE -> "Essa categoria não combina com o tipo escolhido."
    FieldError.DATE_TOO_FAR -> "A data não pode ser mais de ${TransactionValidator.MAX_YEARS_AHEAD} ano no futuro."
    FieldError.NOTE_TOO_LONG -> "A observação deve ter no máximo ${TransactionValidator.NOTE_MAX} caracteres."
}
