package com.example.domain.calculator

import com.example.data.model.CalculationStep
import com.example.data.model.CurrencyUnit
import com.example.data.model.Eligibility
import com.example.data.model.LaborYearConstants
import com.example.data.model.Rule
import com.example.data.model.UnemploymentInput
import com.example.data.model.UnemploymentReason
import com.example.data.model.UnemploymentResult
import kotlin.math.max
import kotlin.math.min

object LaborCalculators {

    // 1. Eidi & Bonus
    data class EidiResult(
        val monthlyWage: Long,
        val workedDays: Int,
        val minimumMonthlyWage: Long,
        val eidiFloor: Long,
        val eidiCap: Long,
        val fullYearEidiUncapped: Long,
        val fullYearEidiCapped: Long,
        val finalEidiAmount: Long,
        val stepByStep: String
    )

    fun calculateEidi(
        monthlyWage: Long,
        workedDays: Int,
        constants: LaborYearConstants,
        currencyUnit: CurrencyUnit = CurrencyUnit.RIAL
    ): EidiResult {
        val minMonthly = constants.minimumDailyWage * 30L
        val floor = minMonthly * 2L
        val cap = minMonthly * 3L
        val rawFullEidi = monthlyWage * 2L
        val cappedFullEidi = min(max(rawFullEidi, floor), cap)
        val finalAmount = (cappedFullEidi * workedDays.toDouble() / 365.0).toLong()

        val steps = buildString {
            append("• حداقل مزد ماهانه سال: ").append(PersianNumberFormatter.formatCurrency(minMonthly, currencyUnit)).append("\n")
            append("• فرمول پایه عیدی: ۲ × مزد ماهانه = ").append(PersianNumberFormatter.formatCurrency(rawFullEidi, currencyUnit)).append("\n")
            append("• کف قانونی عیدی (برای ۱ سال کامل): ").append(PersianNumberFormatter.formatCurrency(floor, currencyUnit)).append("\n")
            append("• سقف قانونی عیدی (برای ۱ سال کامل): ").append(PersianNumberFormatter.formatCurrency(cap, currencyUnit)).append("\n")
            append("• عیدی مشمول سقف: ").append(PersianNumberFormatter.formatCurrency(cappedFullEidi, currencyUnit)).append("\n")
            append("• محاسبه تناسب کارکرد (").append(workedDays).append(" روز از ۳۶۵ روز):\n")
            append("  (").append(PersianNumberFormatter.formatCurrency(cappedFullEidi, currencyUnit))
            append(" × ").append(workedDays).append(") ÷ ۳۶۵ = ")
            append(PersianNumberFormatter.formatCurrency(finalAmount, currencyUnit))
        }

        return EidiResult(
            monthlyWage = monthlyWage,
            workedDays = workedDays,
            minimumMonthlyWage = minMonthly,
            eidiFloor = floor,
            eidiCap = cap,
            fullYearEidiUncapped = rawFullEidi,
            fullYearEidiCapped = cappedFullEidi,
            finalEidiAmount = finalAmount,
            stepByStep = steps
        )
    }

    // 2. Severance (حق سنوات و تسویه حساب)
    data class SeveranceResult(
        val lastMonthlyWage: Long,
        val workedDays: Long,
        val severanceAmount: Long,
        val remainingLeaveDays: Double = 0.0,
        val remainingLeaveHours: Double = 0.0,
        val leaveSettlementAmount: Long = 0L,
        val totalSettlementPay: Long = severanceAmount,
        val stepByStep: String
    )

    fun calculateSeverance(
        lastMonthlyWage: Long,
        workedDays: Long,
        remainingLeaveDays: Double = 0.0,
        remainingLeaveHours: Double = 0.0,
        constants: LaborYearConstants = LaborYearConstants(minimumDailyWage = 3000000L, housingAllowance = 9000000L, foodAllowance = 21000000L),
        currencyUnit: CurrencyUnit = CurrencyUnit.RIAL
    ): SeveranceResult {
        val severanceAmount = (lastMonthlyWage * workedDays.toDouble() / 365.0).toLong()

        val dailyWage = max(0L, lastMonthlyWage / 30L)
        val hourlyWage = max(0L, (dailyWage.toDouble() / constants.dailyWorkHours).toLong())

        val leaveDaysAmount = (remainingLeaveDays * dailyWage.toDouble()).toLong()
        val leaveHoursAmount = (remainingLeaveHours * hourlyWage.toDouble()).toLong()
        val leaveSettlementAmount = max(0L, leaveDaysAmount + leaveHoursAmount)

        val totalSettlementPay = severanceAmount + leaveSettlementAmount

        val years = workedDays / 365
        val remDays = workedDays % 365

        val steps = buildString {
            append("• آخرین مزد ماهانه: ").append(PersianNumberFormatter.formatCurrency(lastMonthlyWage, currencyUnit)).append("\n")
            append("• مدت کارکرد: ").append(workedDays).append(" روز (معادل ").append(years).append(" سال و ").append(remDays).append(" روز)\n")
            append("• ۱. حق سنوات: (").append(PersianNumberFormatter.formatCurrency(lastMonthlyWage, currencyUnit))
            append(" × ").append(workedDays).append(") ÷ ۳۶۵ = ")
            append(PersianNumberFormatter.formatCurrency(severanceAmount, currencyUnit)).append("\n")

            if (remainingLeaveDays > 0 || remainingLeaveHours > 0) {
                append("\n• ۲. تسویه مانده مرخصی:\n")
                append("  - مزد روزانه: ").append(PersianNumberFormatter.formatCurrency(dailyWage, currencyUnit))
                append(" | مزد ساعتی: ").append(PersianNumberFormatter.formatCurrency(hourlyWage, currencyUnit)).append("\n")
                if (remainingLeaveDays > 0) {
                    append("  - بازخرید مرخصی روزانه (").append(remainingLeaveDays).append(" روز): ")
                    append(PersianNumberFormatter.formatCurrency(leaveDaysAmount, currencyUnit)).append("\n")
                }
                if (remainingLeaveHours > 0) {
                    append("  - بازخرید مرخصی ساعتی (").append(remainingLeaveHours).append(" ساعت): ")
                    append(PersianNumberFormatter.formatCurrency(leaveHoursAmount, currencyUnit)).append("\n")
                }
                append("  - مجموع تسویه مرخصی: ").append(PersianNumberFormatter.formatCurrency(leaveSettlementAmount, currencyUnit)).append("\n")

                append("\n• جمع کل تسویه حساب (سنوات + مرخصی): ")
                append(PersianNumberFormatter.formatCurrency(severanceAmount, currencyUnit))
                append(" + ").append(PersianNumberFormatter.formatCurrency(leaveSettlementAmount, currencyUnit))
                append(" = ").append(PersianNumberFormatter.formatCurrency(totalSettlementPay, currencyUnit))
            } else {
                append("\n• جمع کل تسویه حساب سنوات: ").append(PersianNumberFormatter.formatCurrency(severanceAmount, currencyUnit))
            }
        }

        return SeveranceResult(
            lastMonthlyWage = lastMonthlyWage,
            workedDays = workedDays,
            severanceAmount = severanceAmount,
            remainingLeaveDays = remainingLeaveDays,
            remainingLeaveHours = remainingLeaveHours,
            leaveSettlementAmount = leaveSettlementAmount,
            totalSettlementPay = totalSettlementPay,
            stepByStep = steps
        )
    }

