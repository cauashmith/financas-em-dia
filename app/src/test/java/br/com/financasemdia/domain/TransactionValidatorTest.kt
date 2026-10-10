package br.com.financasemdia.domain

import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TransactionValidatorTest {

    private val today = LocalDate.of(2026, 10, 9)

    private fun input(
        description: String = "Mercado",
        amount: String = "150,00",
        type: TransactionType = TransactionType.EXPENSE,
        category: Category? = Category.FOOD,
        date: LocalDate = today,
        note: String = "",
    ) = TransactionFormInput(description, amount, type, category, date, note)

    private fun errorsOf(input: TransactionFormInput): FormErrors =
        (TransactionValidator.validate(input, today) as ValidationResult.Invalid).errors

    @Test
    fun `entrada valida gera rascunho com valores normalizados`() {
        val result = TransactionValidator.validate(input(description = "  Mercado  ", note = "   "), today)

        assertTrue(result is ValidationResult.Valid)
        val draft = (result as ValidationResult.Valid).draft
        assertEquals("Mercado", draft.description)
        assertEquals(15_000L, draft.amountCents)
        assertNull("observação em branco vira null", draft.note)
    }

    @Test
    fun `descricao obrigatoria e com tamanho minimo e maximo`() {
        assertEquals(FieldError.DESCRIPTION_REQUIRED, errorsOf(input(description = "  ")).description)
        assertEquals(FieldError.DESCRIPTION_TOO_SHORT, errorsOf(input(description = "a")).description)
        assertEquals(FieldError.DESCRIPTION_TOO_LONG, errorsOf(input(description = "x".repeat(61))).description)
    }

    @Test
    fun `valor obrigatorio, valido, positivo e com limite`() {
        assertEquals(FieldError.AMOUNT_REQUIRED, errorsOf(input(amount = "")).amount)
        assertEquals(FieldError.AMOUNT_INVALID, errorsOf(input(amount = "12,345")).amount)
        assertEquals(FieldError.AMOUNT_NOT_POSITIVE, errorsOf(input(amount = "0,00")).amount)
        assertEquals(FieldError.AMOUNT_TOO_LARGE, errorsOf(input(amount = "10000000,01")).amount)
    }

    @Test
    fun `categoria obrigatoria e compativel com o tipo`() {
        assertEquals(FieldError.CATEGORY_REQUIRED, errorsOf(input(category = null)).category)
        assertEquals(
            FieldError.CATEGORY_INCOMPATIBLE,
            errorsOf(input(type = TransactionType.INCOME, category = Category.FOOD)).category,
        )
    }

    @Test
    fun `data muito distante no futuro e observacao longa sao rejeitadas`() {
        assertEquals(FieldError.DATE_TOO_FAR, errorsOf(input(date = today.plusYears(2))).date)
        assertEquals(FieldError.NOTE_TOO_LONG, errorsOf(input(note = "x".repeat(201))).note)
    }
}
