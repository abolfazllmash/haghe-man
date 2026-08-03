package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShiftWorkRates(
    @Json(name = "morning_evening") val morningEvening: Double = 0.10,
    @Json(name = "morning_evening_night") val morningEveningNight: Double = 0.15,
    @Json(name = "two_shift_with_night") val twoShiftWithNight: Double = 0.225
)

@JsonClass(generateAdapter = true)
data class TaxBracket(
    @Json(name = "from") val from: Long,
    @Json(name = "to") val to: Long,
    @Json(name = "rate") val rate: Double
)

@JsonClass(generateAdapter = true)
data class UnemploymentDurationRange(
    @Json(name = "from") val from: Int,
    @Json(name = "to") val to: Int?,
    @Json(name = "single") val single: Int,
    @Json(name = "married") val married: Int
)

@JsonClass(generateAdapter = true)
data class UnemploymentConstants(
    @Json(name = "benefit_rate") val benefitRate: Double = 0.55,
    @Json(name = "dependent_rate") val dependentRate: Double = 0.10,
    @Json(name = "max_dependents") val maxDependents: Int = 4,
    @Json(name = "cap_rate_of_average") val capRateOfAverage: Double = 0.80,
    @Json(name = "min_insurance_months") val minInsuranceMonths: Int = 6,
    @Json(name = "min_months_if_contract_expiry") val minMonthsIfContractExpiry: Int = 12,
    @Json(name = "duration_table") val durationTable: List<UnemploymentDurationRange> = listOf(
        UnemploymentDurationRange(6, 24, 6, 12),
        UnemploymentDurationRange(25, 120, 12, 18),
        UnemploymentDurationRange(121, 180, 18, 26),
        UnemploymentDurationRange(181, 240, 26, 36),
        UnemploymentDurationRange(241, null, 36, 50)
    )
)

@JsonClass(generateAdapter = true)
data class LaborYearConstants(
    @Json(name = "minimum_daily_wage") val minimumDailyWage: Long,
    @Json(name = "housing_allowance") val housingAllowance: Long,
    @Json(name = "food_allowance") val foodAllowance: Long,
    @Json(name = "marital_allowance") val maritalAllowance: Long = 5000000L,
    @Json(name = "seniority_base_daily") val seniorityBaseDaily: Long = 166667L,
    @Json(name = "child_allowance_multiplier") val childAllowanceMultiplier: Int = 3,
    @Json(name = "overtime_multiplier") val overtimeMultiplier: Double = 1.4,
    @Json(name = "night_shift_rate") val nightShiftRate: Double = 0.35,
    @Json(name = "friday_rate") val fridayRate: Double = 0.40,
    @Json(name = "shift_work") val shiftWork: ShiftWorkRates = ShiftWorkRates(),
    @Json(name = "insurance_employee_rate") val insuranceEmployeeRate: Double = 0.07,
    @Json(name = "insurance_employer_rate") val insuranceEmployerRate: Double = 0.23,
    @Json(name = "daily_work_hours") val dailyWorkHours: Double = 7.33,
    @Json(name = "annual_leave_days") val annualLeaveDays: Int = 26,
    @Json(name = "tax_brackets") val taxBrackets: List<TaxBracket> = emptyList(),
    @Json(name = "unemployment") val unemployment: UnemploymentConstants = UnemploymentConstants(),
    @Json(name = "source_note") val sourceNote: String = ""
)

enum class CurrencyUnit(val title: String, val rialMultiplier: Long) {
    TOMAN("تومان", 10L),
    RIAL("ریال", 1L)
}

