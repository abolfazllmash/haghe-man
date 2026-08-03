package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyUnit
import com.example.data.model.LaborYearConstants
import com.example.data.model.UserProfile
import com.example.data.repository.LaborConstantsRepository
import com.example.domain.calculator.LaborCalculators
import com.example.domain.calculator.PersianNumberFormatter
import com.example.ui.components.CurrencyInputField
import com.example.ui.components.InfoBadgeButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    profilesList: List<UserProfile>,
    selectedYear: String,
    availableYears: List<String>,
    currentConstants: LaborYearConstants,
    currencyUnit: CurrencyUnit,
    usePersianDigits: Boolean,
    onSaveProfile: (UserProfile) -> Unit,
    onDeleteProfile: (Long) -> Unit,
    onSaveToHistory: (title: String, typeName: String, yr: String, amount: Long, summary: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingProfileId by remember { mutableStateOf<Long?>(null) }
    var isFormVisible by remember { mutableStateOf(false) }

    // Form state
    var profileName by remember { mutableStateOf("") }
    var grossSalaryRialText by remember { mutableStateOf("") }
    var yearState by remember { mutableStateOf(selectedYear) }
    var overtimeHoursText by remember { mutableStateOf("0") }
    var remainingLeaveDaysText by remember { mutableStateOf("0") }
    var childrenCountText by remember { mutableStateOf("0") }
    var workedDaysInYearText by remember { mutableStateOf("365") }
    var nightHoursText by remember { mutableStateOf("0") }
    var fridayHoursText by remember { mutableStateOf("0") }

    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    fun resetForm() {
        editingProfileId = null
        profileName = ""
        grossSalaryRialText = ""
        yearState = selectedYear
        overtimeHoursText = "0"
        remainingLeaveDaysText = "0"
        childrenCountText = "0"
        workedDaysInYearText = "365"
        nightHoursText = "0"
        fridayHoursText = "0"
    }

    fun startEditing(p: UserProfile) {
        editingProfileId = p.id
        profileName = p.name
        grossSalaryRialText = p.grossMonthlyWageRial.toString()
        yearState = p.year
        overtimeHoursText = p.overtimeHours.toString()
        remainingLeaveDaysText = p.remainingLeaveDays.toString()
        childrenCountText = p.childrenCount.toString()
        workedDaysInYearText = p.workedDaysInYear.toString()
        nightHoursText = p.nightShiftHours.toString()
        fridayHoursText = p.fridayHours.toString()
        isFormVisible = true
    }

    val parsedGrossWage = PersianNumberFormatter.parseToLong(grossSalaryRialText)
    val parsedOvertime = overtimeHoursText.toDoubleOrNull() ?: 0.0
    val parsedLeaveDays = remainingLeaveDaysText.toDoubleOrNull() ?: 0.0
    val parsedChildren = childrenCountText.toIntOrNull() ?: 0
    val parsedWorkedDays = workedDaysInYearText.toIntOrNull() ?: 365
    val parsedNight = nightHoursText.toDoubleOrNull() ?: 0.0
    val parsedFriday = fridayHoursText.toDoubleOrNull() ?: 0.0

    // Compute live results for form
    val fullResult = if (parsedGrossWage > 0L) {
        LaborCalculators.calculateFullProfile(
            grossMonthlyWage = parsedGrossWage,
            overtimeHours = parsedOvertime,
            remainingLeaveDays = parsedLeaveDays,
            childrenCount = parsedChildren,
            nightShiftHours = parsedNight,
            fridayHours = parsedFriday,
            workedDaysInYear = parsedWorkedDays,
            constants = currentConstants,
            currencyUnit = currencyUnit
        )
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("profiles_screen")
    ) {
        // Banner Card
        var showProfileBannerInfo by remember { mutableStateOf(false) }
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "پروفایل افراد و محاسبات وابسته به حقوق",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            InfoBadgeButton(
                                isExpanded = showProfileBannerInfo,
                                onClick = { showProfileBannerInfo = !showProfileBannerInfo }
                            )
                        }
                        Text(
                            text = "تعیین حقوق ناخالص و محاسبه مانده مرخصی، اضافه‌کاری، عیدی و سنوات",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                AnimatedVisibility(visible = showProfileBannerInfo) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ابتدا حقوق ناخالص (پایه) فرد را مشخص کنید. سپس مواردی نظیر اضافه‌کاری، مانده مرخصی، عیدی و حق سنوات بر اساس حقوق دریافتی او محاسبه خواهند شد.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save notification toast/banner
        if (saveSuccessMessage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = saveSuccessMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Action Bar for Profiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "پروفایل‌های ثبت‌شده (${PersianNumberFormatter.toPersianDigits(profilesList.size.toString(), usePersianDigits)})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    if (isFormVisible && editingProfileId == null) {
                        isFormVisible = false
                    } else {
                        resetForm()
                        isFormVisible = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isFormVisible && editingProfileId == null) Icons.Default.Add else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isFormVisible && editingProfileId == null) "بستن فرم" else "ایجاد پروفایل جدید")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Existing Profiles List
        if (profilesList.isNotEmpty() && !isFormVisible) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                profilesList.forEach { p ->
                    val pResult = LaborCalculators.calculateFullProfile(
                        grossMonthlyWage = p.grossMonthlyWageRial,
                        overtimeHours = p.overtimeHours,
                        remainingLeaveDays = p.remainingLeaveDays,
                        childrenCount = p.childrenCount,
                        nightShiftHours = p.nightShiftHours,
                        fridayHours = p.fridayHours,
                        workedDaysInYear = p.workedDaysInYear,
                        constants = currentConstants,
                        currencyUnit = currencyUnit
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = p.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "سال ${PersianNumberFormatter.toPersianDigits(p.year, usePersianDigits)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = { startEditing(p) }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "ویرایش",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { onDeleteProfile(p.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Key Stats Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "حقوق ناخالص پایه",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = PersianNumberFormatter.formatCurrency(p.grossMonthlyWageRial, currencyUnit, usePersianDigits),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Column {
                                    Text(
                                        text = "مبلغ اضافه کاری",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = PersianNumberFormatter.formatCurrency(pResult.overtimeTotalPay, currencyUnit, usePersianDigits),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column {
                                    Text(
                                        text = "مانده مرخصی",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = PersianNumberFormatter.formatCurrency(pResult.leaveTotalPay, currencyUnit, usePersianDigits),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Net Pay Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "خالص دریافتی ماهانه:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = PersianNumberFormatter.formatCurrency(pResult.netTakeHomePay, currencyUnit, usePersianDigits),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { startEditing(p) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("مشاهده ریز محاسبات کامل و ویرایش")
                            }
                        }
                    }
                }
            }
        }

        // Profile Form (Create / Edit)
        AnimatedVisibility(
            visible = isFormVisible || profilesList.isEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (editingProfileId == null) "تعریف حقوق و پروفایل جدید" else "ویرایش پروفایل ${profileName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("نام و نام خانوادگی فرد") },
                        placeholder = { Text("مثلاً: علی رضایی یا حقوق خودم") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Year Selection
                    var yearExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = !yearExpanded }
                    ) {
                        OutlinedTextField(
                            value = "سال کاری: ${PersianNumberFormatter.toPersianDigits(yearState, usePersianDigits)}",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            availableYears.forEach { yr ->
                                DropdownMenuItem(
                                    text = { Text("سال ${PersianNumberFormatter.toPersianDigits(yr, usePersianDigits)}") },
                                    onClick = {
                                        yearState = yr
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 1: Gross Salary Prompt / Input
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "گام اول: تعیین حقوق ناخالص دریافتی (پایه)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "برای محاسبه حقوق شما، لازم است حقوق دریافتی ناخالص را وارد کنید:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            CurrencyInputField(
                                value = grossSalaryRialText,
                                onValueChange = { grossSalaryRialText = it },
                                label = "حقوق ناخالص ماهانه (پایه)",
                                currencyUnit = currencyUnit,
                                usePersianDigits = usePersianDigits,
                                testTag = "profile_gross_salary_input"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 2: Salary Dependent Items Configuration
                    if (parsedGrossWage > 0L) {
                        Text(
                            text = "گام دوم: تنظیم موارد وابسته به حقوق",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "مقادیر زیر بر اساس حقوق ناخالص بالا محاسبه می‌شوند:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Overtime Hours
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = overtimeHoursText,
                                onValueChange = { overtimeHoursText = PersianNumberFormatter.cleanNumberInput(it) },
                                label = { Text("ساعات اضافه کاری (ماهانه)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_overtime_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (fullResult != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "نرخ ساعتی (۱۴۰٪)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = PersianNumberFormatter.formatCurrency(fullResult.overtimeHourlyRate, currencyUnit, usePersianDigits),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Leave Days
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = remainingLeaveDaysText,
                                onValueChange = { remainingLeaveDaysText = PersianNumberFormatter.cleanNumberInput(it) },
                                label = { Text("روزهای مانده مرخصی (ذخیره)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_leave_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (fullResult != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "ارزش روزانه مرخصی",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = PersianNumberFormatter.formatCurrency(fullResult.leaveDailyRate, currencyUnit, usePersianDigits),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Children & Worked Days
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = childrenCountText,
                                onValueChange = { childrenCountText = PersianNumberFormatter.cleanNumberInput(it) },
                                label = { Text("تعداد فرزندان") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_children_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = workedDaysInYearText,
                                onValueChange = { workedDaysInYearText = PersianNumberFormatter.cleanNumberInput(it) },
                                label = { Text("روز کارکرد (عیدی/سنوات)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_worked_days_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Night & Friday Hours
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = nightHoursText,
                                onValueChange = { nightHoursText = PersianNumberFormatter.cleanNumberInput(it) },
                                label = { Text("ساعات شب‌کاری") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = fridayHoursText,
                                onValueChange = { fridayHoursText = PersianNumberFormatter.cleanNumberInput(it) },
                                label = { Text("ساعات جمعه‌کاری") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Full Calculated Results Preview Card
                        if (fullResult != null) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "خلاصه کل محاسبات حقوق و مزایای فرد",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    ProfileResultRow("حقوق ناخالص پایه:", fullResult.grossMonthlyWage, currencyUnit, usePersianDigits)
                                    ProfileResultRow("مبلغ کل اضافه‌کاری:", fullResult.overtimeTotalPay, currencyUnit, usePersianDigits, isHighlight = true)
                                    ProfileResultRow("بازخرید مانده مرخصی:", fullResult.leaveTotalPay, currencyUnit, usePersianDigits, isHighlight = true)
                                    ProfileResultRow("عیدی سالانه کارکرد:", fullResult.eidiAmount, currencyUnit, usePersianDigits)
                                    ProfileResultRow("حق سنوات سالانه کارکرد:", fullResult.severanceAmount, currencyUnit, usePersianDigits)
                                    ProfileResultRow("جمع حق اولاد، مسکن و بن:", fullResult.childAllowancePay + fullResult.housingAllowance + fullResult.foodAllowance, currencyUnit, usePersianDigits)
                                    ProfileResultRow("کسر بیمه کارگر (۷٪):", -fullResult.employeeInsurance, currencyUnit, usePersianDigits, isDeduction = true)
                                    ProfileResultRow("کسر مالیات حقوق:", -fullResult.taxAmount, currencyUnit, usePersianDigits, isDeduction = true)

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "خالص دریافتی ماهانه:",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = PersianNumberFormatter.formatCurrency(fullResult.netTakeHomePay, currencyUnit, usePersianDigits),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val nameToSave = profileName.ifBlank { "کارمند جدید" }
                                val newProfile = UserProfile(
                                    id = editingProfileId ?: 0L,
                                    name = nameToSave,
                                    grossMonthlyWageRial = parsedGrossWage,
                                    year = yearState,
                                    childrenCount = parsedChildren,
                                    overtimeHours = parsedOvertime,
                                    remainingLeaveDays = parsedLeaveDays,
                                    nightShiftHours = parsedNight,
                                    fridayHours = parsedFriday,
                                    workedDaysInYear = parsedWorkedDays,
                                    updatedAt = System.currentTimeMillis()
                                )

                                onSaveProfile(newProfile)

                                // Also save to history if calculated
                                if (fullResult != null) {
                                    onSaveToHistory(
                                        "پروفایل: $nameToSave",
                                        "پروفایل فرد",
                                        yearState,
                                        fullResult.netTakeHomePay,
                                        fullResult.stepByStep
                                    )
                                }

                                saveSuccessMessage = "پروفایل $nameToSave با موفقیت ذخیره شد."
                                isFormVisible = false
                                resetForm()
                            },
                            enabled = parsedGrossWage > 0L,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ذخیره پروفایل فرد", fontWeight = FontWeight.Bold)
                        }

                        if (isFormVisible && profilesList.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    isFormVisible = false
                                    resetForm()
                                },
                                modifier = Modifier.height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("انصراف")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileResultRow(
    label: String,
    amount: Long,
    currencyUnit: CurrencyUnit,
    usePersianDigits: Boolean,
    isHighlight: Boolean = false,
    isDeduction: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
        )

        val displayVal = if (isDeduction) {
            "- ${PersianNumberFormatter.formatCurrency(-amount, currencyUnit, usePersianDigits)}"
        } else {
            PersianNumberFormatter.formatCurrency(amount, currencyUnit, usePersianDigits)
        }

        Text(
            text = displayVal,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                isDeduction -> MaterialTheme.colorScheme.error
                isHighlight -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    }
}
