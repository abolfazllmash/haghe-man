package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.Alignment
import com.example.data.model.UserProfile
import com.example.ui.components.InfoBadgeButton
import com.example.data.model.CalculationType
import com.example.data.model.CurrencyUnit
import com.example.data.model.LaborYearConstants
import com.example.data.model.UnemploymentInput
import com.example.data.model.UnemploymentReason
import com.example.domain.calculator.JalaliDate
import com.example.domain.calculator.LaborCalculators
import com.example.domain.calculator.PersianNumberFormatter
import com.example.ui.components.CurrencyInputField
import com.example.ui.components.PersianDatePickerField
import com.example.ui.components.ResultCard
import com.example.ui.components.UnemploymentResultCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleCalculatorScreen(
    module: CalculationType,
    selectedYear: String,
    constants: LaborYearConstants,
    profilesList: List<UserProfile> = emptyList(),
    currencyUnit: CurrencyUnit,
    usePersianDigits: Boolean,
    onBackClick: () -> Unit,
    onSaveToHistory: (title: String, typeName: String, year: String, netAmountRial: Long, summaryText: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val activeProfile = remember(profilesList, selectedYear) {
        profilesList.firstOrNull { it.year == selectedYear } ?: profilesList.firstOrNull()
    }
    val defaultWageRial = activeProfile?.grossMonthlyWageRial ?: (constants.minimumDailyWage * 30L)
    val defaultDailyWageRial = activeProfile?.grossMonthlyWageRial?.let { it / 30L } ?: constants.minimumDailyWage

    // State holders for all fields across modules
    var monthlyWageInput by remember(defaultWageRial) { mutableStateOf(defaultWageRial.toString()) }
    var baseWageInput by remember(defaultWageRial) { mutableStateOf(defaultWageRial.toString()) }
    var seniorityPayInput by remember { mutableStateOf("0") }
    var housingAllowanceInput by remember { mutableStateOf(constants.housingAllowance.toString()) }
    var foodAllowanceInput by remember { mutableStateOf(constants.foodAllowance.toString()) }
    var childCount by remember(activeProfile) { mutableIntStateOf(activeProfile?.childrenCount ?: 0) }

    var workedDaysYearInput by remember(activeProfile) { mutableStateOf(activeProfile?.workedDaysInYear?.toString() ?: "365") }

    var dailyWageInput by remember(defaultDailyWageRial) { mutableStateOf(defaultDailyWageRial.toString()) }
    var usedLeaveDaysInput by remember(activeProfile) { mutableStateOf(activeProfile?.remainingLeaveDays?.toString() ?: "0") }
    var remainingLeaveHoursInput by remember { mutableStateOf("0") }

    var overtimeHoursInput by remember(activeProfile) { mutableStateOf(activeProfile?.overtimeHours?.toString() ?: "0") }
    var nightHoursInput by remember(activeProfile) { mutableStateOf(activeProfile?.nightShiftHours?.toString() ?: "0") }
    var fridayHoursInput by remember(activeProfile) { mutableStateOf(activeProfile?.fridayHours?.toString() ?: "0") }
    var shiftTypeRate by remember { mutableDoubleStateOf(0.0) } // 0.0, 0.10, 0.15, 0.225

    var otherBenefitsInput by remember { mutableStateOf("0") }
    var otherDeductionsInput by remember { mutableStateOf("0") }

    var startDate by remember { mutableStateOf(JalaliDate(selectedYear.toIntOrNull() ?: 1405, 1, 1)) }
    var endDate by remember { mutableStateOf(JalaliDate(selectedYear.toIntOrNull() ?: 1405, 12, 29)) }

    var targetNetInput by remember { mutableStateOf((constants.minimumDailyWage * 30).toString()) }

    // Unemployment State
    var unempAverageWageInput by remember(defaultWageRial) { mutableStateOf(defaultWageRial.toString()) }
    var unempTotalMonthsInput by remember { mutableStateOf("24") }
    var unempLastWorkplaceMonthsInput by remember { mutableStateOf("12") }
    var unempPreviousUsedMonthsInput by remember { mutableStateOf("0") }
    var unempReason by remember { mutableStateOf(UnemploymentReason.CONTRACT_EXPIRY) }
    var unempIsMarried by remember { mutableStateOf(false) }
    var unempDependentsCount by remember { mutableIntStateOf(0) }

    // Instant calculation computation
    val baseWageRial = PersianNumberFormatter.parseToLong(baseWageInput)
    val monthlyWageRial = PersianNumberFormatter.parseToLong(monthlyWageInput)
    val seniorityRial = PersianNumberFormatter.parseToLong(seniorityPayInput)
    val housingRial = PersianNumberFormatter.parseToLong(housingAllowanceInput)
    val foodRial = PersianNumberFormatter.parseToLong(foodAllowanceInput)
    val dailyWageRial = PersianNumberFormatter.parseToLong(dailyWageInput)
    val targetNetRial = PersianNumberFormatter.parseToLong(targetNetInput)
    val otherBenefitsRial = PersianNumberFormatter.parseToLong(otherBenefitsInput)
    val otherDeductionsRial = PersianNumberFormatter.parseToLong(otherDeductionsInput)

    val workedDaysYear = workedDaysYearInput.toIntOrNull() ?: 365
    val usedLeaveDays = usedLeaveDaysInput.toDoubleOrNull() ?: 0.0
    val overtimeHours = overtimeHoursInput.toDoubleOrNull() ?: 0.0
    val nightHours = nightHoursInput.toDoubleOrNull() ?: 0.0
    val fridayHours = fridayHoursInput.toDoubleOrNull() ?: 0.0

    val daysBetweenDates = JalaliDate.daysBetween(startDate, endDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "سال $selectedYear",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        },
        modifier = modifier.testTag("module_calculator_screen_${module.name}")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Reference Wage Source Selector Card
            var showReferenceInfo by remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "مبنا و مرجع حقوق:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        InfoBadgeButton(
                            isExpanded = showReferenceInfo,
                            onClick = { showReferenceInfo = !showReferenceInfo }
                        )
                    }

                    AnimatedVisibility(visible = showReferenceInfo) {
                        Text(
                            text = if (profilesList.isNotEmpty()) "حقوق ثبت‌شده شما در دسترس است. می‌توانید مرجع محاسبه را انتخاب کنید:"
                            else "تا پیش از ثبت حقوق در پروفایل، محاسبات بر اساس حداقل حقوق مصوب دولت انجام می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val govWageRial = constants.minimumDailyWage * 30L
                        val isGovSelected = (monthlyWageRial == govWageRial || baseWageRial == govWageRial || dailyWageRial == constants.minimumDailyWage)

                        FilterChip(
                            selected = isGovSelected,
                            onClick = {
                                monthlyWageInput = govWageRial.toString()
                                baseWageInput = govWageRial.toString()
                                dailyWageInput = constants.minimumDailyWage.toString()
                            },
                            label = { Text("حقوق پایه دولتی") },
                            leadingIcon = { Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )

                        profilesList.forEach { p ->
                            val isProfileSelected = (monthlyWageRial == p.grossMonthlyWageRial || baseWageRial == p.grossMonthlyWageRial || dailyWageRial == p.grossMonthlyWageRial / 30L)
                            FilterChip(
                                selected = isProfileSelected,
                                onClick = {
                                    monthlyWageInput = p.grossMonthlyWageRial.toString()
                                    baseWageInput = p.grossMonthlyWageRial.toString()
                                    dailyWageInput = (p.grossMonthlyWageRial / 30L).toString()
                                    childCount = p.childrenCount
                                    if (p.overtimeHours > 0) overtimeHoursInput = p.overtimeHours.toString()
                                    if (p.nightShiftHours > 0) nightHoursInput = p.nightShiftHours.toString()
                                    if (p.fridayHours > 0) fridayHoursInput = p.fridayHours.toString()
                                    if (p.remainingLeaveDays > 0) usedLeaveDaysInput = p.remainingLeaveDays.toString()
                                },
                                label = { Text("پروفایل: ${p.name}") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }

            // Module-specific Input Fields
            when (module) {
                CalculationType.PAYSLIP -> {
                    CurrencyInputField(
                        value = baseWageInput,
                        onValueChange = { baseWageInput = it },
                        label = "مزد پایه ماهانه (۳۰ روز)",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits,
                        presetLabel = "درج حداقل مصوب (${PersianNumberFormatter.formatCurrency(constants.minimumDailyWage * 30, currencyUnit, usePersianDigits)})",
                        onApplyPreset = { baseWageInput = (constants.minimumDailyWage * 30).toString() },
                        testTag = "input_base_wage"
                    )
                    CurrencyInputField(
                        value = seniorityPayInput,
                        onValueChange = { seniorityPayInput = it },
                        label = "پایه سنوات ماهانه",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits,
                        presetLabel = "صفر (برای سال اول)",
                        onApplyPreset = { seniorityPayInput = "0" },
                        testTag = "input_seniority"
                    )
                    CurrencyInputField(
                        value = housingAllowanceInput,
                        onValueChange = { housingAllowanceInput = it },
                        label = "حق مسکن (مصوب هیئت وزیران)",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits,
                        presetLabel = "مصوب (${PersianNumberFormatter.formatCurrency(constants.housingAllowance, currencyUnit, usePersianDigits)})",
                        onApplyPreset = { housingAllowanceInput = constants.housingAllowance.toString() },
                        testTag = "input_housing"
                    )
                    CurrencyInputField(
                        value = foodAllowanceInput,
                        onValueChange = { foodAllowanceInput = it },
                        label = "بن کارگری / خواروبار",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits,
                        presetLabel = "مصوب (${PersianNumberFormatter.formatCurrency(constants.foodAllowance, currencyUnit, usePersianDigits)})",
                        onApplyPreset = { foodAllowanceInput = constants.foodAllowance.toString() },
                        testTag = "input_food"
                    )

                    // Child Count Selector
                    var showChildInfo by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "تعداد فرزندان مشمول حق اولاد:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        InfoBadgeButton(
                            isExpanded = showChildInfo,
                            onClick = { showChildInfo = !showChildInfo }
                        )
                    }
                    AnimatedVisibility(visible = showChildInfo) {
                        Text(
                            text = "به ازای هر فرزند معادل ۳ روز حداقل دستمزد روزانه (${PersianNumberFormatter.formatCurrency(constants.minimumDailyWage * 3, currencyUnit, usePersianDigits)}) حق اولاد پرداخت می‌شود. داشتن ۷۲۰ روز سابقه بیمه الزامی است.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                        )
                    }
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        listOf(0, 1, 2, 3, 4).forEachIndexed { idx, count ->
                            SegmentedButton(
                                selected = childCount == count,
                                onClick = { childCount = count },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = 5)
                            ) {
                                Text(if (usePersianDigits) PersianNumberFormatter.toPersianDigits(count.toString()) else count.toString())
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = overtimeHoursInput,
                            onValueChange = { overtimeHoursInput = it },
                            label = { Text("ساعت اضافه‌کاری") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("input_overtime_hours")
                        )
                        OutlinedTextField(
                            value = nightHoursInput,
                            onValueChange = { nightHoursInput = it },
                            label = { Text("ساعت شب‌کاری") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fridayHoursInput,
                            onValueChange = { fridayHoursInput = it },
                            label = { Text("ساعت جمعه‌کاری") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        // Shift Rate Dropdown
                        var expandedShift by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedShift,
                            onExpandedChange = { expandedShift = it },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = when (shiftTypeRate) {
                                    0.10 -> "صبح-عصر (۱۰٪)"
                                    0.15 -> "صبح-عصر-شب (۱۵٪)"
                                    0.225 -> "۲ نوبت شب (۲۲.۵٪)"
                                    else -> "بدون نوبت‌کاری"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("نوبت‌کاری") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedShift) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedShift,
                                onDismissRequest = { expandedShift = false }
                            ) {
                                DropdownMenuItem(text = { Text("بدون نوبت‌کاری") }, onClick = { shiftTypeRate = 0.0; expandedShift = false })
                                DropdownMenuItem(text = { Text("صبح و عصر (۱۰٪)") }, onClick = { shiftTypeRate = 0.10; expandedShift = false })
                                DropdownMenuItem(text = { Text("صبح و عصر و شب (۱۵٪)") }, onClick = { shiftTypeRate = 0.15; expandedShift = false })
                                DropdownMenuItem(text = { Text("دو نوبت با شب (۲۲.۵٪)") }, onClick = { shiftTypeRate = 0.225; expandedShift = false })
                            }
                        }
                    }

                    CurrencyInputField(
                        value = otherBenefitsInput,
                        onValueChange = { otherBenefitsInput = it },
                        label = { "سایر مزایای مشمول/غیرمشمول" }.invoke(),
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits
                    )
                    CurrencyInputField(
                        value = otherDeductionsInput,
                        onValueChange = { otherDeductionsInput = it },
                        label = { "سایر کسورات (وام، مساعده)" }.invoke(),
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits
                    )
                }

                CalculationType.BONUS_EIDI -> {
                    CurrencyInputField(
                        value = monthlyWageInput,
                        onValueChange = { monthlyWageInput = it },
                        label = "مزد ماهانه پایه (تومان/ریال)",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits,
                        testTag = "input_monthly_wage"
                    )
                    OutlinedTextField(
                        value = workedDaysYearInput,
                        onValueChange = { workedDaysYearInput = it },
                        label = { Text("تعداد روزهای کارکرد در سال (۱ تا ۳۶۶)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("input_worked_days")
                    )
                }

                CalculationType.SEVERANCE -> {
                    CurrencyInputField(
                        value = monthlyWageInput,
                        onValueChange = { monthlyWageInput = it },
                        label = "آخرین مزد ماهانه پایه",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits,
                        testTag = "input_last_monthly_wage"
                    )
                    PersianDatePickerField(
                        label = "تاریخ شروع همکاری",
                        selectedDate = startDate,
                        onDateSelected = { startDate = it },
                        usePersianDigits = usePersianDigits
                    )
                    PersianDatePickerField(
                        label = "تاریخ پایان همکاری",
                        selectedDate = endDate,
                        onDateSelected = { endDate = it },
                        usePersianDigits = usePersianDigits
                    )
                    Text(
                        text = "مدت کارکرد محاسبه شده: ${if (usePersianDigits) PersianNumberFormatter.toPersianDigits(daysBetweenDates.toString()) else daysBetweenDates} روز",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    var showLeaveSettlementInfo by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "مانده مرخصی جهت تسویه:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        InfoBadgeButton(
                            isExpanded = showLeaveSettlementInfo,
                            onClick = { showLeaveSettlementInfo = !showLeaveSettlementInfo }
                        )
                    }

                    AnimatedVisibility(visible = showLeaveSettlementInfo) {
                        Text(
                            text = "در هنگام تسویه حساب پایان خدمت، علاوه بر حق سنوات، مانده مرخصی نیز بر اساس ارزش مزد روزانه و ساعتی فرد محاسبه و بازخرید می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = usedLeaveDaysInput,
                            onValueChange = { usedLeaveDaysInput = it },
                            label = { Text("مانده مرخصی (روز)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("input_remaining_leave_days")
                        )
                        OutlinedTextField(
                            value = remainingLeaveHoursInput,
                            onValueChange = { remainingLeaveHoursInput = it },
                            label = { Text("مانده مرخصی (ساعت)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("input_remaining_leave_hours")
                        )
                    }
                }

                CalculationType.LEAVE_BALANCE -> {
                    CurrencyInputField(
                        value = dailyWageInput,
                        onValueChange = { dailyWageInput = it },
                        label = "مزد روزانه (تومان/ریال)",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits
                    )
                    OutlinedTextField(
                        value = workedDaysYearInput,
                        onValueChange = { workedDaysYearInput = it },
                        label = { Text("تعداد روزهای کارکرد در سال") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = usedLeaveDaysInput,
                        onValueChange = { usedLeaveDaysInput = it },
                        label = { Text("روزهای مرخصی استفاده شده") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                CalculationType.OVERTIME_SHIFTS -> {
                    CurrencyInputField(
                        value = monthlyWageInput,
                        onValueChange = { monthlyWageInput = it },
                        label = "مزد ماهانه پایه",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = overtimeHoursInput,
                            onValueChange = { overtimeHoursInput = it },
                            label = { Text("ساعت اضافه‌کاری") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = nightHoursInput,
                            onValueChange = { nightHoursInput = it },
                            label = { Text("ساعت شب‌کاری") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fridayHoursInput,
                            onValueChange = { fridayHoursInput = it },
                            label = { Text("ساعت جمعه‌کاری") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        var expandedShift by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedShift,
                            onExpandedChange = { expandedShift = it },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = when (shiftTypeRate) {
                                    0.10 -> "صبح-عصر (۱۰٪)"
                                    0.15 -> "صبح-عصر-شب (۱۵٪)"
                                    0.225 -> "۲ نوبت شب (۲۲.۵٪)"
                                    else -> "بدون نوبت‌کاری"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("نوبت‌کاری") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedShift) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedShift,
                                onDismissRequest = { expandedShift = false }
                            ) {
                                DropdownMenuItem(text = { Text("بدون نوبت‌کاری") }, onClick = { shiftTypeRate = 0.0; expandedShift = false })
                                DropdownMenuItem(text = { Text("صبح و عصر (۱۰٪)") }, onClick = { shiftTypeRate = 0.10; expandedShift = false })
                                DropdownMenuItem(text = { Text("صبح و عصر و شب (۱۵٪)") }, onClick = { shiftTypeRate = 0.15; expandedShift = false })
                                DropdownMenuItem(text = { Text("دو نوبت با شب (۲۲.۵٪)") }, onClick = { shiftTypeRate = 0.225; expandedShift = false })
                            }
                        }
                    }
                }

                CalculationType.TAX -> {
                    CurrencyInputField(
                        value = monthlyWageInput,
                        onValueChange = { monthlyWageInput = it },
                        label = "حقوق ناخالص ماهانه مشمول مالیات",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits
                    )
                }

                CalculationType.INSURANCE -> {
                    CurrencyInputField(
                        value = monthlyWageInput,
                        onValueChange = { monthlyWageInput = it },
                        label = "حقوق ناخالص ماهانه مشمول بیمه",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits
                    )
                }

                CalculationType.NET_GROSS_CONVERTER -> {
                    CurrencyInputField(
                        value = targetNetInput,
                        onValueChange = { targetNetInput = it },
                        label = "مبلغ خالص پرداختی مورد نظر",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits
                    )
                    CurrencyInputField(
                        value = housingAllowanceInput,
                        onValueChange = { housingAllowanceInput = it },
                        label = "حق مسکن",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits
                    )
                    CurrencyInputField(
                        value = foodAllowanceInput,
                        onValueChange = { foodAllowanceInput = it },
                        label = "بن کارگری / خواروبار",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits
                    )
                }

                CalculationType.UNEMPLOYMENT -> {
                    Text(
                        text = "۱. اطلاعات مالی و حقوق",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    CurrencyInputField(
                        value = unempAverageWageInput,
                        onValueChange = { unempAverageWageInput = it },
                        label = "میانگین حقوق مشمول بیمه ۹۰ روز آخر (ماهانه)",
                        currencyUnit = currencyUnit,
                        usePersianDigits = usePersianDigits
                    )
                    Text(
                        text = "حق اولاد، عیدی، سنوات و بازخرید مرخصی را حساب نکن.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "۲. سابقه بیمه و علت بیکاری",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    var expandedReason by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedReason,
                        onExpandedChange = { expandedReason = it },
                        modifier = Modifier.fillMaxWidth().testTag("dropdown_unemployment_reason")
                    ) {
                        OutlinedTextField(
                            value = unempReason.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("علت بیکاری") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedReason) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedReason,
                            onDismissRequest = { expandedReason = false }
                        ) {
                            UnemploymentReason.values().forEach { reason ->
                                DropdownMenuItem(
                                    text = { Text(reason.title) },
                                    onClick = {
                                        unempReason = reason
                                        expandedReason = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = unempTotalMonthsInput,
                            onValueChange = { unempTotalMonthsInput = it },
                            label = { Text("سابقه کل بیمه (ماه)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("input_unemp_total_months")
                        )

                        if (unempReason == UnemploymentReason.CONTRACT_EXPIRY) {
                            OutlinedTextField(
                                value = unempLastWorkplaceMonthsInput,
                                onValueChange = { unempLastWorkplaceMonthsInput = it },
                                label = { Text("سابقه آخرین کارگاه (ماه)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_unemp_last_workplace_months")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = unempPreviousUsedMonthsInput,
                        onValueChange = { unempPreviousUsedMonthsInput = it },
                        label = { Text("ماه‌های استفاده‌شده قبلی از بیمه بیکاری") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("input_unemp_prev_used_months")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "۳. وضعیت خانوادگی و تکفل",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !unempIsMarried,
                            onClick = { unempIsMarried = false },
                            label = { Text("مجرد") },
                            modifier = Modifier.weight(1f).testTag("chip_single")
                        )
                        FilterChip(
                            selected = unempIsMarried,
                            onClick = { unempIsMarried = true },
                            label = { Text("متأهل") },
                            modifier = Modifier.weight(1f).testTag("chip_married")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "تعداد افراد تحت تکفل (همسر شاغل جزو تحت تکفل نیست):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("segment_dependents")
                    ) {
                        (0..4).forEach { count ->
                            SegmentedButton(
                                selected = unempDependentsCount == count,
                                onClick = { unempDependentsCount = count },
                                shape = SegmentedButtonDefaults.itemShape(index = count, count = 5)
                            ) {
                                Text(count.toString())
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (module == CalculationType.UNEMPLOYMENT) {
                val averageWageRial = PersianNumberFormatter.parseToLong(unempAverageWageInput)
                val totalMonths = unempTotalMonthsInput.toIntOrNull() ?: 0
                val lastWorkplaceMonths = unempLastWorkplaceMonthsInput.toIntOrNull() ?: 0
                val prevUsedMonths = unempPreviousUsedMonthsInput.toIntOrNull() ?: 0

                val unempResult = LaborCalculators.calculateUnemployment(
                    input = UnemploymentInput(
                        averageMonthlyWage = averageWageRial,
                        totalInsuranceMonths = totalMonths,
                        reason = unempReason,
                        lastWorkplaceMonths = lastWorkplaceMonths,
                        previousUsedMonths = prevUsedMonths,
                        isMarried = unempIsMarried,
                        dependentsCount = unempDependentsCount
                    ),
                    constants = constants,
                    currencyUnit = currencyUnit
                )

                UnemploymentResultCard(
                    result = unempResult,
                    currencyUnit = currencyUnit,
                    usePersianDigits = usePersianDigits
                )
            } else {
                // Compute result card data dynamically based on active module
                val (finalAmountRial, stepByStepFormula) = when (module) {
                CalculationType.PAYSLIP -> {
                    val res = LaborCalculators.calculatePayslip(
                        baseWage = baseWageRial,
                        seniorityPay = seniorityRial,
                        housingAllowance = housingRial,
                        foodAllowance = foodRial,
                        childCount = childCount,
                        overtimeHours = overtimeHours,
                        nightHours = nightHours,
                        fridayHours = fridayHours,
                        shiftTypeRate = shiftTypeRate,
                        otherBenefits = otherBenefitsRial,
                        otherDeductions = otherDeductionsRial,
                        constants = constants,
                        currencyUnit = currencyUnit
                    )
                    Pair(res.netPayable, res.stepByStep)
                }
                CalculationType.BONUS_EIDI -> {
                    val res = LaborCalculators.calculateEidi(
                        monthlyWage = monthlyWageRial,
                        workedDays = workedDaysYear,
                        constants = constants,
                        currencyUnit = currencyUnit
                    )
                    Pair(res.finalEidiAmount, res.stepByStep)
                }
                CalculationType.SEVERANCE -> {
                    val remainingLeaveHours = remainingLeaveHoursInput.toDoubleOrNull() ?: 0.0
                    val res = LaborCalculators.calculateSeverance(
                        lastMonthlyWage = monthlyWageRial,
                        workedDays = daysBetweenDates,
                        remainingLeaveDays = usedLeaveDays,
                        remainingLeaveHours = remainingLeaveHours,
                        constants = constants,
                        currencyUnit = currencyUnit
                    )
                    Pair(res.totalSettlementPay, res.stepByStep)
                }
                CalculationType.LEAVE_BALANCE -> {
                    val res = LaborCalculators.calculateLeaveBalance(
                        dailyWage = dailyWageRial,
                        totalWorkedDays = workedDaysYear,
                        usedLeaveDays = usedLeaveDays,
                        constants = constants,
                        currencyUnit = currencyUnit
                    )
                    Pair(res.cashEquivalent, res.stepByStep)
                }
                CalculationType.OVERTIME_SHIFTS -> {
                    val res = LaborCalculators.calculateOvertimeShifts(
                        monthlyWage = monthlyWageRial,
                        overtimeHours = overtimeHours,
                        nightHours = nightHours,
                        fridayHours = fridayHours,
                        shiftTypeRate = shiftTypeRate,
                        constants = constants,
                        currencyUnit = currencyUnit
                    )
                    Pair(res.totalEarnings, res.stepByStep)
                }
                CalculationType.TAX -> {
                    val res = LaborCalculators.calculateTax(
                        monthlyGrossWage = monthlyWageRial,
                        constants = constants,
                        currencyUnit = currencyUnit
                    )
                    Pair(res.totalTaxAmount, res.stepByStep)
                }
                CalculationType.INSURANCE -> {
                    val res = LaborCalculators.calculateInsurance(
                        insurableWage = monthlyWageRial,
                        constants = constants,
                        currencyUnit = currencyUnit
                    )
                    Pair(res.employeeInsurance, res.stepByStep)
                }
                CalculationType.NET_GROSS_CONVERTER -> {
                    val res = LaborCalculators.calculateNetToGross(
                        targetNet = targetNetRial,
                        housingAllowance = housingRial,
                        foodAllowance = foodRial,
                        childCount = childCount,
                        constants = constants,
                        currencyUnit = currencyUnit
                    )
                    Pair(res.requiredGrossWage, res.stepByStep)
                }
                CalculationType.UNEMPLOYMENT -> Pair(0L, "")
            }

            // Live Sticky Result Card
            ResultCard(
                title = module.title,
                finalAmountRial = finalAmountRial,
                currencyUnit = currencyUnit,
                usePersianDigits = usePersianDigits,
                stepByStepFormula = stepByStepFormula,
                onSaveToHistory = { title ->
                    onSaveToHistory(
                        title,
                        module.name,
                        selectedYear,
                        finalAmountRial,
                        stepByStepFormula
                    )
                }
            )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
