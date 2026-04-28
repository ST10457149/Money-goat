package com.fake.assignment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.NumberFormat
import java.util.*

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalSpending: TextView
    private lateinit var pbBudget: ProgressBar
    private lateinit var tvBudgetStatus: TextView
    private lateinit var tvInsight: TextView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = AppDatabase.getDatabase(this)

        tvTotalSpending = findViewById(R.id.tvTotalSpending)
        pbBudget = findViewById(R.id.pbBudget)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        tvInsight = findViewById(R.id.tvInsight)

        findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewExpenses).setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }

        findViewById<Button>(R.id.btnCategories).setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        findViewById<Button>(R.id.btnAnalytics).setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }
        
        Log.i("DashboardActivity", "Dashboard loaded")
        
        // Listen to DB changes
        lifecycleScope.launch {
            db.goatDao().getAllExpenses().collect { expenses ->
                val total = expenses.sumOf { it.amount }
                updateUIWithData(total)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // updateUI() - Removed as Flow handles updates
    }

    private fun updateUIWithData(total: Double) {
        lifecycleScope.launch {
            val budget = db.goatDao().getBudgetForMonth("current")
            val maxBudget = budget?.maxGoal?.toDouble() ?: DataManager.maxMonthlyGoal.toDouble()
            val minBudget = budget?.minGoal?.toDouble() ?: DataManager.minMonthlyGoal.toDouble()

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA"))
            tvTotalSpending.text = currencyFormat.format(total)

            if (maxBudget > 0) {
                val progress = ((total / maxBudget) * 100).toInt()
                pbBudget.progress = progress.coerceAtMost(100)
                
                tvBudgetStatus.text = getString(R.string.budget_status, currencyFormat.format(minBudget), currencyFormat.format(maxBudget))
                
                when {
                    total > maxBudget -> {
                        tvTotalSpending.setTextColor(Color.RED)
                        tvInsight.text = getString(R.string.insight_over)
                        tvInsight.setBackgroundColor(Color.parseColor("#331111"))
                        tvInsight.setTextColor(Color.RED)
                    }
                    total > minBudget -> {
                        tvTotalSpending.setTextColor(Color.parseColor("#FFA500")) // Orange
                        tvInsight.text = getString(R.string.insight_warning)
                        tvInsight.setBackgroundColor(Color.parseColor("#332211"))
                        tvInsight.setTextColor(Color.parseColor("#FFA500"))
                    }
                    else -> {
                        tvTotalSpending.setTextColor(Color.parseColor("#32CD32")) // Lime
                        tvInsight.text = getString(R.string.insight_good)
                        tvInsight.setBackgroundColor(Color.parseColor("#113311"))
                        tvInsight.setTextColor(Color.parseColor("#32CD32"))
                    }
                }
            }
            Log.i("DashboardActivity", "UI updated from DB. Total spending: $total")
        }
    }
}