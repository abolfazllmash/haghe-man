package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val typeName: String,
    val year: String,
    val netAmountRial: Long,
    val summaryText: String,
    val jsonInputData: String,
    val jsonResultData: String,
    val createdAt: Long = System.currentTimeMillis()
)
