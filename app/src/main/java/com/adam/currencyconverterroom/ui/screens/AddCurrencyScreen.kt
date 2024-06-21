package com.adam.currencyconverterroom.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.adam.currencyconverterroom.data.CurrencyViewModel
import com.adam.currencyconverterroom.data.CurrencyWithName
import com.adam.currencyconverterroom.ui.theme.CurrencyConverterRoomTheme
import kotlinx.coroutines.launch


@Composable
fun AddCurrencyScreen(viewModel: CurrencyViewModel, navController: NavHostController) {
    LaunchedEffect(Unit) {
        viewModel.fetchAllAvailableCurrencies()
    }
    val listOfCurrenciesToAdd by viewModel.availableCurrencies.collectAsStateWithLifecycle()
    val listOfExistingCurrencies by viewModel.codes.collectAsState(initial = emptyList())
    var searchText by remember { mutableStateOf("") }
    val currenciesToAdd = remember { mutableStateListOf<String>() }
    
    Column {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search Currencies") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        // Filtered List
        if (currenciesToAdd.size > 0) {
            ScrollerWithListToAdd(
                {
                    viewModel.viewModelScope.launch {
                        viewModel.fetchDataForNewEntries(
                            listOfExistingCurrencies, currenciesToAdd
                        )
                        navController.popBackStack()
                    }
                }, currenciesToAdd,
                onCurrencyTap = { tappedCurrency ->
                    currenciesToAdd.remove(tappedCurrency)
                }
            )
        }
        ListOfCurrencies(
            listOfCurrenciesToAdd, { code ->
                currenciesToAdd.add(code)
                searchText = ""
            },
            listOfExistingCurrencies,
            currenciesToAdd,
            searchText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollerWithListToAdd(
    onAction: () -> Unit,
    currenciesToAdd: List<String>,
    onCurrencyTap: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f), // Allow LazyRow to take available space
            contentPadding = PaddingValues(end = 8.dp) // Add padding before the button
        ) {
            items(currenciesToAdd) { currency ->
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(48.dp), // Set a fixed height for the cards
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    onClick = { onCurrencyTap(currency) }
                
                ) {
                    Text(
                        text = currency,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Use FilledTonalButton for a more prominent button
        FilledTonalButton(
            onClick = onAction,
            
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary, // Use a vibrant secondary color
                contentColor = MaterialTheme.colorScheme.onPrimary // Ensure text has good contrast
            )
        ) {
//            Text("+")
            Icon(Icons.Default.Add,"Add currencies icon")
        }
    }
}


@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun ScrollerWithListToAddPreview() {
    MaterialTheme {
        ScrollerWithListToAdd(
            onAction = { /* Handle button click */ },
            currenciesToAdd = mutableListOf("USD", "EUR", "JPY", "GBP", "AUD"),
            {}
        )
    }
}


@Composable
fun ListOfCurrencies(
    currencies: List<CurrencyWithName>,
    onCurrencyAdded: (String) -> Unit,
    addedCurrencies: List<String>,
    currenciesToAdd: List<String>,
    searchText: String
) {
    
    val filteredCurrencies = if (searchText.isBlank()) {
        currencies.filterNot {
            it.base in addedCurrencies || it.base in currenciesToAdd
        }
    } else {
        currencies.filterNot {
            it.base in addedCurrencies || it.base in currenciesToAdd
        }.filter {
            it.base.contains(searchText, ignoreCase = true) || it.name.contains(
                searchText,
                ignoreCase = true
            )
        }
    }
    LazyColumn {
        items(filteredCurrencies) { currencyToAdd ->
            CurrencyToAddCard(
                currencyToAdd, onCurrencyAdded = onCurrencyAdded
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyToAddCard(currencyToAdd: CurrencyWithName, onCurrencyAdded: (String) -> Unit) {
    Card(
        onClick = { onCurrencyAdded(currencyToAdd.base) },
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currencyToAdd.base,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            
            )
            Text(
                text = currencyToAdd.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ListPreview() {
    CurrencyConverterRoomTheme {
        ListOfCurrencies(
            currencies = listOf(
                CurrencyWithName("EUR", "Euro"),
                CurrencyWithName("USD", "US Dollar"),
                CurrencyWithName("JPY", "Japanese Yen"),
                CurrencyWithName("GBP", "British Pound"),
                CurrencyWithName("AUD", "Australian Dollar"),
                CurrencyWithName("CAD", "Canadian Dollar"),
                CurrencyWithName("CHF", "Swiss Franc"),
                CurrencyWithName("CNY", "Chinese Yuan"),
                CurrencyWithName("SEK", "Swedish Krona"),
                CurrencyWithName("NZD", "New Zealand Dollar")
            ),
            onCurrencyAdded = { /*TODO*/ },
            addedCurrencies = listOf("USD"),
            currenciesToAdd = listOf("EUR"),
            searchText = ""
        )
    }
}
