package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyUnit
import com.example.data.model.LaborYearConstants
import com.example.domain.calculator.PersianNumberFormatter

@Composable
fun MinWageInfoBanner(
    selectedYear: String,
    constants: LaborYearConstants,
    currencyUnit: CurrencyUnit,
    usePersianDigits: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val monthlyMinWageRial = constants.minimumDailyWage * 30L
    val displayWage = if (currencyUnit == CurrencyUnit.TOMAN) {
        monthlyMinWageRial / 10L
    } else {
        monthlyMinWageRial
    }

    val formattedWage = PersianNumberFormatter.formatNumber(displayWage, usePersianDigits)
    val formattedYear = if (usePersianDigits) {
        PersianNumberFormatter.toPersianDigits(selectedYear)
    } else {
        selectedYear
    }

    val containerBg = if (!isDark) Color(0xFFEAF5F0) else Color(0xFF132B23)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("info_banner_min_wage"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerBg
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "حداقل حقوق سال $formattedYear: $formattedWage ${currencyUnit.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "آخرین بروزرسانی بر اساس مصوبه شورای عالی کار",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF0E5C48),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
