package com.adam.currencyconverterroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adam.currencyconverter.ui.screens.MainScreen
import com.adam.currencyconverterroom.data.CurrencyViewModel
import com.adam.currencyconverterroom.data.remote.ConnectivityRepository
import com.adam.currencyconverterroom.ui.screens.AddCurrencyScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    // At the top level of your kotlin file:
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyConverterApp()
        }
    }
}
@Composable
fun CurrencyConverterApp() {
    val navController = rememberNavController()
    val viewModel: CurrencyViewModel = viewModel(factory = CurrencyViewModel.WordViewModelFactory((LocalContext.current.applicationContext as CurrencyConverterApplication).repository,
        ConnectivityRepository(LocalContext.current)
    ))

    NavHost(navController, startDestination = "main_screen") {
        composable("main_screen") { MainScreen(viewModel,navController) }
        composable("add_currency_screen") { AddCurrencyScreen(viewModel,navController) }
    }
}