    // 3. Unused Leave Balance (مانده مرخصی)
    data class LeaveBalanceResult(
        val dailyWage: Long,
        val totalWorkedDays: Int,
        val usedLeaveDays: Double,
        val totalEntitledDays: Double,
        val remainingLeaveDays: Double,
        val cashEquivalent: Long,
        val stepByStep: String
    )

    fun calculateLeaveBalance(
        dailyWage: Long,
        totalWorkedDays: Int,
        usedLeaveDays: Double,
        constants: LaborYearConstants,
        currencyUnit: CurrencyUnit = CurrencyUnit.RIAL
    ): LeaveBalanceResult {
        val entitledDays = (constants.annualLeaveDays.toDouble() * totalWorkedDays.toDouble()) / 365.0
        val remainingDays = max(0.0, entitledDays - usedLeaveDays)
        val cash = (remainingDays * dailyWage.toDouble()).toLong()

        val steps = buildString {
            append("• استحقاق مرخصی کل سال: ").append(constants.annualLeaveDays).append(" روز\n")
            append("• مرخصی استحقاقی بر اساس کارکرد (").append(totalWorkedDays).append(" روز): ")
            append(String.format("%.1f", entitledDays)).append(" روز\n")
            append("• مرخصی استفاده شده: ").append(usedLeaveDays).append(" روز\n")
            append("• مانده مرخصی باقیمانده: ").append(String.format("%.1f", remainingDays)).append(" روز\n")
            append("• فرمول بازخرید: مانده مرخصی (روز) × مزد روزانه\n")
            append("• محاسبه: ").append(String.format("%.1f", remainingDays)).append(" × ")
            append(PersianNumberFormatter.formatCurrency(dailyWage, currencyUnit)).append(" = ")
            append(PersianNumberFormatter.formatCurrency(cash, currencyUnit))
        }

        return LeaveBalanceResult(
            dailyWage = dailyWage,
            totalWorkedDays = totalWorkedDays,
            usedLeaveDays = usedLeaveDays,
            totalEntitledDays = entitledDays,
            remainingLeaveDays = remainingDays,
            cashEquivalent = cash,
            stepByStep = steps
        )
    }

    // 4. Overtime & Shifts
    data class OvertimeShiftResult(
        val monthlyWage: Long,
        val hourlyWage: Long,
        val overtimeHours: Double,
        val overtimeAmount: Long,
        val nightHours: Double,
        val nightAmount: Long,
        val fridayHours: Double,
        val fridayAmount: Long,
        val shiftTypeRate: Double,
        val shiftAmount: Long,
        val totalEarnings: Long,
        val stepByStep: String
    )

