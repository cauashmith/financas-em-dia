package br.com.financasemdia.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.financasemdia.FinancasApplication
import br.com.financasemdia.tips.TipsActivity
import br.com.financasemdia.ui.navigation.FinancasNavDisplay
import br.com.financasemdia.ui.theme.FinancasTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as FinancasApplication).container

        setContent {
            FinancasTheme {
                FinancasNavDisplay(
                    container = container,
                    onOpenTips = { topExpenseCategory ->
                        startActivity(TipsActivity.newIntent(this, topExpenseCategory?.name))
                    },
                )
            }
        }
    }
}
