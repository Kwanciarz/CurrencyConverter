package com.adam.currencyconverterroom.ui.screens

import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.adam.currencyconverterroom.data.CurrencyViewModel
import com.adam.currencyconverterroom.data.CurrencyWithRate
import com.adam.currencyconverterroom.data.SharedPrefsHelper
import com.adam.currencyconverterroom.ui.theme.CurrencyConverterRoomTheme
import java.text.DecimalFormat


@Composable
fun MainScreen(viewModel: CurrencyViewModel, navController: NavController) {
    val rates by viewModel.ratesBySelectedCode.collectAsState()
    
    val prefs = SharedPrefsHelper(LocalContext.current)
    var selectedCode by rememberSaveable {
        mutableStateOf(prefs.getSelectedCode("PLN"))
    }
    
    val updateSelectedCode: (String) -> Unit = { newCode ->
        selectedCode = newCode
        prefs.saveSelectedCode(newCode)
    }
    LaunchedEffect(selectedCode) {
        Log.v("HELP", selectedCode)
        viewModel.updateSelectionOfRate(selectedCode)
    }
    val amount by viewModel.amount.collectAsState()
    val internetState by viewModel.getInternetState().collectAsState(initial = false)
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        bottomBar = {
            Text(
                "Data source: Frankfurter.app",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(textAlign = TextAlign.Center, fontStyle = FontStyle.Italic)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RowWithAmount(amount, { newAmount ->
                val filteredValue = newAmount.filter { it.isDigit() || it == '.' || it==',' }
                    .replace(',', '.')
                viewModel.updateAmount(filteredValue)
            }, selectedCode)
            Spacer(modifier = Modifier.height(16.dp))
            ListOfRates(rates, amount, internetState, updateSelectedCode, navController, viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowWithAmount(amount: String, onAmountChange: (String) -> Unit, selectedCode: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Enter amount") },
            modifier = Modifier.weight(0.6f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            selectedCode,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center),
            modifier = Modifier.weight(0.4f)
        )
    }
}

@Composable
fun ListOfRates(
    rates: List<CurrencyWithRate>,
    amount: String,
    internetState: Boolean,
    updateSelectedCode: (String) -> Unit,
    navController: NavController,
    viewModel: CurrencyViewModel
) {
    LazyColumn(Modifier.padding(horizontal = 10.dp)) {
        items(rates, key = { it.target }) {
            RateItem(it, amount, updateSelectedCode, viewModel, rates.size > 1)
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Spacer(modifier = Modifier.height(10.dp)) // Spacing before button
            ButtonAdd(internetState, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateItem(
    rate: CurrencyWithRate,
    amount: String,
    updateSelectedCode: (String) -> Unit,
    viewModel: CurrencyViewModel,
    displayDeleteButton: Boolean
) {
    val decimalFormat = DecimalFormat("#.##")
    val amountDouble = amount.toDoubleOrNull()
    var isRevealed by remember { mutableStateOf(false) }
    val revealOffset = animateDpAsState(
        targetValue = if (isRevealed) (-40).dp else 0.dp, animationSpec = tween(
            durationMillis = 200, easing = LinearOutSlowInEasing
        ), label = "Animation of delete button reveal"
    )
    
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .offset { IntOffset(revealOffset.value.roundToPx(), 0) }
        .pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                when {
                    dragAmount < 0 && !isRevealed -> isRevealed = true
                    dragAmount > 0 && isRevealed -> isRevealed = false
                }
            }
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = { updateSelectedCode(rate.target) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = rate.target)
                val value = rate.rate
                val textOfConvertedCurrency = if (amountDouble != null) {
                    decimalFormat.format(amountDouble * value)
                } else "-"
                Text(
                    text = textOfConvertedCurrency,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (displayDeleteButton && isRevealed) {
                IconButton(onClick = { viewModel.deleteCurrency(rate.target) }) {
                    Icon(Icons.Rounded.Delete, "Delete ${rate.target}")
                }
            }
        }
    }
}


@Composable
fun ButtonAdd(isInternet: Boolean, navController: NavController) {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 5.dp)
        .height(70.dp)
    
    Card(
        modifier = buttonModifier, colors = CardDefaults.cardColors(
            containerColor = if (isInternet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        if (isInternet) {
            IconButton(
                onClick = {
                    navController.navigate("add_currency_screen")
                }, modifier = buttonModifier
            ) {
                Icon(Icons.Rounded.Add, "Add")
            }
        } else {
            Box(
                modifier = buttonModifier, contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, "No internet access")
            }
        }
    }
}


@Preview
@Composable
private fun buttonAdd() {
    CurrencyConverterRoomTheme {
        Column {
            ButtonAdd(true, rememberNavController())
            ButtonAdd(false, rememberNavController())
        }
    }
}





