package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "کارمند جدید",
    val grossMonthlyWageRial: Long = 0L,
    val year: String = "1405",
    val childrenCount: Int = 0,
    val overtimeHours: Double = 0.0,
    val remainingLeaveDays: Double = 0.0,
    val nightShiftHours: Double = 0.0,
    val fridayHours: Double = 0.0,
    val workedDaysInYear: Int = 365,
    val updatedAt: Long = System.currentTimeMillis()
)