    fun calculateOvertimeShifts(
        monthlyWage: Long,
        overtimeHours: Double,
        nightHours: Double,
        fridayHours: Double,
        shiftTypeRate: Double, // 0.0, 0.10, 0.15, 0.225
        constants: LaborYearConstants,
        currencyUnit: CurrencyUnit = CurrencyUnit.RIAL
    ): OvertimeShiftResult {
        val dailyWage = monthlyWage / 30.0
        val hourlyWage = (dailyWage / constants.dailyWorkHours).toLong()

        val overtimeAmount = (overtimeHours * hourlyWage.toDouble() * constants.overtimeMultiplier).toLong()
        val nightAmount = (nightHours * hourlyWage.toDouble() * constants.nightShiftRate).toLong()
        val fridayAmount = (fridayHours * hourlyWage.toDouble() * constants.fridayRate).toLong()
        val shiftAmount = (monthlyWage * shiftTypeRate).toLong()

        val total = overtimeAmount + nightAmount + fridayAmount + shiftAmount

        val steps = buildString {
            append("• مزد ساعتی پایه: (مزد ماهانه ÷ ۳۰) ÷ ").append(constants.dailyWorkHours).append(" = ")
            append(PersianNumberFormatter.formatCurrency(hourlyWage, currencyUnit)).append("\n")
            if (overtimeHours > 0) {
                append("• اضافه‌کاری (ضریب ۱۴۰٪): ").append(overtimeHours).append(" ساعت × ")
                append(PersianNumberFormatter.formatCurrency(hourlyWage, currencyUnit)).append(" × ۱.۴ = ")
                append(PersianNumberFormatter.formatCurrency(overtimeAmount, currencyUnit)).append("\n")
            }
            if (nightHours > 0) {
                append("• شب‌کاری (ضریب ۳۵٪): ").append(nightHours).append(" ساعت × ")
                append(PersianNumberFormatter.formatCurrency(hourlyWage, currencyUnit)).append(" × ۰.۳۵ = ")
                append(PersianNumberFormatter.formatCurrency(nightAmount, currencyUnit)).append("\n")
            }
            if (fridayHours > 0) {
                append("• جمعه‌کاری (ضریب ۴۰٪): ").append(fridayHours).append(" ساعت × ")
                append(PersianNumberFormatter.formatCurrency(hourlyWage, currencyUnit)).append(" × ۰.۴۰ = ")
                append(PersianNumberFormatter.formatCurrency(fridayAmount, currencyUnit)).append("\n")
            }
            if (shiftTypeRate > 0) {
                append("• فوق‌العاده نوبت‌کاری (درصد ").append((shiftTypeRate * 100).toInt()).append("٪): ")
                append(PersianNumberFormatter.formatCurrency(monthlyWage, currencyUnit)).append(" × ")
                append(shiftTypeRate).append(" = ").append(PersianNumberFormatter.formatCurrency(shiftAmount, currencyUnit)).append("\n")
            }
            append("• جمع کل مزایا: ").append(PersianNumberFormatter.formatCurrency(total, currencyUnit))
        }

        return OvertimeShiftResult(
            monthlyWage = monthlyWage,
            hourlyWage = hourlyWage,
            overtimeHours = overtimeHours,
            overtimeAmount = overtimeAmount,
            nightHours = nightHours,
            nightAmount = nightAmount,
            fridayHours = fridayHours,
            fridayAmount = fridayAmount,
            shiftTypeRate = shiftTypeRate,
            shiftAmount = shiftAmount,
            totalEarnings = total,
            stepByStep = steps
        )
    }

    // 5. Income Tax (مالیات حقوق)
    data class TaxResult(
        val monthlyGrossWage: Long,
        val totalTaxAmount: Long,
        val netWageAfterTax: Long,
        val effectiveTaxRatePercent: Double,
        val stepByStep: String
    )

    fun calculateTax(
        monthlyGrossWage: Long,
        constants: LaborYearConstants,
        currencyUnit: CurrencyUnit = CurrencyUnit.RIAL
    ): TaxResult {
        var remainingWage = monthlyGrossWage
        var totalTax = 0L

        val steps = StringBuilder()
        steps.append("• حقوق ناخالص ماهانه: ").append(PersianNumberFormatter.formatCurrency(monthlyGrossWage, currencyUnit)).append("\n")
        steps.append("• محاسبه پلکانی مالیات حقوق:\n")

        for ((index, bracket) in constants.taxBrackets.withIndex()) {
            if (monthlyGrossWage > bracket.from) {
                val bracketMax = min(monthlyGrossWage, bracket.to)
                val taxableInBracket = bracketMax - bracket.from
                val taxInBracket = (taxableInBracket * bracket.rate).toLong()
                totalTax += taxInBracket

                if (bracket.rate == 0.0) {
                    steps.append("  پله ").append(index + 1).append(": تا سقف ")
                        .append(PersianNumberFormatter.formatCurrency(bracket.to, currencyUnit))
                        .append(" معاف از مالیات (۰٪)\n")
                } else {
                    steps.append("  پله ").append(index + 1).append(": مبلغ ")
                        .append(PersianNumberFormatter.formatCurrency(taxableInBracket, currencyUnit))
                        .append(" با نرخ ").append((bracket.rate * 100).toInt()).append("٪ = ")
                        .append(PersianNumberFormatter.formatCurrency(taxInBracket, currencyUnit)).append("\n")
                }
            }
        }

        steps.append("• جمع کل مالیات: ").append(PersianNumberFormatter.formatCurrency(totalTax, currencyUnit))

        val effectiveRate = if (monthlyGrossWage > 0) (totalTax.toDouble() / monthlyGrossWage.toDouble()) * 100.0 else 0.0

        return TaxResult(
            monthlyGrossWage = monthlyGrossWage,
            totalTaxAmount = totalTax,
            netWageAfterTax = monthlyGrossWage - totalTax,
            effectiveTaxRatePercent = effectiveRate,
            stepByStep = steps.toString()
        )
    }

    // 6. Insurance (بیمه تامین اجتماعی)
    data class InsuranceResult(
        val insurableWage: Long,
        val employeeInsurance: Long, // 7%
        val employerInsurance: Long, // 23%
        val totalInsurance: Long,    // 30%
        val stepByStep: String
    )

