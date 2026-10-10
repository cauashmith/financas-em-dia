package br.com.financasemdia.ui.form

import br.com.financasemdia.domain.FieldError
import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.domain.model.Transaction
import br.com.financasemdia.domain.model.TransactionType
import br.com.financasemdia.testing.FakeTransactionRepository
import br.com.financasemdia.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class TransactionFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 10, 9)

    private fun newViewModel(repository: FakeTransactionRepository, id: Long? = null) =
        TransactionFormViewModel(id, repository, today = { today })

    @Test
    fun `salvar com campos vazios mostra erros e nao grava`() {
        val repository = FakeTransactionRepository()
        val viewModel = newViewModel(repository)

        viewModel.onSave()

        val state = viewModel.uiState.value
        assertEquals(FieldError.DESCRIPTION_REQUIRED, state.errors.description)
        assertEquals(FieldError.AMOUNT_REQUIRED, state.errors.amount)
        assertEquals(FieldError.CATEGORY_REQUIRED, state.errors.category)
        assertFalse(state.isSaved)
        assertTrue(repository.savedDrafts.isEmpty())
    }

    @Test
    fun `apos tentar salvar, os erros somem conforme o usuario corrige`() {
        val viewModel = newViewModel(FakeTransactionRepository())
        viewModel.onSave()

        viewModel.onDescriptionChange("Mercado")

        assertNull(viewModel.uiState.value.errors.description)
        assertNotNull(viewModel.uiState.value.errors.amount)
    }

    @Test
    fun `salvar dados validos grava no repository e sinaliza sucesso`() {
        val repository = FakeTransactionRepository()
        val viewModel = newViewModel(repository)

        viewModel.onDescriptionChange("Mercado")
        viewModel.onAmountChange("89,90")
        viewModel.onCategoryChange(Category.FOOD)
        viewModel.onSave()

        assertTrue(viewModel.uiState.value.isSaved)
        val (id, draft) = repository.savedDrafts.single()
        assertNull(id)
        assertEquals(8_990L, draft.amountCents)
        assertEquals(TransactionType.EXPENSE, draft.type)
    }

    @Test
    fun `falha ao salvar mostra mensagem e permite tentar novamente`() {
        val repository = FakeTransactionRepository().apply { failOnSave = true }
        val viewModel = newViewModel(repository)
        viewModel.onDescriptionChange("Mercado")
        viewModel.onAmountChange("10")
        viewModel.onCategoryChange(Category.FOOD)

        viewModel.onSave()

        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertFalse(state.isSaving)
        assertNotNull(state.saveErrorMessage)
    }

    @Test
    fun `trocar o tipo limpa categoria incompativel`() {
        val viewModel = newViewModel(FakeTransactionRepository())
        viewModel.onCategoryChange(Category.FOOD)

        viewModel.onTypeChange(TransactionType.INCOME)

        assertNull(viewModel.uiState.value.category)
        assertTrue(Category.SALARY in viewModel.uiState.value.availableCategories)
    }

    @Test
    fun `modo edicao carrega os dados existentes`() {
        val existing = Transaction(7, "Salário", 500_000, TransactionType.INCOME, Category.SALARY, today, "Outubro")
        val viewModel = newViewModel(FakeTransactionRepository(listOf(existing)), id = 7)

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertFalse(state.isLoading)
        assertEquals("Salário", state.description)
        assertEquals("5000,00", state.amountText)
        assertEquals(Category.SALARY, state.category)
        assertEquals("Outubro", state.note)
    }

    @Test
    fun `modo edicao com id inexistente indica falha no carregamento`() {
        val viewModel = newViewModel(FakeTransactionRepository(), id = 99)

        assertTrue(viewModel.uiState.value.loadFailed)
    }
}
