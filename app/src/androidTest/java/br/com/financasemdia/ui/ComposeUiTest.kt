package br.com.financasemdia.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.financasemdia.domain.FieldError
import br.com.financasemdia.domain.FormErrors
import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.domain.model.MonthlySummary
import br.com.financasemdia.domain.model.Transaction
import br.com.financasemdia.domain.model.TransactionType
import br.com.financasemdia.ui.components.EmptyState
import br.com.financasemdia.ui.components.ErrorState
import br.com.financasemdia.ui.form.TransactionFormScreen
import br.com.financasemdia.ui.form.TransactionFormUiState
import br.com.financasemdia.ui.theme.FinancasTheme
import br.com.financasemdia.ui.transactions.TransactionListContent
import br.com.financasemdia.ui.transactions.TransactionListScreen
import br.com.financasemdia.ui.transactions.TransactionListUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class ComposeUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_mostraTextosEExecutaAcao() {
        var clicked = false
        composeRule.setContent {
            FinancasTheme {
                EmptyState(
                    icon = Icons.Outlined.Inbox,
                    title = "Nada por aqui",
                    message = "Adicione algo",
                    actionLabel = "Adicionar",
                    onAction = { clicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Nada por aqui").assertIsDisplayed()
        composeRule.onNodeWithText("Adicionar").performClick()
        assertTrue(clicked)
    }

    @Test
    fun errorState_botaoTentarNovamenteChamaCallback() {
        var retries = 0
        composeRule.setContent {
            FinancasTheme { ErrorState(title = "Erro", message = "Falhou", onRetry = { retries++ }) }
        }

        composeRule.onNodeWithText("Tentar novamente").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun listaDeTransacoes_cliqueNoItemInformaOId() {
        val transaction = Transaction(42, "Mercado", 5_000, TransactionType.EXPENSE, Category.FOOD, LocalDate.now(), null)
        var clickedId: Long? = null
        composeRule.setContent {
            FinancasTheme {
                TransactionListScreen(
                    uiState = TransactionListUiState(
                        month = YearMonth.now(),
                        content = TransactionListContent.Success(listOf(transaction)),
                        summary = MonthlySummary.from(listOf(transaction)),
                    ),
                    onPreviousMonth = {}, onNextMonth = {}, onFilterSelected = {}, onRetry = {},
                    onAddTransaction = {}, onTransactionClick = { clickedId = it }, onOpenTips = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Despesa: Mercado", substring = true).performClick()
        assertEquals(42L, clickedId)
    }

    @Test
    fun formulario_exibeMensagensDeErro() {
        composeRule.setContent {
            FinancasTheme {
                TransactionFormScreen(
                    uiState = TransactionFormUiState(
                        date = LocalDate.now(),
                        submitAttempted = true,
                        errors = FormErrors(
                            description = FieldError.DESCRIPTION_REQUIRED,
                            amount = FieldError.AMOUNT_REQUIRED,
                        ),
                    ),
                    onClose = {}, onDescriptionChange = {}, onAmountChange = {}, onTypeChange = {},
                    onCategoryChange = {}, onDateChange = {}, onNoteChange = {}, onSave = {}, onSaveErrorShown = {},
                )
            }
        }

        composeRule.onNodeWithText("Informe uma descrição.").assertIsDisplayed()
        composeRule.onNodeWithText("Informe o valor.").assertIsDisplayed()
        composeRule.onNodeWithText("Corrija os campos destacados para salvar.").assertExists()
    }
}
