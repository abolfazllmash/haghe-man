package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CalculationHistory
import com.example.data.model.CalculationType
import com.example.data.model.CurrencyUnit
import com.example.data.model.LaborYearConstants
import com.example.data.model.UserProfile
import com.example.data.repository.DarkModeTheme
import com.example.data.repository.HistoryRepository
import com.example.data.repository.LaborConstantsRepository
import com.example.data.repository.ProfileRepository
import com.example.data.repository.UserPreferences
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val constantsRepo = LaborConstantsRepository(application)
    private val prefsRepo = UserPreferencesRepository(application)
    private val historyRepo = HistoryRepository(AppDatabase.getDatabase(application).historyDao())
    private val profileRepo = ProfileRepository(AppDatabase.getDatabase(application).userProfileDao())

    val userPreferences: StateFlow<UserPreferences> = prefsRepo.userPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    val profilesList: StateFlow<List<UserProfile>> = profileRepo.allProfiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val availableYears: List<String> = constantsRepo.getAvailableYears()

    private val _selectedYear = MutableStateFlow(availableYears.firstOrNull() ?: "1405")
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()

    private val _currentYearConstants = MutableStateFlow(constantsRepo.getConstantsForYear(_selectedYear.value))
    val currentYearConstants: StateFlow<LaborYearConstants> = _currentYearConstants.asStateFlow()

    val historyList: StateFlow<List<CalculationHistory>> = historyRepo.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedModule = MutableStateFlow<CalculationType?>(null)
    val selectedModule: StateFlow<CalculationType?> = _selectedModule.asStateFlow()

    fun selectYear(year: String) {
        _selectedYear.value = year
        _currentYearConstants.value = constantsRepo.getConstantsForYear(year)
    }

    fun selectModule(module: CalculationType?) {
        _selectedModule.value = module
    }

    fun updateCurrencyUnit(unit: CurrencyUnit) {
        viewModelScope.launch {
            prefsRepo.setCurrencyUnit(unit)
        }
    }

    fun updateUsePersianDigits(usePersian: Boolean) {
        viewModelScope.launch {
            prefsRepo.setUsePersianDigits(usePersian)
        }
    }

    fun updateDarkModeTheme(theme: DarkModeTheme) {
        viewModelScope.launch {
            prefsRepo.setDarkModeTheme(theme)
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            profileRepo.saveProfile(profile)
        }
    }

    fun deleteUserProfile(id: Long) {
        viewModelScope.launch {
            profileRepo.deleteProfile(id)
        }
    }

    fun saveHistoryItem(
        title: String,
        typeName: String,
        year: String,
        netAmountRial: Long,
        summaryText: String,
        jsonInputData: String = "",
        jsonResultData: String = ""
    ) {
        viewModelScope.launch {
            val item = CalculationHistory(
                title = title,
                typeName = typeName,
                year = year,
                netAmountRial = netAmountRial,
                summaryText = summaryText,
                jsonInputData = jsonInputData,
                jsonResultData = jsonResultData
            )
            historyRepo.saveHistory(item)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyRepo.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepo.clearHistory()
        }
    }

    fun exportHistoryToJson(): String {
        val list = historyList.value
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("typeName", item.typeName)
                put("year", item.year)
                put("netAmountRial", item.netAmountRial)
                put("summaryText", item.summaryText)
                put("jsonInputData", item.jsonInputData)
                put("jsonResultData", item.jsonResultData)
                put("createdAt", item.createdAt)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    fun importHistoryFromJson(jsonContent: String): Int {
        var count = 0
        try {
            val jsonArray = JSONArray(jsonContent)
            viewModelScope.launch {
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val item = CalculationHistory(
                        title = obj.optString("title", "محاسبه وارد شده"),
                        typeName = obj.optString("typeName", CalculationType.PAYSLIP.name),
                        year = obj.optString("year", "1405"),
                        netAmountRial = obj.optLong("netAmountRial", 0L),
                        summaryText = obj.optString("summaryText", ""),
                        jsonInputData = obj.optString("jsonInputData", ""),
                        jsonResultData = obj.optString("jsonResultData", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                    historyRepo.saveHistory(item)
                    count++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }
}
