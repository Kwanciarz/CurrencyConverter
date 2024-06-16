package com.adam.currencyconverterroom.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.adam.currencyconverterroom.data.CurrencyViewModel
import com.adam.currencyconverterroom.data.CurrencyWithName

@Composable
fun AddCurrencyScreen(viewModel: CurrencyViewModel, navController: NavHostController) {
    viewModel.fetchAllAvailableCurrencies()
    var listOfCurrenciesToAddState = viewModel.availableCurrencies.collectAsState()
    var listOfCurrenciesToAdd by remember { mutableStateOf(listOfCurrenciesToAddState.value.toMutableList()) }
    val listOfExistingCurrencies by viewModel.codes.collectAsState(initial = emptyList())
    var searchText by remember { mutableStateOf("") }
    var currenciesToAdd by remember { mutableStateOf(listOf<CurrencyWithName>())}
    
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
            listOfCurrenciesToAdd,
          
            listOfExistingCurrencies,
            currenciesToAdd.map{it.base},
            {
                listOfCurrenciesToAdd += it
            },
        )
    }


//    DisposableEffect(Unit) {
////        onDispose {
////            if (listOfCurrenciesToAdd.isNotEmpty()) {
////                viewModel.viewModelScope.launch {
////                    viewModel.fetchDataForNewEntries(
////                        addedCurrencies.map { it.base },
////                        listOfCurrenciesToAdd
////                    )
////                }
////            }
////        }
//    }
}


@Composable
fun ListOfCurrencies(
    currencies: List<CurrencyWithName>,
    addedCurrencies: List<String>,
    currenciesToAdd: List<String>,
    onCurrencyAdded: (String) -> Unit

) {
    LazyColumn {
//        item{
//            Text(text = "${addedCurrencies.toString()} \n ${currenciesToAdd.toString()} ")
//        }
        items(currencies.filterNot {
            it.base in addedCurrencies ||
                    it.base in currenciesToAdd
        }) { currencyToAdd ->
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