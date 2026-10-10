package br.com.financasemdia.di

import android.content.Context
import br.com.financasemdia.data.local.AppDatabase
import br.com.financasemdia.data.repository.OfflineTransactionRepository
import br.com.financasemdia.data.repository.TransactionRepository

interface AppContainer {
    val transactionRepository: TransactionRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy { AppDatabase.build(appContext) }

    override val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(database.transactionDao())
    }
}
