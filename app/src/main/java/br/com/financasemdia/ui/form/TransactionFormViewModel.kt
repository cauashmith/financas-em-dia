package br.com.financasemdia.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.financasemdia.data.repository.TransactionRepository
import br.com.financasemdia.domain.FormErrors
import br.com.financasemdia.domain.Money
import br.com.financasemdia.domain.TransactionFormInput
import br.com.financasemdia.domain.TransactionValidator
import br.com.financasemdia.domain.ValidationResult
import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.domain.model.TransactionType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TransactionFormUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val description: String = "",
    val amountText: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: Category? = null,
    val date: LocalDate,
    val note: String = "",
    val errors: FormErrors = FormErrors(),
    val submitAttempted: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val saveErrorMessage: String? = null,
) {
    val availableCategories: List<Category> get() = Category.forType(type)
}

class TransactionFormViewModel(
    private val transactionId: Long?,
    private val repository: TransactionRepository,
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TransactionFormUiState(isEditing = transactionId != null, isLoading = transactionId != null, date = today())
    )
    val uiState: StateFlow<TransactionFormUiState> = _uiState.asStateFlow()

    private var saveJob: Job? = null

    init {
        if (transactionId != null) loadTransaction(transactionId)
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = try {
                repository.getById(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            _uiState.update { state ->
                if (transaction == null) {
                    state.copy(isLoading = false, loadFailed = true)
                } else {
                    state.copy(
                        isLoading = false,
                        description = transaction.description,
                        amountText = Money.toInputText(transaction.amountCents),
                        type = transaction.type,
                        category = transaction.category,
                        date = transaction.date,
                        note = transaction.note.orEmpty(),
                    )
                }
            }
        }
    }

    fun onDescriptionChange(value: String) = updateAndRevalidate { it.copy(description = value) }

    fun onAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == ',' || it == '.' }.take(16)
        updateAndRevalidate { it.copy(amountText = filtered) }
    }

    fun onTypeChange(type: TransactionType) = updateAndRevalidate { state ->
        val keepCategory = state.category?.takeIf { type in it.allowedTypes }
        state.copy(type = type, category = keepCategory)
    }

    fun onCategoryChange(category: Category) = updateAndRevalidate { it.copy(category = category) }

    fun onDateChange(date: LocalDate) = updateAndRevalidate { it.copy(date = date) }

    fun onNoteChange(value: String) = updateAndRevalidate { it.copy(note = value) }

    fun onSaveErrorShown() = _uiState.update { it.copy(saveErrorMessage = null) }

    fun onSave() {
        if (saveJob?.isActive == true) return
        val state = _uiState.value
        when (val result = TransactionValidator.validate(state.toInput(), today())) {
            is ValidationResult.Invalid -> _uiState.update {
                it.copy(errors = result.errors, submitAttempted = true)
            }

            is ValidationResult.Valid -> {
                _uiState.update { it.copy(errors = FormErrors(), submitAttempted = true, isSaving = true) }
                saveJob = viewModelScope.launch {
                    try {
                        repository.save(transactionId, result.draft)
                        _uiState.update { it.copy(isSaving = false, isSaved = true) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(isSaving = false, saveErrorMessage = "Não foi possível salvar. Tente novamente.")
                        }
                    }
                }
            }
        }
    }

    private fun updateAndRevalidate(transform: (TransactionFormUiState) -> TransactionFormUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            if (!updated.submitAttempted) {
                updated
            } else {
                val errors = when (val result = TransactionValidator.validate(updated.toInput(), today())) {
                    is ValidationResult.Invalid -> result.errors
                    is ValidationResult.Valid -> FormErrors()
                }
                updated.copy(errors = errors)
            }
        }
    }

    private fun TransactionFormUiState.toInput() = TransactionFormInput(
        description = description,
        amountText = amountText,
        type = type,
        category = category,
        date = date,
        note = note,
    )

    companion object {
        fun factory(id: Long?, repository: TransactionRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { TransactionFormViewModel(id, repository) }
        }
    }
}
