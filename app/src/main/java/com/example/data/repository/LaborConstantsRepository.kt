package com.example.data.repository

import android.content.Context
import com.example.data.model.LaborYearConstants
import com.example.data.model.ShiftWorkRates
import com.example.data.model.TaxBracket
import org.json.JSONObject

class LaborConstantsRepository(private val context: Context) {

    private var cachedConstantsMap: Map<String, LaborYearConstants>? = null

    fun getConstantsForYear(year: String): LaborYearConstants {
        val map = getAllYearConstants()
        return map[year] ?: map.values.firstOrNull() ?: getDefaultConstants()
    }

    fun getAvailableYears(): List<String> {
        return getAllYearConstants().keys.toList().sortedDescending()
    }

    fun getAllYearConstants(): Map<String, LaborYearConstants> {
        cachedConstantsMap?.let { return it }

        val map = mutableMapOf<String, LaborYearConstants>()
        try {
            val jsonString = context.assets.open("labor_constants.json").bufferedReader().use { it.readText() }
            val rootObj = JSONObject(jsonString)

            val keys = rootObj.keys()
            while (keys.hasNext()) {
                val yearKey = keys.next()
                val yearObj = rootObj.getJSONObject(yearKey)

                val shiftObj = yearObj.optJSONObject("shift_work")
                val shiftRates = ShiftWorkRates(
                    morningEvening = shiftObj?.optDouble("morning_evening", 0.10) ?: 0.10,
                    morningEveningNight = shiftObj?.optDouble("morning_evening_night", 0.15) ?: 0.15,
                    twoShiftWithNight = shiftObj?.optDouble("two_shift_with_night", 0.225) ?: 0.225
                )

                val taxArray = yearObj.optJSONArray("tax_brackets")
                val taxBrackets = mutableListOf<TaxBracket>()
                if (taxArray != null) {
                    for (i in 0 until taxArray.length()) {
                        val tObj = taxArray.getJSONObject(i)
                        taxBrackets.add(
                            TaxBracket(
                                from = tObj.optLong("from", 0L),
                                to = tObj.optLong("to", 0L),
                                rate = tObj.optDouble("rate", 0.0)
                            )
                        )
                    }
                }

                val unempObj = yearObj.optJSONObject("unemployment")
                val unemploymentConsts = if (unempObj != null) {
                    val tableArray = unempObj.optJSONArray("duration_table")
                    val durationList = mutableListOf<com.example.data.model.UnemploymentDurationRange>()
                    if (tableArray != null) {
                        for (i in 0 until tableArray.length()) {
                            val row = tableArray.getJSONObject(i)
                            durationList.add(
                                com.example.data.model.UnemploymentDurationRange(
                                    from = row.optInt("from"),
                                    to = if (row.isNull("to")) null else row.optInt("to"),
                                    single = row.optInt("single"),
                                    married = row.optInt("married")
                                )
                            )
                        }
                    }
                    com.example.data.model.UnemploymentConstants(
                        benefitRate = unempObj.optDouble("benefit_rate", 0.55),
                        dependentRate = unempObj.optDouble("dependent_rate", 0.10),
                        maxDependents = unempObj.optInt("max_dependents", 4),
                        capRateOfAverage = unempObj.optDouble("cap_rate_of_average", 0.80),
                        minInsuranceMonths = unempObj.optInt("min_insurance_months", 6),
                        minMonthsIfContractExpiry = unempObj.optInt("min_months_if_contract_expiry", 12),
                        durationTable = if (durationList.isNotEmpty()) durationList else com.example.data.model.UnemploymentConstants().durationTable
                    )
                } else {
                    com.example.data.model.UnemploymentConstants()
                }

                val constants = LaborYearConstants(
                    minimumDailyWage = yearObj.optLong("minimum_daily_wage", 3530000L),
                    housingAllowance = yearObj.optLong("housing_allowance", 9000000L),
                    foodAllowance = yearObj.optLong("food_allowance", 21000000L),
                    childAllowanceMultiplier = yearObj.optInt("child_allowance_multiplier", 3),
                    overtimeMultiplier = yearObj.optDouble("overtime_multiplier", 1.4),
                    nightShiftRate = yearObj.optDouble("night_shift_rate", 0.35),
                    fridayRate = yearObj.optDouble("friday_rate", 0.40),
                    shiftWork = shiftRates,
                    insuranceEmployeeRate = yearObj.optDouble("insurance_employee_rate", 0.07),
                    insuranceEmployerRate = yearObj.optDouble("insurance_employer_rate", 0.23),
                    dailyWorkHours = yearObj.optDouble("daily_work_hours", 7.33),
                    annualLeaveDays = yearObj.optInt("annual_leave_days", 26),
                    taxBrackets = taxBrackets,
                    unemployment = unemploymentConsts,
                    sourceNote = yearObj.optString("source_note", "")
                )
                map[yearKey] = constants
            }
        } catch (e: Exception) {
            e.printStackTrace()
            map["1405"] = getDefaultConstants()
        }

        cachedConstantsMap = map
        return map
    }

    private fun getDefaultConstants(): LaborYearConstants {
        return LaborYearConstants(
            minimumDailyWage = 3530000L,
            housingAllowance = 9000000L,
            foodAllowance = 21000000L,
            childAllowanceMultiplier = 3,
            overtimeMultiplier = 1.4,
            nightShiftRate = 0.35,
            fridayRate = 0.4,
            shiftWork = ShiftWorkRates(),
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
            sourceNote = "بخشنامه مزد وزارت کار سال ۱۴۰۵"
        )
    }
}