    fun calculateInsurance(
        insurableWage: Long,
        constants: LaborYearConstants,
        currencyUnit: CurrencyUnit = CurrencyUnit.RIAL
    ): InsuranceResult {
        val emp = (insurableWage * constants.insuranceEmployeeRate).toLong()
        val empEmployer = (insurableWage * constants.insuranceEmployerRate).toLong()
        val total = emp + empEmployer

        val steps = buildString {
            append("• حقوق مشمول بیمه: ").append(PersianNumberFormatter.formatCurrency(insurableWage, currencyUnit)).append("\n")
            append("• سهم بیمه کارگر (۷٪): ").append(PersianNumberFormatter.formatCurrency(emp, currencyUnit)).append("\n")
            append("• سهم بیمه کارفرما (۲۳٪): ").append(PersianNumberFormatter.formatCurrency(empEmployer, currencyUnit)).append("\n")
            append("• مجموع حق بیمه پرداختی به سازمان (۳۰٪): ").append(PersianNumberFormatter.formatCurrency(total, currencyUnit))
        }

        return InsuranceResult(
            insurableWage = insurableWage,
            employeeInsurance = emp,
            employerInsurance = empEmployer,
            totalInsurance = total,
            stepByStep = steps
        )
    }

    // 7. Full Payslip (فیش حقوقی کامل)
    data class PayslipResult(
        val baseWage: Long,
        val seniorityPay: Long,
        val housingAllowance: Long,
        val foodAllowance: Long,
        val childCount: Int,
        val childAllowance: Long,
        val overtimeAmount: Long,
        val nightAmount: Long,
        val fridayAmount: Long,
        val shiftAmount: Long,
        val otherBenefits: Long,
        val grossEarnings: Long,
        val insurableWage: Long,
        val employeeInsurance: Long,
        val employerInsurance: Long,
        val incomeTax: Long,
        val otherDeductions: Long,
        val totalDeductions: Long,
        val netPayable: Long,
        val stepByStep: String
    )

    fun calculatePayslip(
        baseWage: Long,
        seniorityPay: Long,
        housingAllowance: Long,
        foodAllowance: Long,
        childCount: Int,
        overtimeHours: Double,
        nightHours: Double,
        fridayHours: Double,
        shiftTypeRate: Double,
        otherBenefits: Long,
        otherDeductions: Long,
        constants: LaborYearConstants,
        currencyUnit: CurrencyUnit = CurrencyUnit.RIAL
    ): PayslipResult {
        val dailyWage = baseWage / 30.0
        val hourlyWage = (dailyWage / constants.dailyWorkHours).toLong()

        val childAllowance = childCount * constants.childAllowanceMultiplier * constants.minimumDailyWage
        val overtimeAmount = (overtimeHours * hourlyWage.toDouble() * constants.overtimeMultiplier).toLong()
        val nightAmount = (nightHours * hourlyWage.toDouble() * constants.nightShiftRate).toLong()
        val fridayAmount = (fridayHours * hourlyWage.toDouble() * constants.fridayRate).toLong()
        val shiftAmount = (baseWage * shiftTypeRate).toLong()

        val grossEarnings = baseWage + seniorityPay + housingAllowance + foodAllowance +
                childAllowance + overtimeAmount + nightAmount + fridayAmount + shiftAmount + otherBenefits

        // Insurable base includes base, seniority, housing, food, shift, overtime
        val insurableWage = baseWage + seniorityPay + housingAllowance + foodAllowance +
                overtimeAmount + nightAmount + fridayAmount + shiftAmount + otherBenefits

        val employeeInsurance = (insurableWage * constants.insuranceEmployeeRate).toLong()
        val employerInsurance = (insurableWage * constants.insuranceEmployerRate).toLong()

        val taxableWage = max(0L, grossEarnings - employeeInsurance)
        val taxRes = calculateTax(taxableWage, constants, currencyUnit)
        val incomeTax = taxRes.totalTaxAmount

        val totalDeductions = employeeInsurance + incomeTax + otherDeductions
        val netPayable = grossEarnings - totalDeductions

        val steps = buildString {
            append("۱. ناخالص حقوق و مزایا:\n")
            append("  • مزد پایه: ").append(PersianNumberFormatter.formatCurrency(baseWage, currencyUnit)).append("\n")
            if (seniorityPay > 0) append("  • پایه سنوات: ").append(PersianNumberFormatter.formatCurrency(seniorityPay, currencyUnit)).append("\n")
            append("  • حق مسکن: ").append(PersianNumberFormatter.formatCurrency(housingAllowance, currencyUnit)).append("\n")
            append("  • بن کارگری: ").append(PersianNumberFormatter.formatCurrency(foodAllowance, currencyUnit)).append("\n")
            if (childCount > 0) append("  • حق اولاد (").append(childCount).append(" فرزند): ").append(PersianNumberFormatter.formatCurrency(childAllowance, currencyUnit)).append("\n")
            if (overtimeAmount > 0) append("  • اضافه‌کاری: ").append(PersianNumberFormatter.formatCurrency(overtimeAmount, currencyUnit)).append("\n")
            if (nightAmount > 0) append("  • شب‌کاری: ").append(PersianNumberFormatter.formatCurrency(nightAmount, currencyUnit)).append("\n")
            if (fridayAmount > 0) append("  • جمعه‌کاری: ").append(PersianNumberFormatter.formatCurrency(fridayAmount, currencyUnit)).append("\n")
            if (shiftAmount > 0) append("  • نوبت‌کاری: ").append(PersianNumberFormatter.formatCurrency(shiftAmount, currencyUnit)).append("\n")
            if (otherBenefits > 0) append("  • سایر مزایا: ").append(PersianNumberFormatter.formatCurrency(otherBenefits, currencyUnit)).append("\n")
            append("  => جمع ناخالص: ").append(PersianNumberFormatter.formatCurrency(grossEarnings, currencyUnit)).append("\n\n")

            append("۲. کسورات قانونی:\n")
            append("  • بیمه سهم کارگر (۷٪ از مشمول بیمه): ").append(PersianNumberFormatter.formatCurrency(employeeInsurance, currencyUnit)).append("\n")
            append("  • مالیات حقوق (پس از کسر بیمه): ").append(PersianNumberFormatter.formatCurrency(incomeTax, currencyUnit)).append("\n")
            if (otherDeductions > 0) append("  • سایر کسورات: ").append(PersianNumberFormatter.formatCurrency(otherDeductions, currencyUnit)).append("\n")
            append("  => جمع کسورات: ").append(PersianNumberFormatter.formatCurrency(totalDeductions, currencyUnit)).append("\n\n")

            append("۳. خالص پرداختی نهايی:\n")
            append("  ").append(PersianNumberFormatter.formatCurrency(grossEarnings, currencyUnit))
            append(" − ").append(PersianNumberFormatter.formatCurrency(totalDeductions, currencyUnit))
            append(" = ").append(PersianNumberFormatter.formatCurrency(netPayable, currencyUnit))
        }

        return PayslipResult(
            baseWage = baseWage,
            seniorityPay = seniorityPay,
            housingAllowance = housingAllowance,
            foodAllowance = foodAllowance,
            childCount = childCount,
            childAllowance = childAllowance,
            overtimeAmount = overtimeAmount,
            nightAmount = nightAmount,
            fridayAmount = fridayAmount,
            shiftAmount = shiftAmount,
            otherBenefits = otherBenefits,
            grossEarnings = grossEarnings,
            insurableWage = insurableWage,
            employeeInsurance = employeeInsurance,
            employerInsurance = employerInsurance,
            incomeTax = incomeTax,
            otherDeductions = otherDeductions,
            totalDeductions = totalDeductions,
            netPayable = netPayable,
            stepByStep = steps
        )
    }

