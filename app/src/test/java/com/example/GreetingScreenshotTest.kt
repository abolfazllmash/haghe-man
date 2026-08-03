package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.LaborYearConstants
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.HaghEManTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun home_screen_screenshot() {
        val dummyConstants = LaborYearConstants(
            minimumDailyWage = 3530000L,
            housingAllowance = 9000000L,
            foodAllowance = 21000000L,
            sourceNote = "بخشنامه مزد سال ۱۴۰۵"
        )

        composeTestRule.setContent {
            HaghEManTheme {
                HomeScreen(
                    selectedYear = "1405",
                    availableYears = listOf("1405", "1404", "1403"),
                    currentConstants = dummyConstants,
                    onYearSelected = {},
                    onModuleClick = {},
                    onNavigateToProfiles = {},
                    onSaveProfile = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
