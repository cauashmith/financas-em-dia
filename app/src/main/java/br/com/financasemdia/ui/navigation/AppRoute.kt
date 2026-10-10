package br.com.financasemdia.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppRoute : NavKey {

    @Serializable
    data object Transactions : AppRoute

    @Serializable
    data class TransactionDetail(val transactionId: Long) : AppRoute

    @Serializable
    data class TransactionForm(val transactionId: Long? = null) : AppRoute
}