    // 8. Net to Gross Converter (تبدیل خالص به ناخالص)
    data class NetToGrossResult(
        val targetNetPayable: Long,
        val requiredGrossWage: Long,
        val estimatedEmployeeInsurance: Long,
        val estimatedIncomeTax: Long,
        val calculatedNetPayable: Long,
        val stepByStep: String
    )

    fun calculateNetToGross(
        targetNet: Long,
        housingAllowance: Long,
        foodAllowance: Long,
        childCount: Int,
        constants: LaborYearConstants,
        currencyUnit: CurrencyUnit = CurrencyUnit.RIAL
    ): NetToGrossResult {
        // Binary search to find gross salary that produces target net
        val safeTarget = max(0L, targetNet)
        var low = safeTarget
        val safeMax = Long.MAX_VALUE / 3L
        var high = if (safeTarget > safeMax) Long.MAX_VALUE else safeTarget * 3L
        var bestGross = safeTarget

        for (i in 0..60) {
            val mid = (low + high) / 2
            val slip = calculatePayslip(
                baseWage = mid,
                seniorityPay = 0L,
                housingAllowance = housingAllowance,
                foodAllowance = foodAllowance,
                childCount = childCount,
                overtimeHours = 0.0,
                nightHours = 0.0,
                fridayHours = 0.0,
                shiftTypeRate = 0.0,
                otherBenefits = 0L,
                otherDeductions = 0L,
                constants = constants,
                currencyUnit = currencyUnit
            )

            if (slip.netPayable >= targetNet) {
                bestGross = mid
                high = mid - 1
            } else {
                low = mid + 1
            }
        }

        val finalSlip = calculatePayslip(
            baseWage = bestGross,
            seniorityPay = 0L,
            housingAllowance = housingAllowance,
            foodAllowance = foodAllowance,
            childCount = childCount,
            overtimeHours = 0.0,
            nightHours = 0.0,
            fridayHours = 0.0,
            shiftTypeRate = 0.0,
            otherBenefits = 0L,
            otherDeductions = 0L,
            constants = constants,
            currencyUnit = currencyUnit
        )

        val steps = buildString {
            append("• خالص پرداختی مورد نظر: ").append(PersianNumberFormatter.formatCurrency(targetNet, currencyUnit)).append("\n")
            append("• ناخالص حقوق پیشنهادی (مزد پایه): ").append(PersianNumberFormatter.formatCurrency(bestGross, currencyUnit)).append("\n")
            append("• جمع ناخالص دریافتی با مزایا: ").append(PersianNumberFormatter.formatCurrency(finalSlip.grossEarnings, currencyUnit)).append("\n")
            append("• کسر حق بیمه سهم کارگر (۷٪): ").append(PersianNumberFormatter.formatCurrency(finalSlip.employeeInsurance, currencyUnit)).append("\n")
            append("• کسر مالیات حقوق: ").append(PersianNumberFormatter.formatCurrency(finalSlip.incomeTax, currencyUnit)).append("\n")
            append("• خالص پرداختی حاصل شده: ").append(PersianNumberFormatter.formatCurrency(finalSlip.netPayable, currencyUnit))
        }

        return NetToGrossResult(
            targetNetPayable = targetNet,
            requiredGrossWage = bestGross,
            estimatedEmployeeInsurance = finalSlip.employeeInsurance,
            estimatedIncomeTax = finalSlip.incomeTax,
            calculatedNetPayable = finalSlip.netPayable,
            stepByStep = steps
        )
    }

