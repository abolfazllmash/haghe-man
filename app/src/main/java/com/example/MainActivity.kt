package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CalculationType
import com.example.ui.components.BottomNavBar
import com.example.ui.components.ModulePickerSheet
import com.example.ui.components.NavTab
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModuleCalculatorScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.ReferenceScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HaghEManTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
            val constants by viewModel.currentYearConstants.collectAsStateWithLifecycle()
            val historyList by viewModel.historyList.collectAsStateWithLifecycle()
            val profilesList by viewModel.profilesList.collectAsStateWithLifecycle()
            val activeModule by viewModel.selectedModule.collectAsStateWithLifecycle()

            var currentTab by remember { mutableStateOf(NavTab.CALCULATE) }
            var showModulePicker by remember { mutableStateOf(false) }

            // پشته‌ی تاریخچه‌ی تب‌ها برای بازگشت «یک مرحله عقب» با دکمه‌ی بک نیتیو.
            val tabBackStack = remember { mutableStateListOf<NavTab>() }

            // هنگام تغییر تب، تب فعلی در پشته ذخیره می‌شود تا بک بتواند به آن بازگردد.
            fun navigateToTab(target: NavTab) {
                if (target != currentTab) {
                    tabBackStack.add(currentTab)
                    currentTab = target
                }
            }

            val context = LocalContext.current

            HaghEManTheme(darkModeTheme = userPreferences.darkModeTheme) {
                // ۱) داخل یک ماژول محاسبه: بک نیتیو به لیست برمی‌گردد (نه خروج از اپ).
                BackHandler(enabled = activeModule != null) {
                    viewModel.selectModule(null)
                }

                // ۲) اگر تاریخچه‌ی تب داریم: بک نیتیو دقیقاً به تب قبلی می‌رود (یک مرحله عقب).
                BackHandler(enabled = activeModule == null && tabBackStack.isNotEmpty()) {
                    currentTab = tabBackStack.removeAt(tabBackStack.lastIndex)
                }

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (activeModule == null) {
                            BottomNavBar(
                                selectedTab = when (currentTab) {
                                    NavTab.PROFILES, NavTab.SETTINGS -> NavTab.MORE
                                    else -> currentTab
                                },
                                onTabSelected = { tab -> navigateToTab(tab) },
                                onNewCalculation = { showModulePicker = true }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    val mainModifier = Modifier.padding(innerPadding)

                    if (activeModule != null) {
                        ModuleCalculatorScreen(
                            module = activeModule!!,
                            selectedYear = selectedYear,
                            constants = constants,
                            profilesList = profilesList,
                            currencyUnit = userPreferences.currencyUnit,
                            usePersianDigits = userPreferences.usePersianDigits,
                            onBackClick = { viewModel.selectModule(null) },
                            onSaveToHistory = { title, typeName, yr, amount, summary ->
                                viewModel.saveHistoryItem(
                                    title = title,
                                    typeName = typeName,
                                    year = yr,
                                    netAmountRial = amount,
                                    summaryText = summary
                                )
                            },
                            modifier = mainModifier
                        )
                    } else {
                        Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                            when (tab) {
                                NavTab.CALCULATE -> HomeScreen(
                                    selectedYear = selectedYear,
                                    availableYears = viewModel.availableYears,
                                    currentConstants = constants,
                                    profilesList = profilesList,
                                    onYearSelected = { viewModel.selectYear(it) },
                                    onModuleClick = { viewModel.selectModule(it) },
                                    onNavigateToProfiles = { navigateToTab(NavTab.PROFILES) },
                                    onSaveProfile = { viewModel.saveUserProfile(it) },
                                    currencyUnit = userPreferences.currencyUnit,
                                    usePersianDigits = userPreferences.usePersianDigits,
                                    historyList = historyList,
                                    modifier = mainModifier
                                )

                                NavTab.MORE -> MoreScreen(
                                    onNavigateToProfiles = { navigateToTab(NavTab.PROFILES) },
                                    onNavigateToSettings = { navigateToTab(NavTab.SETTINGS) },
                                    onShareApp = {
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "اپلیکیشن «حق من» — ماشین‌حساب حقوق و قانون کار"
                                            )
                                        }
                                        context.startActivity(
                                            Intent.createChooser(share, "اشتراک‌گذاری")
                                        )
                                    },
                                    modifier = mainModifier
                                )

                                NavTab.PROFILES -> ProfilesScreen(
                                    profilesList = profilesList,
                                    selectedYear = selectedYear,
                                    availableYears = viewModel.availableYears,
                                    currentConstants = constants,
                                    currencyUnit = userPreferences.currencyUnit,
                                    usePersianDigits = userPreferences.usePersianDigits,
                                    onSaveProfile = { viewModel.saveUserProfile(it) },
                                    onDeleteProfile = { viewModel.deleteUserProfile(it) },
                                    onSaveToHistory = { title, typeName, yr, amount, summary ->
                                        viewModel.saveHistoryItem(
                                            title = title,
                                            typeName = typeName,
                                            year = yr,
                                            netAmountRial = amount,
                                            summaryText = summary
                                        )
                                    },
                                    modifier = mainModifier
                                )

                                NavTab.HISTORY -> HistoryScreen(
                                    historyList = historyList,
                                    currencyUnit = userPreferences.currencyUnit,
                                    usePersianDigits = userPreferences.usePersianDigits,
                                    onDeleteItem = { viewModel.deleteHistoryItem(it) },
                                    onClearAll = { viewModel.clearAllHistory() },
                                    onExportJson = { viewModel.exportHistoryToJson() },
                                    onImportJson = { viewModel.importHistoryFromJson(it) },
                                    modifier = mainModifier
                                )

                                NavTab.REFERENCE -> ReferenceScreen(
                                    selectedYear = selectedYear,
                                    constants = constants,
                                    currencyUnit = userPreferences.currencyUnit,
                                    usePersianDigits = userPreferences.usePersianDigits,
                                    modifier = mainModifier
                                )

                                NavTab.SETTINGS -> SettingsScreen(
                                    userPreferences = userPreferences,
                                    onCurrencyUnitChange = { viewModel.updateCurrencyUnit(it) },
                                    onUsePersianDigitsChange = { viewModel.updateUsePersianDigits(it) },
                                    onDarkModeThemeChange = { viewModel.updateDarkModeTheme(it) },
                                    modifier = mainModifier
                                )
                            }
                        }
                    }

                    if (showModulePicker) {
                        ModulePickerSheet(
                            modules = CalculationType.values().toList(),
                            usePersianDigits = userPreferences.usePersianDigits,
                            onModuleSelected = { module ->
                                showModulePicker = false
                                viewModel.selectModule(module)
                            },
                            onDismiss = { showModulePicker = false }
                        )
                    }
                }
            }
        }
    }
}
