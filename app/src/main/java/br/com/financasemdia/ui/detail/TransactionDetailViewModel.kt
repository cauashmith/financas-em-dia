package br.com.financasemdia.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.financasemdia.data.repository.TransactionRepository
import br.com.financasemdia.domain.model.Transaction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DetailContent {
    data object Loading : DetailContent
    data object NotFound : DetailContent
    data object Error : DetailContent
    data class Loaded(val transaction: Transaction) : DetailContent
}

data class TransactionDetailUiState(
    val content: DetailContent = DetailContent.Loading,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
)

private data class DeleteState(
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
)

class TransactionDetailViewModel(
    private val transactionId: Long,
    private val repository: TransactionRepository,
) : ViewModel() {

    private val deleteState = MutableStateFlow(DeleteState())
    private var deleteJob: Job? = null

    private val content = repository.observeById(transactionId)
        .map { transaction ->
            if (transaction == null) DetailContent.NotFound else DetailContent.Loaded(transaction)
        }
        .onStart { emit(DetailContent.Loading) }
        .catch { emit(DetailContent.Error) }

    val uiState: StateFlow<TransactionDetailUiState> =
        combine(content, deleteState) { content, delete ->
            TransactionDetailUiState(
                content = content,
                isDeleting = delete.isDeleting,
                isDeleted = delete.isDeleted,
                errorMessage = delete.errorMessage,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionDetailUiState())

    fun onDeleteConfirmed() {
        if (deleteJob?.isActive == true) return
        deleteJob = viewModelScope.launch {
            deleteState.update { it.copy(isDeleting = true, errorMessage = null) }
            try {
                repository.delete(transactionId)
                deleteState.update { it.copy(isDeleting = false, isDeleted = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                deleteState.update {
                    it.copy(isDeleting = false, errorMessage = "Não foi possível excluir a transação. Tente novamente.")
                }
            }
        }
    }

    fun onErrorMessageShown() = deleteState.update { it.copy(errorMessage = null) }

    companion object {
        fun factory(id: Long, repository: TransactionRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { TransactionDetailViewModel(id, repository) }
        }
    }
}
