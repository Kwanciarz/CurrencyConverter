package com.adam.currencyconverterroom.ui.screens

import androidx.collection.floatListOf
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.adam.currencyconverterroom.data.CurrencyViewModel
import com.adam.currencyconverterroom.data.CurrencyWithName
import com.adam.currencyconverterroom.data.CurrencyWithRate
import kotlinx.coroutines.launch


@Composable
fun AddCurrencyScreen(viewModel: CurrencyViewModel, navController: NavHostController) {
    LaunchedEffect(Unit) {
        viewModel.fetchAllAvailableCurrencies()
    }
    val listOfCurrenciesToAddState by viewModel.availableCurrencies.collectAsStateWithLifecycle()
    val listOfExistingCurrencies by viewModel.codes.collectAsState(initial = emptyList())
    var searchText by remember { mutableStateOf("") }
    val currenciesToAdd = remember { mutableStateListOf<String>()}

    DisposableEffect(Unit) {
        onDispose {
            if (currenciesToAdd.isNotEmpty()) {
                viewModel.viewModelScope.launch {
                    viewModel.fetchDataForNewEntries(
                        listOfExistingCurrencies,
                        currenciesToAdd
                    )
                }
            }
        }
    }
    Column {
        // Text Field for Search
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search Currencies") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        // Filtered List
        ListOfCurrencies(
            listOfCurrenciesToAddState,
            { code ->
                currenciesToAdd.add(code)
            },
            listOfExistingCurrencies,
            currenciesToAdd,
            searchText
        )
    }
}


@Composable
fun ListOfCurrencies(
    currencies: List<CurrencyWithName>,
    onCurrencyAdded: (String) -> Unit,
    addedCurrencies: List<String>,
    currenciesToAdd: List<String>,
    searchText:String
) {
    
    val filteredCurrencies = if (searchText.isBlank()) {currencies.filterNot {
        it.base in addedCurrencies || it.base in currenciesToAdd
    }
    } else {
        currencies.filterNot {
            it.base in addedCurrencies || it.base in currenciesToAdd
        }.filter {
            it.base.contains(searchText, ignoreCase = true) ||
                    it.name.contains(searchText, ignoreCase = true)
        }
    }
    LazyColumn {
//        item{
//            Text(text = "${currencies.toString()} \n${addedCurrencies.toString()} \n ${currenciesToAdd.toString()} ")
//        }
        items(filteredCurrencies) { currencyToAdd ->
            CurrencyToAddCard(
                currencyToAdd,
                onCurrencyAdded = onCurrencyAdded
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyToAddCard(currencyToAdd: CurrencyWithName, onCurrencyAdded: (String) -> Unit) {
    Card(onClick = { onCurrencyAdded(currencyToAdd.base) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(
                text = currencyToAdd.base,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = currencyToAdd.name,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}