package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CurrencyUnit
import com.example.domain.calculator.PersianNumberFormatter

/**
 * "آخرین حقوق ثبت‌شده" hero card. When [amountRial] is zero or null the card
 * switches to an empty state that invites the user to record a salary.
 */
@Composable
fun LastSalaryHeroCard(
    amountRial: Long?,
    updatedAtMillis: Long?,
    currencyUnit: CurrencyUnit,
    usePersianDigits: Boolean,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasSalary = (amountRial ?: 0L) > 0L

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("last_salary_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hasSalary) "آخرین حقوق ثبت‌شده" else "هنوز حقوقی ثبت نکرده‌ای",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (hasSalary) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = PersianNumberFormatter.formatNumber(
                                    amountRial!! / currencyUnit.rialMultiplier,
                                    usePersianDigits
                                ),
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currencyUnit.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "آخرین بروزرسانی: ${relativeUpdatedLabel(updatedAtMillis, usePersianDigits)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    } else {
                        Text(
                            text = "با ثبت حقوق ناخالص، همه محاسبات بر اساس حقوق شخصی تو انجام می‌شود.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Image(
                    painter = painterResource(id = R.drawable.img_wallet_hero),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPrimaryAction,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("hero_primary_action")
            ) {
                Icon(
                    imageVector = if (hasSalary) Icons.Default.Refresh else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasSalary) "محاسبه مجدد" else "ثبت حقوق من",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Relative label for the last update time. Intentionally avoids Gregorian→Jalali
 * conversion so no new date math is introduced.
 */
private fun relativeUpdatedLabel(updatedAtMillis: Long?, usePersianDigits: Boolean): String {
    if (updatedAtMillis == null || updatedAtMillis <= 0L) return "نامشخص"
    val diff = System.currentTimeMillis() - updatedAtMillis
    if (diff < 0L) return "امروز"

    val days = diff / (24L * 60L * 60L * 1000L)
    val raw = when {
        days == 0L -> return "امروز"
        days == 1L -> return "دیروز"
        days < 30L -> "$days روز پیش"
        days < 365L -> "${days / 30L} ماه پیش"
        else -> "${days / 365L} سال پیش"
    }
    return PersianNumberFormatter.toPersianDigits(raw, usePersianDigits)
}
