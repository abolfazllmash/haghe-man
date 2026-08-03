package com.example.data.repository

import com.example.data.db.CalculationHistoryDao
import com.example.data.model.CalculationHistory
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: CalculationHistoryDao) {
    val allHistory: Flow<List<CalculationHistory>> = dao.getAllHistory()

    suspend fun saveHistory(item: CalculationHistory): Long {
        return dao.insertHistory(item)
    }

    suspend fun deleteHistory(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