enum class CalculationType(
    val title: String,
    val description: String,
    val iconName: String,
    /** Two-to-four word label used on the redesigned home cards. */
    val shortDescription: String,
    /** Rough time-to-complete shown as card metadata. */
    val estimatedSeconds: Int,
    /** Number of inputs the module's form asks for. */
    val inputCount: Int
) {
    PAYSLIP(
        title = "فیش حقوقی کامل",
        description = "محاسبه دقیق ناخالص، کسورات بیمه و مالیات و خالص پرداختی",
        iconName = "receipt_long",
        shortDescription = "محاسبه حقوق ماهانه",
        estimatedSeconds = 30,
        inputCount = 4
    ),
    UNEMPLOYMENT(
        title = "بیمه بیکاری",
        description = "محاسبه شرایط شمول، مبلغ مقرری ماهانه و مدت زمان دریافت",
        iconName = "pension",
        shortDescription = "مقرری و شرایط بیمه بیکاری",
        estimatedSeconds = 90,
        inputCount = 4
    ),
    SEVERANCE(
        title = "سنوات پایان کار",
        description = "حق سنوات پایان خدمت بر اساس سوابق و آخرین مزد",
        iconName = "work_history",
        shortDescription = "محاسبه سنوات و مزایای پایان کار",
        estimatedSeconds = 60,
        inputCount = 2
    ),
    LEAVE_BALANCE(
        title = "مانده مرخصی",
        description = "محاسبه روزهای باقیمانده مرخصی استحقاقی و بازخرید ریالی",
        iconName = "event_busy",
        shortDescription = "مرخصی و بازخرید آن",
        estimatedSeconds = 45,
        inputCount = 3
    ),
    OVERTIME_SHIFTS(
        title = "اضافه‌کاری و نوبت‌کاری",
        description = "محاسبه اضافه‌کاری، شب‌کاری، جمعه‌کاری و فوق‌العاده شیفت",
        iconName = "schedule",
        shortDescription = "اضافه‌کاری، شب‌کاری و شیفت",
        estimatedSeconds = 60,
        inputCount = 4
    ),
    TAX(
        title = "مالیات حقوق",
        description = "محاسبه پلکانی مالیات بر درآمد حقوق سالانه",
        iconName = "account_balance",
        shortDescription = "محاسبه مالیات حقوق",
        estimatedSeconds = 30,
        inputCount = 2
    ),
    INSURANCE(
        title = "بیمه تامین اجتماعی",
        description = "تفکیک سهم ۷٪ کارگر و ۲۳٪ کارفرما",
        iconName = "health_and_safety",
        shortDescription = "سهم بیمه کارگر و کارفرما",
        estimatedSeconds = 30,
        inputCount = 2
    ),
    NET_GROSS_CONVERTER(
        title = "تبدیل خالص به ناخالص",
        description = "محاسبه معکوس حقوق ناخالص مورد نیاز برای خالص دلخواه",
        iconName = "swap_horiz",
        shortDescription = "محاسبه معکوس حقوق",
        estimatedSeconds = 45,
        inputCount = 2
    ),
    BONUS_EIDI(
        title = "عیدی و پاداش",
        description = "محاسبه عیدی سالانه با احتساب سقف قانونی و روزهای کارکرد",
        iconName = "card_giftcard",
        shortDescription = "محاسبه عیدی و پاداش",
        estimatedSeconds = 45,
        inputCount = 3
    )
}

enum class UnemploymentReason(val title: String) {
    CONTRACT_EXPIRY("اتمام قرارداد"),
    DISMISSAL("اخراج"),
    DOWNSIZING("تعدیل نیرو"),
    FORCE_MAJEURE("حوادث غیرمترقبه"),
    RESIGNATION("استعفا")
}

enum class Rule { NORMAL, FLOOR, CAP }

enum class Eligibility {
    ELIGIBLE,
    RESIGNATION,
    INSUFFICIENT_HISTORY,
    CONTRACT_EXPIRY_SHORT,
    QUOTA_EXHAUSTED
}

data class CalculationStep(
    val title: String,
    val value: String,
    val description: String = ""
)

data class UnemploymentInput(
    val averageMonthlyWage: Long,
    val totalInsuranceMonths: Int,
    val reason: UnemploymentReason,
    val lastWorkplaceMonths: Int = 0,
    val previousUsedMonths: Int = 0,
    val isMarried: Boolean = false,
    val dependentsCount: Int = 0
)

data class UnemploymentResult(
    val eligibility: Eligibility,
    val eligibilityMessage: String,
    val solutionHint: String = "",
    val monthlyBenefit: Long,
    val entitledMonths: Int,
    val remainingMonths: Int,
    val totalPayout: Long,
    val appliedRule: Rule,
    val calculationSteps: List<CalculationStep>
)
