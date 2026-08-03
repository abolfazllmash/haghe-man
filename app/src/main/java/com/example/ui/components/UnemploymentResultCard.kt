package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CurrencyUnit
import com.example.data.model.Eligibility
import com.example.data.model.UnemploymentResult
import com.example.domain.calculator.PersianNumberFormatter

@Composable
fun UnemploymentResultCard(
    result: UnemploymentResult,
    currencyUnit: CurrencyUnit,
    usePersianDigits: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isStepsExpanded by remember { mutableStateOf(false) }

    val isEligible = result.eligibility == Eligibility.ELIGIBLE

    val statusBgColor = if (isEligible) {
        Color(0xFFE8F5E9)
    } else {
        Color(0xFFFFEBEE)
    }

    val statusTextColor = if (isEligible) {
        Color(0xFF2E7D32)
    } else {
        Color(0xFFC62828)
    }

    val statusIcon = if (isEligible) Icons.Default.CheckCircle else Icons.Default.Error

    val formattedMonthly = PersianNumberFormatter.formatCurrency(
        result.monthlyBenefit,
        currencyUnit,
        usePersianDigits
    )
    val monthlyWords = PersianNumberFormatter.currencyToWords(result.monthlyBenefit, currencyUnit)
    val formattedTotal = PersianNumberFormatter.formatCurrency(
        result.totalPayout,
        currencyUnit,
        usePersianDigits
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("unemployment_result_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Status Bar at the top of the Result Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusBgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("unemployment_status_bar")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusTextColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.eligibilityMessage,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                        if (result.solutionHint.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = result.solutionHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = statusTextColor.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            if (isEligible) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "مقرری ماهانه بیمه بیکاری:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedMonthly,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "معادل $monthlyWords",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مدت دریافت:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${result.remainingMonths} ماه  •  مجموع $formattedTotal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("toggle_calculation_steps"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { isStepsExpanded = !isStepsExpanded }) {
                        Text(
                            text = "چطور محاسبه شد؟",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { isStepsExpanded = !isStepsExpanded }) {
                        Icon(
                            imageVector = if (isStepsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "جزییات محاسبه"
                        )
                    }
                }

                AnimatedVisibility(visible = isStepsExpanded) {
                    Column(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        result.calculationSteps.forEach { step ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = step.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = step.value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (step.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = step.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 6.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val shareSummaryText = buildString {
                        append("📌 محاسبه بیمه بیکاری (قانون کار ایران)\n")
                        append("وضعیت: ").append(result.eligibilityMessage).append("\n")
                        append("مقرری ماهانه: ").append(formattedMonthly).append("\n")
                        append("مدت استحقاق: ").append(result.remainingMonths).append(" ماه\n")
                        append("مجموع دریافتی: ").append(formattedTotal).append("\n\n")
                        append("— جزئیات محاسبه:\n")
                        result.calculationSteps.forEach { step ->
                            append(step.title).append(": ").append(step.value).append("\n")
                        }
                        append("\nمحاسبه شده با اپلیکیشن «حق من»")
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Unemployment Calculation", shareSummaryText))
                            Toast.makeText(context, "نتیجه محاسبه کپی شد", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_unemployment")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("کپی نتیجه")
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareSummaryText)
                            }
                            context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری نتیجه بیمه بیکاری"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_share_unemployment")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اشتراک‌گذاری")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "نکات مهم و حقوق قانونی شما:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                AwarenessCard(
                    icon = Icons.Default.VerifiedUser,
                    text = "این مبلغ خالص است. مالیات و بیمه از آن کسر نمی‌شود."
                )
                Spacer(modifier = Modifier.height(6.dp))
                AwarenessCard(
                    icon = Icons.Default.WorkHistory,
                    text = "دوره دریافت، جزو سابقه بیمه بازنشستگی محاسبه می‌شود."
                )
                Spacer(modifier = Modifier.height(6.dp))
                AwarenessCard(
                    icon = Icons.Default.Info,
                    text = "برای ثبت درخواست حداکثر ۳۰ روز از تاریخ بیکاری فرصت دارید."
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "محاسبه بر اساس ماده ۷ قانون بیمه بیکاری. مبلغ نهایی توسط تأمین اجتماعی و بر مبنای لیست‌های بیمه واریزی تعیین می‌شود.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AwarenessCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
