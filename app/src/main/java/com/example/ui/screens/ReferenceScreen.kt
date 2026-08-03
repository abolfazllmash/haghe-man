package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.InfoBadgeButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyUnit
import com.example.data.model.LaborYearConstants
import com.example.domain.calculator.PersianNumberFormatter

@Composable
fun ReferenceScreen(
    selectedYear: String,
    constants: LaborYearConstants,
    currencyUnit: CurrencyUnit,
    usePersianDigits: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .testTag("reference_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "مرجع قانون کار",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "مرجع و خلاصه قانون کار",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "راهنمای ساده و کاربردی حقوق و مزایای کارگران",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Table of Constants for Selected Year
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var showTableInfo by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ضرایب و مصوبات بخشنامه مزد سال ${if (usePersianDigits) PersianNumberFormatter.toPersianDigits(selectedYear) else selectedYear}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    InfoBadgeButton(
                        isExpanded = showTableInfo,
                        onClick = { showTableInfo = !showTableInfo }
                    )
                }

                AnimatedVisibility(visible = showTableInfo) {
                    Text(
                        text = "این جدول شامل ارقام مصوب وزارت تعاون، کار و رفاه اجتماعی برای سال ${if (usePersianDigits) PersianNumberFormatter.toPersianDigits(selectedYear) else selectedYear} است که به عنوان مبنای حقوق و مزایا استفاده می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                ReferenceTableRow(
                    label = "حداقل مزد روزانه",
                    value = PersianNumberFormatter.formatCurrency(constants.minimumDailyWage, currencyUnit, usePersianDigits)
                )
                ReferenceTableRow(
                    label = "حداقل مزد ماهانه (۳۰ روز)",
                    value = PersianNumberFormatter.formatCurrency(constants.minimumDailyWage * 30, currencyUnit, usePersianDigits)
                )
                ReferenceTableRow(
                    label = "حق مسکن ماهانه",
                    value = PersianNumberFormatter.formatCurrency(constants.housingAllowance, currencyUnit, usePersianDigits)
                )
                ReferenceTableRow(
                    label = "بن کارگری / خواروبار",
                    value = PersianNumberFormatter.formatCurrency(constants.foodAllowance, currencyUnit, usePersianDigits)
                )
                ReferenceTableRow(
                    label = "حق اولاد (هر فرزند)",
                    value = PersianNumberFormatter.formatCurrency(constants.minimumDailyWage * 3, currencyUnit, usePersianDigits)
                )
                ReferenceTableRow(
                    label = "منبع رسمی",
                    value = constants.sourceNote.ifEmpty { "بخشنامه مزد وزارت کار" }
                )
            }
        }

        // Plain Language Articles
        Text(
            text = "مواد قانونی مرتبط در قانون کار",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        LawArticleCard(
            title = "عیدی و پاداش سالانه (قانون نحوه پرداخت عیدی مصوب ۱۳۷۰)",
            description = "کارفرما موظف است به هر کارگر به نسبت ۱ سال کار، معادل ۶۰ روز آخرین مزد (۲ برابر مزد ماهانه) عیدی بپردازد. سقف عیدی نباید از ۹۰ روز حداقل مزد روزانه (۳ برابر حداقل مزد) تجاوز کند. برای کارکرد کمتر از یک سال، عیدی به نسبت روزهای کارکرد محاسبه می‌شود."
        )

        LawArticleCard(
            title = "حق سنوات پایان کار (ماده ۲۴ قانون کار)",
            description = "در صورت خاتمه قرارداد کار (استعفا، بازنشستگی، اخراج یا پایان قرارداد)، کارفرما مکلف است به ازای هر سال سابقه کار، مبالغی معادل یک ماه آخرین حقوق را به عنوان حق سنوات به کارگر بپردازد. برای کسر سال، تناسب روزانه محاسبه می‌شود."
        )

        LawArticleCard(
            title = "مرخصی استحقاقی سالانه (ماده ۶۴ قانون کار)",
            description = "مرخصی استحقاقی سالانه کارگران ۲۶ روز کاری در سال با دریافت مزد کامل است. روزهای جمعه و تعطیل رسمی جزء مرخصی حساب نمی‌شوند. امکان ذخیره حداکثر ۹ روز مرخصی برای سال‌های بعد وجود دارد."
        )

        LawArticleCard(
            title = "فوق‌العاده اضافه‌کاری (ماده ۵۹ قانون کار)",
            description = "کار در ساعات مازاد بر ۷ ساعت و ۲۰ دقیقه در روز (یا ۴۴ ساعت در هفته) اضافه‌کاری محسوب شده و با ۴۰٪ فوق‌العاده بر مزد ساعتی عادی پرداخت می‌شود. انجام اضافه‌کاری موکول به موافقت کارگر است."
        )

        LawArticleCard(
            title = "شب‌کاری و جمعه‌کاری (مواد ۵۸ و ۶۲ قانون کار)",
            description = "برای ساعات کاری غیرنوبت‌کاری که بین ۲۲:۰۰ الی ۰۶:۰۰ انجام شود، ۳۵٪ فوق‌العاده شب‌کاری تعلق می‌گیرد. همچنین کار در روز جمعه با ۴۰٪ فوق‌العاده اضافه بر مزد پرداختی همراه است."
        )

        LawArticleCard(
            title = "حق اولاد و عائله‌مندی (ماده ۸۶ قانون تامین اجتماعی)",
            description = "کمک‌هزینه عائله‌مندی (حق اولاد) برای هر فرزند معادل ۳ برابر حداقل مزد روزانه همان سال است. شرط استحقاق، داشتن حداقل ۷۲۰ روز سابقه پرداخت حق بیمه نزد سازمان تامین اجتماعی است."
        )

        // Legal Disclaimer Box
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("legal_disclaimer_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "سلب مسئولیت",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "«این محاسبه جنبه اطلاع‌رسانی دارد و سند حقوقی نیست. مبنای رسمی، بخشنامه مزد وزارت کار و رأی مراجع حل اختلاف است.»",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ReferenceTableRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
}

@Composable
fun LawArticleCard(title: String, description: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                InfoBadgeButton(
                    isExpanded = isExpanded,
                    onClick = { isExpanded = !isExpanded }
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
