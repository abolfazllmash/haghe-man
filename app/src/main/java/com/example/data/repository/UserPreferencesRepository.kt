package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.CurrencyUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

enum class DarkModeTheme {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserPreferences(
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    val usePersianDigits: Boolean = true,
    val darkModeTheme: DarkModeTheme = DarkModeTheme.SYSTEM
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val CURRENCY_UNIT = stringPreferencesKey("currency_unit")
        val USE_PERSIAN_DIGITS = booleanPreferencesKey("use_persian_digits")
        val DARK_MODE_THEME = stringPreferencesKey("dark_mode_theme")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val currencyStr = prefs[Keys.CURRENCY_UNIT] ?: CurrencyUnit.TOMAN.name
        val currency = try { CurrencyUnit.valueOf(currencyStr) } catch (e: Exception) { CurrencyUnit.TOMAN }

        val usePersian = prefs[Keys.USE_PERSIAN_DIGITS] ?: true

        val themeStr = prefs[Keys.DARK_MODE_THEME] ?: DarkModeTheme.SYSTEM.name
        val theme = try { DarkModeTheme.valueOf(themeStr) } catch (e: Exception) { DarkModeTheme.SYSTEM }

        UserPreferences(
            currencyUnit = currency,
            usePersianDigits = usePersian,
            darkModeTheme = theme
        )
    }

    suspend fun setCurrencyUnit(unit: CurrencyUnit) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENCY_UNIT] = unit.name
        }
    }

    suspend fun setUsePersianDigits(usePersian: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USE_PERSIAN_DIGITS] = usePersian
        }
    }

    suspend fun setDarkModeTheme(theme: DarkModeTheme) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE_THEME] = theme.name
        }
    }
}
