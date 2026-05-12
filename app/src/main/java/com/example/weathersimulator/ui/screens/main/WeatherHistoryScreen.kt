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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import com.example.weathersimulator.data.repository.WeatherRepository
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MenuDefaults
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherHistoryScreen(
    state: WeatherUiState,
    archiveCities: List<WeatherRepository.ArchiveCity>,
    onBackClick: () -> Unit,
    onLoadHistory: () -> Unit,
    onCitySelected: (String) -> Unit,
    onSourceSelected: (String) -> Unit,
    onMonthSelected: (String) -> Unit,
    onOpenSelectedDay: () -> Unit
) {
    var yearExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf<String?>(null) }
    var selectedMonthKey by remember { mutableStateOf<String?>(null) }
    var isMonthSelectionConfirmed by remember { mutableStateOf(state.selectedHistoryMonth != null) }
    var cityExpanded by remember { mutableStateOf(false) }
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

    LaunchedEffect(Unit) {
        if (state.availableHistoryMonths.isEmpty() && !state.isLoading) {
            onLoadHistory()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Istoric meteo", color = Color.White, style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF182A45))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F1D35),
                            Color(0xFF182A45),
                            Color(0xFF243852)
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
                        CircularProgressIndicator(color = Color(0xFFBEE7FF))
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3D2C2C)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                                color = Color(0xFFFF9999)
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3D6FA6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF3D6FA6),
                                                Color(0xFF2C4E73)
                                            )
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Arhivă meteo",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Alege anul, apoi luna, ca să ajungi la ziua dorită din istoricul meteo.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFBEE7FF)
                                    )
                                    Text(
                                        text = "CSV local dacă există, API istoric dacă nu există",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF182A45)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Selectează orașul",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFBEE7FF),
                                    fontWeight = FontWeight.Bold
                                )

                                Box {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { cityExpanded = true },
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF243852))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = state.selectedArchiveCity,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White
                                            )

                                            Icon(
                                                imageVector = Icons.Filled.ArrowDropDown,
                                                contentDescription = null,
                                                tint = Color(0xFFBEE7FF)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = cityExpanded,
                                        onDismissRequest = { cityExpanded = false },
                                        modifier = Modifier
                                            .background(
                                                color = Color(0xFF243852),
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                    ) {

                                        archiveCities.forEach { city ->

                                            val isSelected =
                                                state.selectedArchiveCity == city.name

                                            DropdownMenuItem(
                                                modifier = Modifier.background(
                                                    if (isSelected)
                                                        Color(0xFF3D6FA6)
                                                    else
                                                        Color.Transparent
                                                ),

                                                text = {
                                                    Text(
                                                        text = city.name,
                                                        color =
                                                            if (isSelected)
                                                                Color.White
                                                            else
                                                                Color(0xFFBEE7FF),

                                                        fontWeight =
                                                            if (isSelected)
                                                                FontWeight.Bold
                                                            else
                                                                FontWeight.Medium
                                                    )
                                                },

                                                onClick = {
                                                    cityExpanded = false
                                                    onCitySelected(city.name)
                                                },

                                                colors = MenuDefaults.itemColors(
                                                    textColor = Color(0xFFBEE7FF)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF182A45)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                if (state.selectedArchiveCity == "Timișoara") {

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF182A45)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {

                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {

                                            Text(
                                                text = "Sursa datelor",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color(0xFFBEE7FF),
                                                fontWeight = FontWeight.Bold
                                            )

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {

                                                FilterChip(
                                                    selected = state.selectedArchiveSource == "CSV",
                                                    onClick = { onSourceSelected("CSV") },
                                                    label = {
                                                        Text(
                                                            text = "CSV local",
                                                            color = if (state.selectedArchiveSource == "CSV")
                                                                Color(0xFF10243D)
                                                            else
                                                                Color(0xFFBEE7FF),
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFFEDE7FF),
                                                        containerColor = Color(0xFF243852),
                                                        selectedLabelColor = Color(0xFF10243D),
                                                        labelColor = Color(0xFFBEE7FF)
                                                    ),
                                                    border = FilterChipDefaults.filterChipBorder(
                                                        enabled = true,
                                                        selected = state.selectedArchiveSource == "CSV",
                                                        borderColor = Color(0xFF6FA8DC),
                                                        selectedBorderColor = Color(0xFFEDE7FF),
                                                        borderWidth = 1.dp,
                                                        selectedBorderWidth = 1.dp
                                                    )
                                                )

                                                FilterChip(
                                                    selected = state.selectedArchiveSource == "API",
                                                    onClick = { onSourceSelected("API") },
                                                    label = {
                                                        Text(
                                                            text = "API meteo",
                                                            color = if (state.selectedArchiveSource == "API")
                                                                Color(0xFF10243D)
                                                            else
                                                                Color(0xFFBEE7FF),
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFFEDE7FF),
                                                        containerColor = Color(0xFF243852),
                                                        selectedLabelColor = Color(0xFF10243D),
                                                        labelColor = Color(0xFFBEE7FF)
                                                    ),
                                                    border = FilterChipDefaults.filterChipBorder(
                                                        enabled = true,
                                                        selected = state.selectedArchiveSource == "API",
                                                        borderColor = Color(0xFF6FA8DC),
                                                        selectedBorderColor = Color(0xFFEDE7FF),
                                                        borderWidth = 1.dp,
                                                        selectedBorderWidth = 1.dp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "Selectează perioada",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFBEE7FF),
                                    fontWeight = FontWeight.Bold
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
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF243852))
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
                                                        text = "An",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = Color(0xFF86A7CF)
                                                    )
                                                    Text(
                                                        text = selectedYear ?: "Selectează anul",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = Color.White
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = Color(0xFFBEE7FF)
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = yearExpanded,
                                            onDismissRequest = { yearExpanded = false },
                                            modifier = Modifier
                                                .background(
                                                    color = Color(0xFF243852),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                        ) {
                                            availableYears.forEach { year ->

                                                val isSelected = selectedYear == year

                                                DropdownMenuItem(
                                                    modifier = Modifier.background(
                                                        if (isSelected) Color(0xFF3D6FA6)
                                                        else Color.Transparent
                                                    ),
                                                    text = {
                                                        Text(
                                                            text = year,
                                                            color = if (isSelected) Color.White else Color(0xFFBEE7FF),
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        )
                                                    },
                                                    onClick = {
                                                        yearExpanded = false
                                                        selectedYear = year
                                                        selectedMonthKey = null
                                                        isMonthSelectionConfirmed = false
                                                    },
                                                    colors = MenuDefaults.itemColors(
                                                        textColor = Color(0xFFBEE7FF)
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (selectedYear != null) {
                                                        monthExpanded = true
                                                    }
                                                },
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF243852))
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
                                                        text = "Lună",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = Color(0xFF86A7CF)
                                                    )
                                                    Text(
                                                        text = selectedMonthKey?.let { key ->
                                                            state.availableHistoryMonths
                                                                .firstOrNull { it.key == key }
                                                                ?.label
                                                        } ?: "Selectează luna",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = Color.White
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = Color(0xFFBEE7FF)
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = monthExpanded,
                                            onDismissRequest = { monthExpanded = false },
                                            modifier = Modifier
                                                .background(
                                                    color = Color(0xFF243852),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                        ) {
                                            monthsForSelectedYear.forEach { month ->

                                                val isSelected = selectedMonthKey == month.key

                                                DropdownMenuItem(
                                                    modifier = Modifier.background(
                                                        if (isSelected) Color(0xFF3D6FA6)
                                                        else Color.Transparent
                                                    ),
                                                    text = {
                                                        Text(
                                                            text = month.label,
                                                            color = if (isSelected) Color.White else Color(0xFFBEE7FF),
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        )
                                                    },
                                                    onClick = {
                                                        monthExpanded = false
                                                        selectedMonthKey = month.key
                                                        onMonthSelected(month.key)
                                                        isMonthSelectionConfirmed = true
                                                    },
                                                    colors = MenuDefaults.itemColors(
                                                        textColor = Color(0xFFBEE7FF)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!isMonthSelectionConfirmed) {
                                    Text(
                                        text = "Selectează o lună pentru detalii.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF86A7CF)
                                    )
                                } else if (state.selectedHistoryMonth != null && state.historyMonthSummary == null) {
                                    Text(
                                        text = "Nu există date pentru luna selectată.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF86A7CF)
                                    )
                                }
                            }
                        }

                        if (isMonthSelectionConfirmed) {
                            state.historyMonthSummary?.let { summary ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF243852)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = summary.monthLabel,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color(0xFFBEE7FF),
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Temperatura maximă a lunii", style = MaterialTheme.typography.labelMedium, color = Color(0xFF86A7CF))
                                                Text(summary.maxTemperature, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                                Text(
                                                    text = summary.maxTemperatureDate,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF86A7CF)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Temperatura minimă a lunii", style = MaterialTheme.typography.labelMedium, color = Color(0xFF86A7CF))
                                                Text(summary.minTemperature, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                                Text(
                                                    text = summary.minTemperatureDate,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF86A7CF)
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Umiditatea medie", style = MaterialTheme.typography.labelMedium, color = Color(0xFF86A7CF))
                                                Text(summary.averageHumidity, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Presiunea medie", style = MaterialTheme.typography.labelMedium, color = Color(0xFF86A7CF))
                                                Text(summary.averagePressure, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = onOpenSelectedDay,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isMonthSelectionConfirmed && state.selectedHistoryMonth != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3D6FA6),
                                disabledContainerColor = Color(0xFF5F6F86),

                                contentColor = Color.White,
                                disabledContentColor = Color(0xFFEAF4FF)
                            )
                        ) {
                            Text("Afișează luna selectată")
                        }
                    }
                }
            }
        }
    }
}