package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CalculationHistory
import com.example.data.model.CalculationType
import com.example.data.model.CurrencyUnit
import com.example.data.model.LaborYearConstants
import com.example.data.model.UserProfile
import com.example.ui.components.GreetingHeader
import com.example.ui.components.LastSalaryHeroCard
import com.example.ui.components.MinWageInfoBanner
import com.example.ui.components.ModuleCard
import com.example.ui.components.SalaryEntrySheet
import com.example.ui.components.SectionHeader
import com.example.ui.components.SmartSuggestionRow
import com.example.ui.components.YearPickerSheet

@Composable
fun HomeScreen(
    selectedYear: String,
    availableYears: List<String>,
    currentConstants: LaborYearConstants,
    profilesList: List<UserProfile> = emptyList(),
    onYearSelected: (String) -> Unit,
    onModuleClick: (CalculationType) -> Unit,
    onNavigateToProfiles: () -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    usePersianDigits: Boolean = true,
    historyList: List<CalculationHistory> = emptyList(),
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var showYearSheet by remember { mutableStateOf(false) }
    var showSalarySheet by remember { mutableStateOf(false) }

    val allModules = remember { CalculationType.values().toList() }

    val filteredModules = if (searchQuery.isBlank()) {
        allModules
    } else {
        allModules.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.shortDescription.contains(searchQuery, ignoreCase = true)
        }
    }

    // The profile backing the hero card: this year's profile first, otherwise the newest one.
    val activeProfile = remember(profilesList, selectedYear) {
        profilesList.firstOrNull { it.year == selectedYear && it.grossMonthlyWageRial > 0L }
            ?: profilesList.filter { it.grossMonthlyWageRial > 0L }.maxByOrNull { it.updatedAt }
            ?: profilesList.firstOrNull()
    }

    val popularModule = remember(historyList) { mostUsedModule(historyList) }
    val suggestions = remember(historyList) { buildSuggestions(historyList) }

    val gridRows = filteredModules.chunked(2)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen"),
            contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "greeting") {
                GreetingHeader(
                    userName = activeProfile?.name?.takeIf { it.isNotBlank() && it != "پروفایل من" },
                    onCalendarClick = { showYearSheet = true },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item(key = "hero") {
                LastSalaryHeroCard(
                    amountRial = activeProfile?.grossMonthlyWageRial,
                    updatedAtMillis = activeProfile?.updatedAt,
                    currencyUnit = currencyUnit,
                    usePersianDigits = usePersianDigits,
                    onPrimaryAction = {
                        if ((activeProfile?.grossMonthlyWageRial ?: 0L) > 0L) {
                            onModuleClick(CalculationType.PAYSLIP)
                        } else {
                            showSalarySheet = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item(key = "suggestions_header") {
                SectionHeader(
                    title = "پیشنهادهای هوشمند برای شما",
                    leadingEmoji = "💡",
                    actionLabel = "مشاهده همه",
                    onActionClick = { searchVisible = true },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item(key = "suggestions") {
                SmartSuggestionRow(
                    suggestions = suggestions,
                    onSuggestionClick = onModuleClick
                )
            }

            item(key = "modules_header") {
                SectionHeader(
                    title = "ماشین‌حساب‌ها",
                    actionLabel = "جستجو",
                    actionIcon = Icons.Default.Search,
                    onActionClick = {
                        searchVisible = !searchVisible
                        if (!searchVisible) searchQuery = ""
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item(key = "search") {
                AnimatedVisibility(visible = searchVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("جستجوی ابزار (عیدی، مرخصی، مالیات...)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "پاک کردن"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .testTag("search_module_input")
                    )
                }
            }

            if (filteredModules.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هیچ ابزاری با این مشخصات یافت نشد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(gridRows.size, key = { index -> "row_$index" }) { index ->
                    val row = gridRows[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { module ->
                            ModuleCard(
                                calculationType = module,
                                onClick = { onModuleClick(module) },
                                isPopular = module == popularModule,
                                usePersianDigits = usePersianDigits,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Keep the last odd card at half width.
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item(key = "min_wage") {
                MinWageInfoBanner(
                    year = selectedYear,
                    minimumDailyWageRial = currentConstants.minimumDailyWage,
                    currencyUnit = currencyUnit,
                    usePersianDigits = usePersianDigits,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item(key = "bottom_gap") {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    if (showYearSheet) {
        YearPickerSheet(
            availableYears = availableYears,
            selectedYear = selectedYear,
            usePersianDigits = usePersianDigits,
            onYearSelected = {
                onYearSelected(it)
                showYearSheet = false
            },
            onDismiss = { showYearSheet = false }
        )
    }

    if (showSalarySheet) {
        SalaryEntrySheet(
            initialValue = activeProfile?.grossMonthlyWageRial?.takeIf { it > 0L }?.toString().orEmpty(),
            constants = currentConstants,
            currencyUnit = currencyUnit,
            usePersianDigits = usePersianDigits,
            onSave = { amount ->
                onSaveProfile(
                    (activeProfile ?: UserProfile()).copy(
                        name = activeProfile?.name?.takeIf { it.isNotBlank() } ?: "پروفایل من",
                        grossMonthlyWageRial = amount,
                        year = selectedYear,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                showSalarySheet = false
            },
            onDismiss = { showSalarySheet = false }
        )
    }
}

/** The calculator used most often, shown with a "محبوب‌ترین" chip. */
private fun mostUsedModule(history: List<CalculationHistory>): CalculationType {
    if (history.isEmpty()) return CalculationType.PAYSLIP
    val counts = history.groupingBy { it.typeName }.eachCount()
    val topName = counts.maxByOrNull { it.value }?.key ?: return CalculationType.PAYSLIP
    return CalculationType.values().firstOrNull { it.name == topName } ?: CalculationType.PAYSLIP
}

/** Four suggestions: most-used modules first, then a sensible default order. */
private fun buildSuggestions(history: List<CalculationHistory>): List<CalculationType> {
    val defaults = listOf(
        CalculationType.UNEMPLOYMENT,
        CalculationType.SEVERANCE,
        CalculationType.PAYSLIP,
        CalculationType.LEAVE_BALANCE
    )
    if (history.isEmpty()) return defaults

    val ranked = history.groupingBy { it.typeName }.eachCount()
        .entries
        .sortedByDescending { it.value }
        .mapNotNull { entry -> CalculationType.values().firstOrNull { it.name == entry.key } }

    return (ranked + defaults).distinct().take(4)
}