    // 9. Full Profile Calculations
    data class FullProfileResult(
        val grossMonthlyWage: Long,
        val dailyWage: Long,
        val hourlyWage: Long,
        val overtimeHourlyRate: Long,
        val overtimeTotalPay: Long,
        val leaveDailyRate: Long,
        val leaveTotalPay: Long,
        val eidiAmount: Long,
        val severanceAmount: Long,
        val nightShiftPay: Long,
        val fridayWorkPay: Long,
        val childAllowancePay: Long,
        val housingAllowance: Long,
        val foodAllowance: Long,
        val employeeInsurance: Long,
        val employerInsurance: Long,
        val taxAmount: Long,
        val totalGrossEarnings: Long,
        val netTakeHomePay: Long,
        val stepByStep: String
    )

    fun calculateFullProfile(
        grossMonthlyWage: Long,
        overtimeHours: Double,
        remainingLeaveDays: Double,
        childrenCount: Int,
        nightShiftHours: Double = 0.0,
        fridayHours: Double = 0.0,
        workedDaysInYear: Int = 365,
        constants: LaborYearConstants,
        currencyUnit: CurrencyUnit = CurrencyUnit.RIAL
    ): FullProfileResult {
        val daily = max(0L, grossMonthlyWage / 30L)
        val hourly = max(0L, (daily / constants.dailyWorkHours).toLong())

        val overtimeRate = (hourly * constants.overtimeMultiplier).toLong()
        val overtimeTotal = (overtimeHours * overtimeRate.toDouble()).toLong()

        val leaveDaily = daily
        val leaveTotal = (remainingLeaveDays * leaveDaily.toDouble()).toLong()

        val eidiRes = calculateEidi(grossMonthlyWage, workedDaysInYear, constants, currencyUnit)
        val severanceRes = calculateSeverance(
            lastMonthlyWage = grossMonthlyWage,
            workedDays = workedDaysInYear.toLong(),
            remainingLeaveDays = remainingLeaveDays,
            remainingLeaveHours = 0.0,
            constants = constants,
            currencyUnit = currencyUnit
        )

        val nightPay = (nightShiftHours * hourly.toDouble() * constants.nightShiftRate).toLong()
        val fridayPay = (fridayHours * hourly.toDouble() * constants.fridayRate).toLong()

        val childAllowancePay = childrenCount * constants.childAllowanceMultiplier * constants.minimumDailyWage
        val housing = constants.housingAllowance
        val food = constants.foodAllowance

        val insRes = calculateInsurance(grossMonthlyWage, constants, currencyUnit)
        val taxRes = calculateTax(grossMonthlyWage, constants, currencyUnit)

        val totalGross = grossMonthlyWage + overtimeTotal + leaveTotal + nightPay + fridayPay
        val net = totalGross + childAllowancePay + housing + food - insRes.employeeInsurance - taxRes.totalTaxAmount

        val steps = buildString {
            append("• حقوق ناخالص پایه ماهانه: ").append(PersianNumberFormatter.formatCurrency(grossMonthlyWage, currencyUnit)).append("\n")
            append("• پایه روزانه: ").append(PersianNumberFormatter.formatCurrency(daily, currencyUnit))
            append(" | پایه ساعتی: ").append(PersianNumberFormatter.formatCurrency(hourly, currencyUnit)).append("\n\n")

            append("• اضافه‌کاری (").append(overtimeHours).append(" ساعت با نرخ ۱۴۰٪ ساعتی ").append(PersianNumberFormatter.formatCurrency(overtimeRate, currencyUnit)).append("): ")
            append(PersianNumberFormatter.formatCurrency(overtimeTotal, currencyUnit)).append("\n")

            append("• مانده مرخصی (").append(remainingLeaveDays).append(" روز با روزانه ").append(PersianNumberFormatter.formatCurrency(leaveDaily, currencyUnit)).append("): ")
            append(PersianNumberFormatter.formatCurrency(leaveTotal, currencyUnit)).append("\n")

            append("• عیدی سالانه کارکرد (").append(workedDaysInYear).append(" روز): ").append(PersianNumberFormatter.formatCurrency(eidiRes.finalEidiAmount, currencyUnit)).append("\n")
            append("• حق سنوات سالانه کارکرد: ").append(PersianNumberFormatter.formatCurrency(severanceRes.severanceAmount, currencyUnit)).append("\n")

            if (childAllowancePay > 0) {
                append("• حق اولاد (").append(childrenCount).append(" فرزند): ").append(PersianNumberFormatter.formatCurrency(childAllowancePay, currencyUnit)).append("\n")
            }
            append("• حق مسکن: ").append(PersianNumberFormatter.formatCurrency(housing, currencyUnit)).append(" | بن کارگری: ").append(PersianNumberFormatter.formatCurrency(food, currencyUnit)).append("\n")

            append("• بیمه سهم کارگر (۷٪): ").append(PersianNumberFormatter.formatCurrency(insRes.employeeInsurance, currencyUnit)).append("\n")
            append("• مالیات حقوق: ").append(PersianNumberFormatter.formatCurrency(taxRes.totalTaxAmount, currencyUnit)).append("\n\n")

            append("• خالص دریافتی نهایی ماهانه: ").append(PersianNumberFormatter.formatCurrency(net, currencyUnit))
        }

        return FullProfileResult(
            grossMonthlyWage = grossMonthlyWage,
            dailyWage = daily,
            hourlyWage = hourly,
            overtimeHourlyRate = overtimeRate,
            overtimeTotalPay = overtimeTotal,
            leaveDailyRate = leaveDaily,
            leaveTotalPay = leaveTotal,
            eidiAmount = eidiRes.finalEidiAmount,
            severanceAmount = severanceRes.severanceAmount,
            nightShiftPay = nightPay,
            fridayWorkPay = fridayPay,
            childAllowancePay = childAllowancePay,
            housingAllowance = housing,
            foodAllowance = food,
            employeeInsurance = insRes.employeeInsurance,
            employerInsurance = insRes.employerInsurance,
            taxAmount = taxRes.totalTaxAmount,
            totalGrossEarnings = totalGross,
            netTakeHomePay = net,
            stepByStep = steps
        )
    }

