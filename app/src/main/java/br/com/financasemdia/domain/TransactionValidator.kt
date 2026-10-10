package br.com.financasemdia.domain

import br.com.financasemdia.domain.model.Category
import br.com.financasemdia.domain.model.TransactionDraft
import br.com.financasemdia.domain.model.TransactionType
import java.time.LocalDate

data class TransactionFormInput(
    val description: String,
    val amountText: String,
    val type: TransactionType,
    val category: Category?,
    val date: LocalDate,
    val note: String,
)

enum class FieldError {
    DESCRIPTION_REQUIRED,
    DESCRIPTION_TOO_SHORT,
    DESCRIPTION_TOO_LONG,
    AMOUNT_REQUIRED,
    AMOUNT_INVALID,
    AMOUNT_NOT_POSITIVE,
    AMOUNT_TOO_LARGE,
    CATEGORY_REQUIRED,
    CATEGORY_INCOMPATIBLE,
    DATE_TOO_FAR,
    NOTE_TOO_LONG,
}

data class FormErrors(
    val description: FieldError? = null,
    val amount: FieldError? = null,
    val category: FieldError? = null,
    val date: FieldError? = null,
    val note: FieldError? = null,
) {
    val hasErrors: Boolean
        get() = listOfNotNull(description, amount, category, date, note).isNotEmpty()
}

sealed interface ValidationResult {
    data class Valid(val draft: TransactionDraft) : ValidationResult
    data class Invalid(val errors: FormErrors) : ValidationResult
}

object TransactionValidator {

    const val DESCRIPTION_MIN = 2
    const val DESCRIPTION_MAX = 60
    const val NOTE_MAX = 200
    const val MAX_AMOUNT_CENTS = 1_000_000_000L
    const val MAX_YEARS_AHEAD = 1L

    fun validate(input: TransactionFormInput, today: LocalDate): ValidationResult {
        val description = input.description.trim()
        val note = input.note.trim()
        val amountCents = Money.parseToCents(input.amountText)

        val errors = FormErrors(
            description = when {
                description.isEmpty() -> FieldError.DESCRIPTION_REQUIRED
                description.length < DESCRIPTION_MIN -> FieldError.DESCRIPTION_TOO_SHORT
                description.length > DESCRIPTION_MAX -> FieldError.DESCRIPTION_TOO_LONG
                else -> null
            },
            amount = when {
                input.amountText.isBlank() -> FieldError.AMOUNT_REQUIRED
                amountCents == null -> FieldError.AMOUNT_INVALID
                amountCents <= 0 -> FieldError.AMOUNT_NOT_POSITIVE
                amountCents > MAX_AMOUNT_CENTS -> FieldError.AMOUNT_TOO_LARGE
                else -> null
            },
            category = when {
                input.category == null -> FieldError.CATEGORY_REQUIRED
                input.type !in input.category.allowedTypes -> FieldError.CATEGORY_INCOMPATIBLE
                else -> null
            },
            date = if (input.date.isAfter(today.plusYears(MAX_YEARS_AHEAD))) FieldError.DATE_TOO_FAR else null,
            note = if (note.length > NOTE_MAX) FieldError.NOTE_TOO_LONG else null,
        )

        if (errors.hasErrors || amountCents == null || input.category == null) {
            return ValidationResult.Invalid(errors)
        }

        return ValidationResult.Valid(
            TransactionDraft(
                description = description,
                amountCents = amountCents,
                type = input.type,
                category = input.category,
                date = input.date,
                note = note.ifEmpty { null },
            )
        )
    }
}
