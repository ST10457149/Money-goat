package com.fake.assignment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class CategoryActivity : AppCompatActivity() {

    private lateinit var adapter: CategoryAdapter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA"))
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        db = AppDatabase.getDatabase(this)

        val tvMinGoalLabel = findViewById<TextView>(R.id.tvMinGoalLabel)
        val sbMinGoal = findViewById<SeekBar>(R.id.sbMinGoal)
        val tvMaxGoalLabel = findViewById<TextView>(R.id.tvMaxGoalLabel)
        val sbMaxGoal = findViewById<SeekBar>(R.id.sbMaxGoal)
        val etNewCategory = findViewById<EditText>(R.id.etNewCategory)
        val btnAddCategory = findViewById<Button>(R.id.btnAddCategory)
        val rvCategories = findViewById<RecyclerView>(R.id.rvCategories)

        // Initialize SeekBars and load budget from Room
        lifecycleScope.launch {
            val budget = db.goatDao().getBudgetForMonth("current")
            val min = budget?.minGoal ?: DataManager.minMonthlyGoal
            val max = budget?.maxGoal ?: DataManager.maxMonthlyGoal
            
            // Set values without triggering listeners first if possible, 
            // but setting them here and then listeners below is safer.
            sbMinGoal.progress = min
            tvMinGoalLabel.text = getString(R.string.min_goal_label, currencyFormat.format(min))
            
            sbMaxGoal.progress = max
            tvMaxGoalLabel.text = getString(R.string.max_goal_label, currencyFormat.format(max))

            // Now set listeners after initial values are loaded
            sbMinGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        DataManager.minMonthlyGoal = progress
                        tvMinGoalLabel.text = getString(R.string.min_goal_label, currencyFormat.format(progress))
                        saveBudget()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            sbMaxGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        DataManager.maxMonthlyGoal = progress
                        tvMaxGoalLabel.text = getString(R.string.max_goal_label, currencyFormat.format(progress))
                        saveBudget()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        // RecyclerView setup with Flow from Room
        rvCategories.layoutManager = LinearLayoutManager(this)
        lifecycleScope.launch {
            db.goatDao().getAllCategories().collect { categoryEntities ->
                Log.i("CategoryActivity", "Categories read from DB: ${categoryEntities.size}")
                val categories = categoryEntities.map { Category(id = it.categoryId, name = it.categoryName) }
                adapter = CategoryAdapter(categories)
                rvCategories.adapter = adapter
            }
        }

        btnAddCategory.setOnClickListener {
            val name = etNewCategory.text.toString()
            if (name.isNotEmpty()) {
                lifecycleScope.launch {
                    db.goatDao().insertCategory(CategoryEntity(categoryName = name))
                    etNewCategory.text.clear()
                    Log.i("CategoryActivity", "Category saved: $name")
                }
            } else {
                Toast.makeText(this, R.string.err_enter_category, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBudget() {
        lifecycleScope.launch {
            val budget = BudgetEntity(
                budgetId = 1, // Simple single-budget approach
                minGoal = DataManager.minMonthlyGoal,
                maxGoal = DataManager.maxMonthlyGoal,
                month = "current"
            )
            db.goatDao().insertBudget(budget)
            Log.i("CategoryActivity", "Budget goals updated: Min=${budget.minGoal}, Max=${budget.maxGoal}")
        }
    }

    class CategoryAdapter(private val categories: List<Category>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvCategoryName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvName.text = categories[position].name
        }

        override fun getItemCount() = categories.size
    }
}