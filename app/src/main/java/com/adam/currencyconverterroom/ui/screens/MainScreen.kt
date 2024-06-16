package com.adam.currencyconverter.ui.screens

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.adam.currencyconverterroom.data.CurrencyViewModel
import com.adam.currencyconverterroom.data.CurrencyWithRate
import com.adam.currencyconverterroom.data.remote.ConnectivityRepository
import java.util.Locale

@Composable
fun MainScreen(viewModel: CurrencyViewModel, navController: NavHostController) {
//    val rates by viewModel.ratesForSelectedCurrency.collectAsState()
    var amount by rememberSaveable {
        mutableStateOf("10.0")
    }
    
    var selectedCode by rememberSaveable {
        mutableStateOf("PLN")
    }
    
    val updateSelectedCode: (String) -> Unit = { newCode ->
        selectedCode = newCode
    }
    LaunchedEffect(selectedCode) {
        Log.v("HELP",selectedCode)
        viewModel.updateSelectionOfRate(selectedCode)
    }
    var amountValue: Double? by remember { mutableStateOf(amount.toDoubleOrNull()) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                
                AmountField(amount = amount, onAmountChange = {
                    val cleanString = it.replace(",", ".").replace(" ", "")
                    amount = cleanString
                    amountValue = amount.toDoubleOrNull()
                })
                Text(selectedCode)
                
            }
            CurrencyList(amountValue, viewModel, updateSelectedCode, navController)
        }
    }
}

@Composable
fun CurrencyList(
//    selectedCurrencyWithRates: QueryResult<CurrencyWithRates>,
    amountValue: Double?,
    viewModel: CurrencyViewModel,
    action: (String) -> Unit,
    navController: NavHostController
) {
    
    
    val rates by viewModel.ratesBySelectedCode.collectAsState()
    Log.v("HELP", rates.toString())
    
    
    val connectivityRepository = ConnectivityRepository(LocalContext.current as ComponentActivity)
    val isOnline = connectivityRepository.isConnected.collectAsState(initial = false)
    
    ListOfCurrenciesOnSuccess(
        rates,
        amountValue,
        action,
        viewModel,
        isOnline.value,
        navController
    )
}

@Composable
fun ListOfCurrenciesOnSuccess(
    rates: List<CurrencyWithRate>,
    amountValue: Double?,
    action: (String) -> Unit,
    viewModel: CurrencyViewModel,
    isOnline: Boolean,
    navController: NavHostController
) {
    LazyColumn {
        this.items(
            rates
        ) {
            CurrencyRateItem(
                Pair(it.target, it.rate),
                amountValue,
                action,
                {},
            )
            
        }
        item {
            CurrencyAddButton(
                action = {
                    navController.navigate("add_currency_screen")
                }, isOnline
            )
        }
    }
}


@Composable
fun AmountField(amount: String, onAmountChange: (String) -> Unit) {
    TextField(
        value = amount,
        onValueChange = onAmountChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyRateItem(
    currency: Pair<String, Double>,
    amount: Double?,
    action: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = { action(currency.first) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currency.first,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (amount != null) {
                    String.format(Locale.getDefault(), "%.2f", amount * currency.second)
                } else "-",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
}

@Composable
fun CurrencyAddButton(
    action: () -> Unit, isInternetAvailable: Boolean, modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (!isInternetAvailable) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            IconButton(
                onClick = action,
                modifier = Modifier
                    .padding(7.dp)
                    .align(alignment = Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


//@Preview
//@Composable
//fun MainScreenPreview() {
//    androidx.compose.material3.MaterialTheme {
//        MainScreen(androidx.navigation.compose.rememberNavController())
//    }
//}


