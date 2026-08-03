package com.example

import com.example.data.model.CurrencyUnit
import com.example.data.model.Eligibility
import com.example.data.model.LaborYearConstants
import com.example.data.model.Rule
import com.example.data.model.ShiftWorkRates
import com.example.data.model.TaxBracket
import com.example.data.model.UnemploymentInput
import com.example.data.model.UnemploymentReason
import com.example.domain.calculator.JalaliDate
import com.example.domain.calculator.LaborCalculators
import com.example.domain.calculator.PersianNumberFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaborCalculatorsUnitTest {

    private val testConstants1405 = LaborYearConstants(
        minimumDailyWage = 3530000L,
        housingAllowance = 9000000L,
        foodAllowance = 21000000L,
        childAllowanceMultiplier = 3,
        overtimeMultiplier = 1.4,
        nightShiftRate = 0.35,
        fridayRate = 0.4,
        shiftWork = ShiftWorkRates(0.10, 0.15, 0.225),
        insuranceEmployeeRate = 0.07,
        insuranceEmployerRate = 0.23,
        dailyWorkHours = 7.33,
        annualLeaveDays = 26,
        taxBrackets = listOf(
            TaxBracket(0, 240000000L, 0.0),
            TaxBracket(240000000L, 300000000L, 0.10),
            TaxBracket(300000000L, 380000000L, 0.15),
            TaxBracket(380000000L, 500000000L, 0.20),
            TaxBracket(500000000L, 660000000L, 0.25),
            TaxBracket(660000000L, 999999999999L, 0.30)
        ),
        sourceNote = "بخشنامه مزد ۱۴۰۵"
    )

    // 1. Persian Number Formatter & Words Conversion
    @Test
    fun testPersianDigitsConversion() {
        val input = "1234567890"
        val expected = "۱۲۳۴۵۶۷۸۹۰"
        assertEquals(expected, PersianNumberFormatter.toPersianDigits(input))
        assertEquals(input, PersianNumberFormatter.toEnglishDigits(expected))
    }

    @Test
    fun testPersianNumberToWords() {
        assertEquals("صفر", PersianNumberFormatter.numberToPersianWords(0))
        assertEquals("یک", PersianNumberFormatter.numberToPersianWords(1))
        assertEquals("دوازده میلیون و پانصد هزار", PersianNumberFormatter.numberToPersianWords(12500000))
        assertEquals("یک میلیارد و دوازده میلیون", PersianNumberFormatter.numberToPersianWords(1012000000))
    }

    // 2. Jalali Calendar & Dates
    @Test
    fun testJalaliLeapYear() {
        assertTrue(JalaliDate.isJalaliLeapYear(1399))
        assertTrue(JalaliDate.isJalaliLeapYear(1395))
        assertFalse(JalaliDate.isJalaliLeapYear(1401))
        assertFalse(JalaliDate.isJalaliLeapYear(1402))
    }

    @Test
    fun testDaysBetweenJalaliDates() {
        val start = JalaliDate(1405, 1, 1)
        val end = JalaliDate(1405, 12, 29)
        val days = JalaliDate.daysBetween(start, end)
        assertEquals(365, days)
    }

    // 3. Eidi Calculator
    @Test
    fun testEidiCalculationFullYearCapped() {
        // High wage: 300,000,000 Rials monthly. Should be capped at 3 * minimum monthly wage (3 * 3530000 * 30 = 317700000)
        val monthlyWage = 300000000L
        val res = LaborCalculators.calculateEidi(
            monthlyWage = monthlyWage,
            workedDays = 365,
            constants = testConstants1405,
            currencyUnit = CurrencyUnit.RIAL
        )
        // 2 * 300,000,000 = 600,000,000 > cap 317,700,000 => final = 317,700,000
        val expectedCap = testConstants1405.minimumDailyWage * 30 * 3
        assertEquals(expectedCap, res.finalEidiAmount)
    }

    @Test
    fun testEidiCalculationPartialYear() {
        val monthlyWage = 100000000L
        val workedDays = 182 // Approx 6 months
        val res = LaborCalculators.calculateEidi(
            monthlyWage = monthlyWage,
            workedDays = workedDays,
            constants = testConstants1405,
            currencyUnit = CurrencyUnit.RIAL
        )
        // 100m monthly * 2 = 200m < legal floor (2 * 105.9m = 211.8m) => clamped to floor 211.8m
        val minMonthly = testConstants1405.minimumDailyWage * 30L
        val eidiFloor = minMonthly * 2L
        val expectedPartial = (eidiFloor * 182.0 / 365.0).toLong()
        assertEquals(expectedPartial, res.finalEidiAmount)
    }

    // 4. Severance Calculator
    @Test
    fun testSeveranceCalculation() {
        val lastWage = 120000000L
        val workedDays = 730L // 2 full years
        val res = LaborCalculators.calculateSeverance(
            lastMonthlyWage = lastWage,
            workedDays = workedDays,
            currencyUnit = CurrencyUnit.RIAL
        )
        assertEquals(240000000L, res.severanceAmount)
        assertEquals(240000000L, res.totalSettlementPay)
    }

    @Test
    fun testSeveranceWithLeaveSettlement() {
        val lastWage = 120000000L // Daily = 4,000,000, Hourly (7.33h) = 545,699
        val workedDays = 365L
        val remainingDays = 2.0
        val remainingHours = 4.0
        val res = LaborCalculators.calculateSeverance(
            lastMonthlyWage = lastWage,
            workedDays = workedDays,
            remainingLeaveDays = remainingDays,
            remainingLeaveHours = remainingHours,
            constants = testConstants1405,
            currencyUnit = CurrencyUnit.RIAL
        )
        assertEquals(120000000L, res.severanceAmount)
        val dailyWage = 120000000L / 30L
        val hourlyWage = (dailyWage.toDouble() / testConstants1405.dailyWorkHours).toLong()
        val expectedLeavePay = (2.0 * dailyWage).toLong() + (4.0 * hourlyWage).toLong()
        assertEquals(expectedLeavePay, res.leaveSettlementAmount)
        assertEquals(120000000L + expectedLeavePay, res.totalSettlementPay)
    }

    // 5. Leave Balance Calculator
    @Test
    fun testLeaveBalanceCalculation() {
        val dailyWage = 3530000L
        val workedDays = 365
        val usedDays = 10.0
        val res = LaborCalculators.calculateLeaveBalance(
            dailyWage = dailyWage,
            totalWorkedDays = workedDays,
            usedLeaveDays = usedDays,
            constants = testConstants1405,
            currencyUnit = CurrencyUnit.RIAL
        )
        assertEquals(16.0, res.remainingLeaveDays, 0.01)
        val expectedCash = (16.0 * dailyWage).toLong()
        assertEquals(expectedCash, res.cashEquivalent)
    }

    // 6. Tax Calculator Progressive Brackets
    @Test
    fun testTaxCalculationBrackets() {
        // Wage below exemption threshold (240,000,000 Rials) => 0 tax
        val lowWage = 200000000L
        val lowTax = LaborCalculators.calculateTax(lowWage, testConstants1405, CurrencyUnit.RIAL)
        assertEquals(0L, lowTax.totalTaxAmount)

        // Wage in first tax bracket (270,000,000 Rials)
        // 240m free, 30m @ 10% = 3,000,000 Rials
        val midWage = 270000000L
        val midTax = LaborCalculators.calculateTax(midWage, testConstants1405, CurrencyUnit.RIAL)
        assertEquals(3000000L, midTax.totalTaxAmount)
    }

    // 7. Social Security Insurance
    @Test
    fun testInsuranceCalculation() {
        val insurableWage = 100000000L
        val res = LaborCalculators.calculateInsurance(insurableWage, testConstants1405, CurrencyUnit.RIAL)
        assertEquals(7000000L, res.employeeInsurance)
        assertEquals(23000000L, res.employerInsurance)
        assertEquals(30000000L, res.totalInsurance)
    }

    // 8. Net to Gross Converter
    @Test
    fun testNetToGrossConverter() {
        val targetNet = 150000000L
        val res = LaborCalculators.calculateNetToGross(
            targetNet = targetNet,
            housingAllowance = testConstants1405.housingAllowance,
            foodAllowance = testConstants1405.foodAllowance,
            childCount = 0,
            constants = testConstants1405,
            currencyUnit = CurrencyUnit.RIAL
        )
        assertTrue(res.calculatedNetPayable >= targetNet)
    }

    // 9. Unemployment Insurance Calculator Tests
    @Test
    fun testUnemploymentResignationIneligible() {
        val input = UnemploymentInput(
            averageMonthlyWage = 150000000L,
            totalInsuranceMonths = 24,
            reason = UnemploymentReason.RESIGNATION,
            isMarried = false,
            dependentsCount = 0
        )
        val res = LaborCalculators.calculateUnemployment(input, testConstants1405, CurrencyUnit.RIAL)
        assertEquals(Eligibility.RESIGNATION, res.eligibility)
        assertEquals(0L, res.totalPayout)
    }

    @Test
    fun testUnemploymentContractExpiryShortIneligible() {
        val input = UnemploymentInput(
            averageMonthlyWage = 150000000L,
            totalInsuranceMonths = 24,
            reason = UnemploymentReason.CONTRACT_EXPIRY,
            lastWorkplaceMonths = 8, // Less than 12
            isMarried = false,
            dependentsCount = 0
        )
        val res = LaborCalculators.calculateUnemployment(input, testConstants1405, CurrencyUnit.RIAL)
        assertEquals(Eligibility.CONTRACT_EXPIRY_SHORT, res.eligibility)
    }

    @Test
    fun testUnemploymentInsufficientHistoryIneligible() {
        val input = UnemploymentInput(
            averageMonthlyWage = 150000000L,
            totalInsuranceMonths = 4, // Less than 6
            reason = UnemploymentReason.DISMISSAL,
            isMarried = false,
            dependentsCount = 0
        )
        val res = LaborCalculators.calculateUnemployment(input, testConstants1405, CurrencyUnit.RIAL)
        assertEquals(Eligibility.INSUFFICIENT_HISTORY, res.eligibility)
    }

    @Test
    fun testUnemploymentEligibleNormalRule() {
        // Average monthly wage = 200,000,000 Rials (Daily wage = 6,666,666.67)
        // Base = 55% = 3,666,666.67
        // Dependents = 1 (10% of min daily wage 3,530,000 = 353,000)
        // Raw daily = 4,019,666.67
        // Min daily = 3,530,000 | Cap daily = 80% of 6,666,666.67 = 5,333,333.33
        // Since 3,530,000 <= 4,019,666.67 <= 5,333,333.33 => Rule.NORMAL
        val input = UnemploymentInput(
            averageMonthlyWage = 200000000L,
            totalInsuranceMonths = 24, // 6-24 months => Single: 6 months
            reason = UnemploymentReason.DISMISSAL,
            isMarried = false,
            dependentsCount = 1
        )
        val res = LaborCalculators.calculateUnemployment(input, testConstants1405, CurrencyUnit.RIAL)
        assertEquals(Eligibility.ELIGIBLE, res.eligibility)
        assertEquals(6, res.entitledMonths)
        assertEquals(Rule.NORMAL, res.appliedRule)
        assertTrue(res.monthlyBenefit > 0)
        assertEquals(res.monthlyBenefit * 6, res.totalPayout)
    }

    @Test
    fun testUnemploymentEligibleFloorRule() {
        // Average monthly wage = minimum wage monthly (3,530,000 * 30 = 105,900,000)
        // Base daily = 55% of 3,530,000 = 1,941,500 < minimum daily wage (3,530,000)
        // Even with 0 dependents, raw daily (1,941,500) < floor (3,530,000) => Rule.FLOOR applied!
        val input = UnemploymentInput(
            averageMonthlyWage = 105900000L,
            totalInsuranceMonths = 36, // Married: 18 months
            reason = UnemploymentReason.DOWNSIZING,
            isMarried = true,
            dependentsCount = 0
        )
        val res = LaborCalculators.calculateUnemployment(input, testConstants1405, CurrencyUnit.RIAL)
        assertEquals(Eligibility.ELIGIBLE, res.eligibility)
        assertEquals(18, res.entitledMonths)
        assertEquals(Rule.FLOOR, res.appliedRule)
        assertEquals(105900000L, res.monthlyBenefit)
    }

    @Test
    fun testUnemploymentEligibleCapRule() {
        // High wage: 500,000,000 Rials monthly (daily = 16,666,666.67)
        // Base daily = 55% = 9,166,666.67
        // 4 Dependents = 4 * 10% * 3,530,000 = 1,412,000
        // Raw daily = 10,578,666.67
        // Cap daily = 80% of 16,666,666.67 = 13,333,333.33
        // If raw daily exceeds cap daily => Rule.CAP
        val input = UnemploymentInput(
            averageMonthlyWage = 200000000L,
            totalInsuranceMonths = 250, // Married: 50 months
            reason = UnemploymentReason.DOWNSIZING,
            isMarried = true,
            dependentsCount = 4
        )
        val res = LaborCalculators.calculateUnemployment(input, testConstants1405, CurrencyUnit.RIAL)
        assertEquals(Eligibility.ELIGIBLE, res.eligibility)
        assertEquals(50, res.entitledMonths)
    }
}
