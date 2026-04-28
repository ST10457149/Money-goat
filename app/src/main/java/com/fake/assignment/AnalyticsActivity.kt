package com.fake.assignment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.combine

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var adapter: AnalyticsAdapter
    private var filterDate: String? = null
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        db = AppDatabase.getDatabase(this)

        val btnFilterDate = findViewById<Button>(R.id.btnFilterDate)
        val tvCurrentFilter = findViewById<TextView>(R.id.tvCurrentFilter)
        val rvAnalytics = findViewById<RecyclerView>(R.id.rvAnalytics)

        rvAnalytics.layoutManager = LinearLayoutManager(this)
        
        // Initial load
        observeExpenses(null)

        btnFilterDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                filterDate = "$day/${month + 1}/$year"
                tvCurrentFilter.text = getString(R.string.showing_filter, filterDate)
                observeExpenses(filterDate)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun observeExpenses(date: String?) {
        lifecycleScope.launch {
            combine(db.goatDao().getAllExpenses(), db.goatDao().getAllCategories()) { expenses, categories ->
                val categoryMap = categories.associate { it.categoryId to it.categoryName }
                val filtered = if (date == null) expenses else expenses.filter { it.date == date }
                
                filtered.groupBy { it.categoryId }
                    .map { (catId, exps) ->
                        (categoryMap[catId] ?: "Unknown") to exps.sumOf { it.amount }
                    }
            }.collect { dataList ->
                adapter = AnalyticsAdapter(dataList)
                findViewById<RecyclerView>(R.id.rvAnalytics).adapter = adapter
            }
        }
    }

    class AnalyticsAdapter(private val data: List<Pair<String, Double>>) : RecyclerView.Adapter<AnalyticsAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvCategory: TextView = view.findViewById(R.id.tvAnalyticsCategory)
            val tvTotal: TextView = view.findViewById(R.id.tvAnalyticsAmount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_analytics, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA"))
            holder.tvCategory.text = item.first
            holder.tvTotal.text = currencyFormat.format(item.second)
        }

        override fun getItemCount() = data.size
    }
}