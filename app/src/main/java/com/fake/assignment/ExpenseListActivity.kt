package com.fake.assignment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class ExpenseListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        val rvExpenses = findViewById<RecyclerView>(R.id.rvExpenses)
        rvExpenses.layoutManager = LinearLayoutManager(this)
        
        val db = AppDatabase.getDatabase(this)
        
        lifecycleScope.launch {
            combine(db.goatDao().getAllExpenses(), db.goatDao().getAllCategories()) { expenseEntities, categoryEntities ->
                val categoryMap = categoryEntities.associate { it.categoryId to it.categoryName }
                
                expenseEntities.map {
                    Expense(
                        date = it.date,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        description = it.description,
                        category = categoryMap[it.categoryId] ?: "Unknown",
                        amount = it.amount,
                        photoUri = it.imageUri
                    )
                }
            }.collect { expenses ->
                rvExpenses.adapter = ExpenseAdapter(expenses) { expense ->
                    Toast.makeText(this@ExpenseListActivity, getString(R.string.msg_details, expense.description), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    class ExpenseAdapter(
        private val expenses: List<Expense>,
        private val onClick: (Expense) -> Unit
    ) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvCategory: TextView = view.findViewById(R.id.tvItemCategory)
            val tvAmount: TextView = view.findViewById(R.id.tvItemAmount)
            val tvDate: TextView = view.findViewById(R.id.tvItemDate)
            val tvDescription: TextView = view.findViewById(R.id.tvItemDescription)
            val ivPhoto: ImageView = view.findViewById(R.id.ivItemPhoto)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val expense = expenses[position]
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA"))
            
            holder.tvCategory.text = expense.category
            holder.tvAmount.text = currencyFormat.format(expense.amount)
            holder.tvDate.text = String.format("%s (%s - %s)", expense.date, expense.startTime, expense.endTime)
            holder.tvDescription.text = expense.description

            if (expense.photoUri != null) {
                holder.ivPhoto.visibility = View.VISIBLE
                holder.ivPhoto.setImageURI(Uri.parse(expense.photoUri))
            } else {
                holder.ivPhoto.visibility = View.GONE
            }

            holder.itemView.setOnClickListener { onClick(expense) }
        }

        override fun getItemCount() = expenses.size
    }
}