    // 10. Unemployment Insurance Calculator (بیمه بیکاری)
    fun calculateUnemployment(
        input: UnemploymentInput,
        constants: LaborYearConstants,
        currencyUnit: CurrencyUnit = CurrencyUnit.RIAL
    ): UnemploymentResult {
        val unempConsts = constants.unemployment
        val reason = input.reason
        val totalMonths = input.totalInsuranceMonths
        val lastWorkplaceMonths = input.lastWorkplaceMonths
        val previousUsedMonths = input.previousUsedMonths
        val isMarried = input.isMarried
        val dependents = minOf(maxOf(0, input.dependentsCount), unempConsts.maxDependents)

        // Effective history for table lookup
        val effectiveHistoryForTable = if (reason == UnemploymentReason.FORCE_MAJEURE && totalMonths < unempConsts.minInsuranceMonths) {
            unempConsts.minInsuranceMonths
        } else {
            totalMonths
        }

        // Find duration range from Article 7 table
        val matchedRange = unempConsts.durationTable.find { range ->
            val fromOk = effectiveHistoryForTable >= range.from
            val toOk = range.to == null || effectiveHistoryForTable <= range.to
            fromOk && toOk
        }

        val entitledMonths = matchedRange?.let { if (isMarried) it.married else it.single } ?: 0
        val remainingMonths = maxOf(0, entitledMonths - previousUsedMonths)

        // Eligibility Checks (Strict Order)
        val (eligibility, eligibilityMessage, solutionHint) = when {
            reason == UnemploymentReason.RESIGNATION -> {
                Triple(
                    Eligibility.RESIGNATION,
                    "استعفا مشمول بیمه بیکاری نیست.",
                    "طبق قانون بیمه بیکاری، ترک کار داوطلبانه یا استعفا مشمول دریافت مقرری نمی‌شود."
                )
            }
            reason == UnemploymentReason.CONTRACT_EXPIRY && lastWorkplaceMonths < unempConsts.minMonthsIfContractExpiry -> {
                val needed = unempConsts.minMonthsIfContractExpiry - lastWorkplaceMonths
                Triple(
                    Eligibility.CONTRACT_EXPIRY_SHORT,
                    "حداقل ${unempConsts.minMonthsIfContractExpiry} ماه سابقه در آخرین کارگاه برای اتمام قرارداد لازم است.",
                    "سابقه شما در آخرین کارگاه $lastWorkplaceMonths ماه است ($needed ماه دیگر لازم است)."
                )
            }
            reason != UnemploymentReason.FORCE_MAJEURE && totalMonths < unempConsts.minInsuranceMonths -> {
                val needed = unempConsts.minInsuranceMonths - totalMonths
                Triple(
                    Eligibility.INSUFFICIENT_HISTORY,
                    "حداقل ${unempConsts.minInsuranceMonths} ماه سابقه کل پرداخت حق بیمه لازم است.",
                    "سابقه کل شما $totalMonths ماه است ($needed ماه دیگر سابقه بیمه لازم دارید)."
                )
            }
            remainingMonths <= 0 -> {
                Triple(
                    Eligibility.QUOTA_EXHAUSTED,
                    "سقف تجمعی دریافت بیمه بیکاری به پایان رسیده است.",
                    "شما قبلاً $previousUsedMonths ماه استفاده کرده‌اید و سقف استحقاق سابقه شما $entitledMonths ماه بوده است."
                )
            }
            else -> {
                Triple(
                    Eligibility.ELIGIBLE,
                    "واجد شرایط دریافت بیمه بیکاری هستید",
                    ""
                )
            }
        }

        // Formula Calculation
        val averageDailyWage = input.averageMonthlyWage / 30.0
        val baseDaily = unempConsts.benefitRate * averageDailyWage
        val dependencyDaily = unempConsts.dependentRate * constants.minimumDailyWage.toDouble() * dependents
        val rawDaily = baseDaily + dependencyDaily
        val floorDaily = constants.minimumDailyWage.toDouble()
        val capDaily = unempConsts.capRateOfAverage * averageDailyWage

        // CRITICAL ORDER: Floor has priority over Cap
        val (dailyBenefitDouble, appliedRule) = when {
            rawDaily < floorDaily -> Pair(floorDaily, Rule.FLOOR)
            rawDaily > capDaily -> Pair(capDaily, Rule.CAP)
            else -> Pair(rawDaily, Rule.NORMAL)
        }

        val monthlyBenefit = (dailyBenefitDouble * 30.0).toLong()
        val totalPayout = if (eligibility == Eligibility.ELIGIBLE) monthlyBenefit * remainingMonths else 0L

        val steps = buildUnemploymentCalculationSteps(
            input = input,
            constants = constants,
            averageDailyWage = averageDailyWage,
            baseDaily = baseDaily,
            dependencyDaily = dependencyDaily,
            rawDaily = rawDaily,
            floorDaily = floorDaily,
            capDaily = capDaily,
            dailyBenefitDouble = dailyBenefitDouble,
            appliedRule = appliedRule,
            monthlyBenefit = monthlyBenefit,
            entitledMonths = entitledMonths,
            remainingMonths = remainingMonths,
            totalPayout = totalPayout,
            currencyUnit = currencyUnit
        )

        return UnemploymentResult(
            eligibility = eligibility,
            eligibilityMessage = eligibilityMessage,
            solutionHint = solutionHint,
            monthlyBenefit = monthlyBenefit,
            entitledMonths = entitledMonths,
            remainingMonths = remainingMonths,
            totalPayout = totalPayout,
            appliedRule = appliedRule,
            calculationSteps = steps
        )
    }

