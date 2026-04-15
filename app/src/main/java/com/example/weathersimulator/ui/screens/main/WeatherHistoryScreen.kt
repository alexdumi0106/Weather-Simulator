package com.example.weathersimulator.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherHistoryScreen(
    state: WeatherUiState,
    onBackClick: () -> Unit,
    onLoadHistory: () -> Unit,
    onMonthSelected: (String) -> Unit,
    onOpenSelectedDay: () -> Unit
) {
    var yearExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf<String?>(null) }
    var selectedMonthKey by remember { mutableStateOf<String?>(null) }
    var isMonthSelectionConfirmed by remember { mutableStateOf(state.selectedHistoryMonth != null) }
    val scrollState = rememberScrollState()
    val availableYears = remember(state.availableHistoryMonths) {
        state.availableHistoryMonths
            .map { it.key.substring(0, 4) }
            .distinct()
            .sortedDescending()
    }
    val monthsForSelectedYear = remember(selectedYear, state.availableHistoryMonths) {
        val year = selectedYear
        if (year == null) emptyList() else {
            state.availableHistoryMonths.filter { it.key.startsWith("$year-") }
        }
    }
    val selectedMonth = state.availableHistoryMonths.firstOrNull { it.key == state.selectedHistoryMonth }

    LaunchedEffect(Unit) {
        if (state.availableHistoryMonths.isEmpty() && !state.isLoading) {
            onLoadHistory()
        }
    }

    LaunchedEffect(state.availableHistoryMonths, state.selectedHistoryMonth) {
        val currentMonth = selectedMonth ?: state.availableHistoryMonths.firstOrNull()
        if (currentMonth != null) {
            selectedYear = currentMonth.key.substring(0, 4)
            selectedMonthKey = currentMonth.key
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F7FB),
        topBar = {
            TopAppBar(
                title = { Text("Weather History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF3F7FB),
                            Color(0xFFE8EFF7),
                            Color(0xFFFDFDFE)
                        )
                    )
                )
        ) {
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4F3)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                                color = Color(0xFF7A1E1E)
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF214A6A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Historical weather",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "Alege anul, apoi luna, ca să ajungi la ziua dorită din istoricul meteo.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "Date locale din CSV",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Select period",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { yearExpanded = true },
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Year",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = Color(0xFF6A7280)
                                                    )
                                                    Text(
                                                        text = selectedYear ?: "Select year",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDropDown,
                                                    contentDescription = null
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = yearExpanded,
                                            onDismissRequest = { yearExpanded = false }
                                        ) {
                                            availableYears.forEach { year ->
                                                DropdownMenuItem(
                                                    text = { Text(year) },
                                                    onClick = {
                                                        yearExpanded = false
                                                        selectedYear = year
                                                        selectedMonthKey = null
                                                        isMonthSelectionConfirmed = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { monthExpanded = true },
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Month",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = Color(0xFF6A7280)
                                                    )
                                                    Text(
                                                        text = selectedMonthKey?.let { key ->
                                                            state.availableHistoryMonths
                                                                .firstOrNull { it.key == key }
                                                                ?.label
                                                        } ?: "Select month",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDropDown,
                                                    contentDescription = null
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = monthExpanded,
                                            onDismissRequest = { monthExpanded = false }
                                        ) {
                                            monthsForSelectedYear.forEach { month ->
                                                DropdownMenuItem(
                                                    text = { Text(month.label) },
                                                    onClick = {
                                                        monthExpanded = false
                                                        selectedMonthKey = month.key
                                                        onMonthSelected(month.key)
                                                        isMonthSelectionConfirmed = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!isMonthSelectionConfirmed) {
                                    Text(
                                        text = "Selectează o lună pentru detalii.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6A7280)
                                    )
                                } else if (state.selectedHistoryMonth != null && state.historyMonthSummary == null) {
                                    Text(
                                        text = "Nu există date pentru luna selectată.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6A7280)
                                    )
                                }
                            }
                        }

                        if (isMonthSelectionConfirmed) {
                            state.historyMonthSummary?.let { summary ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = summary.monthLabel,
                                            style = MaterialTheme.typography.titleLarge
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Max temperature", style = MaterialTheme.typography.labelMedium)
                                                Text(summary.maxTemperature, style = MaterialTheme.typography.titleMedium)
                                                Text(
                                                    text = summary.maxTemperatureDate,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF6A7280)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Min temperature", style = MaterialTheme.typography.labelMedium)
                                                Text(summary.minTemperature, style = MaterialTheme.typography.titleMedium)
                                                Text(
                                                    text = summary.minTemperatureDate,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF6A7280)
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Average humidity", style = MaterialTheme.typography.labelMedium)
                                                Text(summary.averageHumidity, style = MaterialTheme.typography.titleMedium)
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Average pressure", style = MaterialTheme.typography.labelMedium)
                                                Text(summary.averagePressure, style = MaterialTheme.typography.titleMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = onOpenSelectedDay,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isMonthSelectionConfirmed && state.selectedHistoryMonth != null
                        ) {
                            Text("Open selected month")
                        }
                    }
                }
            }
        }
    }
}