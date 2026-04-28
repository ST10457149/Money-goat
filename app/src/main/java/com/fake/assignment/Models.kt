package com.fake.assignment

import android.net.Uri

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Category(
    val id: Int = 0,
    val name: String,
    val icon: Int = 0
)

data class Expense(
    val id: Long = System.currentTimeMillis(),
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val category: String,
    val amount: Double,
    val photoUri: String? = null
)

object DataManager {
    val categories = mutableListOf<Category>()
    
    val expenses = mutableListOf<Expense>()
    
    var minMonthlyGoal: Int = 1000
    var maxMonthlyGoal: Int = 5000
    
    fun getTotalSpending(): Double {
        return expenses.sumOf { it.amount }
    }
}