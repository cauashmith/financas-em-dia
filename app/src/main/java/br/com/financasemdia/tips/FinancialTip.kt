package br.com.financasemdia.tips

import androidx.annotation.DrawableRes
import br.com.financasemdia.R

enum class TipCategory(val label: String, @param:DrawableRes val iconRes: Int) {
    BUDGET("Orçamento", R.drawable.ic_tip_lightbulb),
    SAVINGS("Reserva e poupança", R.drawable.ic_tip_trending_up),
    DEBT("Dívidas", R.drawable.ic_tip_lightbulb),
    FOOD("Alimentação", R.drawable.ic_tip_lightbulb),
    HOUSING("Moradia", R.drawable.ic_tip_home),
    TRANSPORT("Transporte", R.drawable.ic_tip_lightbulb),
    LEISURE("Lazer", R.drawable.ic_tip_lightbulb),
    BILLS("Contas e serviços", R.drawable.ic_tip_home);

    companion object {
        fun fromNameOrNull(name: String?): TipCategory? = entries.firstOrNull { it.name == name }
    }
}

data class FinancialTip(
    val id: Int,
    val title: String,
    val summary: String,
    val content: String,
    val category: TipCategory,
    val steps: List<String>,
    val readingMinutes: Int? = null,
    val source: String? = null,
)
