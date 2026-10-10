package br.com.financasemdia

import android.app.Application
import br.com.financasemdia.di.AppContainer
import br.com.financasemdia.di.DefaultAppContainer

class FinancasApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