    fun buildUnemploymentCalculationSteps(
        input: UnemploymentInput,
        constants: LaborYearConstants,
        averageDailyWage: Double,
        baseDaily: Double,
        dependencyDaily: Double,
        rawDaily: Double,
        floorDaily: Double,
        capDaily: Double,
        dailyBenefitDouble: Double,
        appliedRule: Rule,
        monthlyBenefit: Long,
        entitledMonths: Int,
        remainingMonths: Int,
        totalPayout: Long,
        currencyUnit: CurrencyUnit
    ): List<CalculationStep> {
        val steps = mutableListOf<CalculationStep>()

        steps.add(
            CalculationStep(
                title = "۱. متوسط مزد روزانه",
                value = PersianNumberFormatter.formatCurrency(averageDailyWage.toLong(), currencyUnit),
                description = "میانگین ماهانه (${PersianNumberFormatter.formatCurrency(input.averageMonthlyWage, currencyUnit)}) ÷ ۳۰ روز"
            )
        )

        steps.add(
            CalculationStep(
                title = "۲. پایه مقرری (۵۵٪ متوسط روزانه)",
                value = PersianNumberFormatter.formatCurrency(baseDaily.toLong(), currencyUnit),
                description = "۵۵٪ × ${PersianNumberFormatter.formatCurrency(averageDailyWage.toLong(), currencyUnit)}"
            )
        )

        val dependentsClamped = minOf(maxOf(0, input.dependentsCount), constants.unemployment.maxDependents)
        steps.add(
            CalculationStep(
                title = "۳. حق عائله‌مندی و تکفل",
                value = PersianNumberFormatter.formatCurrency(dependencyDaily.toLong(), currencyUnit),
                description = "$dependentsClamped نفر تحت تکفل × ۱۰٪ × حداقل مزد روزانه (${PersianNumberFormatter.formatCurrency(constants.minimumDailyWage, currencyUnit)})"
            )
        )

        steps.add(
            CalculationStep(
                title = "۴. مجموع مقرری اولیه (خام)",
                value = PersianNumberFormatter.formatCurrency(rawDaily.toLong(), currencyUnit),
                description = "پایه (${PersianNumberFormatter.formatCurrency(baseDaily.toLong(), currencyUnit)}) + عائله‌مندی (${PersianNumberFormatter.formatCurrency(dependencyDaily.toLong(), currencyUnit)})"
            )
        )

        val ruleDesc = when (appliedRule) {
            Rule.FLOOR -> "مجموع اولیه کمتر از حداقل مزد روزانه قانونی (${PersianNumberFormatter.formatCurrency(floorDaily.toLong(), currencyUnit)}) است؛ بنابراین بر اساس قاعده FLOOR برابر کف تعیین گردید."
            Rule.CAP -> "مجموع اولیه بیشتر از سقف قانونی ۸۰٪ متوسط مزد (${PersianNumberFormatter.formatCurrency(capDaily.toLong(), currencyUnit)}) است؛ بنابراین بر اساس قاعده CAP برابر سقف تعیین گردید."
            Rule.NORMAL -> "مجموع اولیه بین کف قانونی (${PersianNumberFormatter.formatCurrency(floorDaily.toLong(), currencyUnit)}) و سقف قانونی (${PersianNumberFormatter.formatCurrency(capDaily.toLong(), currencyUnit)}) قرار دارد و بدون تغییر پذیرفته شد."
        }

        steps.add(
            CalculationStep(
                title = "۵. اعمال کف و سقف قانونی (قاعده $appliedRule)",
                value = PersianNumberFormatter.formatCurrency(dailyBenefitDouble.toLong(), currencyUnit) + " / روز",
                description = ruleDesc
            )
        )

        steps.add(
            CalculationStep(
                title = "۶. مقرری ماهانه نهایی (۳۰ روز)",
                value = PersianNumberFormatter.formatCurrency(monthlyBenefit, currencyUnit),
                description = "مقرری روزانه (${PersianNumberFormatter.formatCurrency(dailyBenefitDouble.toLong(), currencyUnit)}) × ۳۰"
            )
        )

        steps.add(
            CalculationStep(
                title = "۷. استحقاق مدت پرداخت و مجموع کل",
                value = "$remainingMonths ماه (مجموع ${PersianNumberFormatter.formatCurrency(totalPayout, currencyUnit)})",
                description = "سقف استحقاق سابقه (${input.totalInsuranceMonths} ماه): $entitledMonths ماه | استفاده‌شده قبلی: ${input.previousUsedMonths} ماه | باقیمانده: $remainingMonths ماه"
            )
        )

        return steps
    }
}
