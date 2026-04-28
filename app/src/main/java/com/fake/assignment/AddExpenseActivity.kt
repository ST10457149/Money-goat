package com.fake.assignment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.*

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.first

class AddExpenseActivity : AppCompatActivity() {

    private var selectedDate = ""
    private var selectedStartTime = ""
    private var selectedEndTime = ""
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val btnPickDate = findViewById<Button>(R.id.btnPickDate)
        val btnStartTime = findViewById<Button>(R.id.btnStartTime)
        val btnEndTime = findViewById<Button>(R.id.btnEndTime)
        val btnAddPhoto = findViewById<Button>(R.id.btnAddPhoto)
        val ivPreview = findViewById<ImageView>(R.id.ivPreview)
        val btnSaveExpense = findViewById<Button>(R.id.btnSaveExpense)

        val db = AppDatabase.getDatabase(this)

        // Setup Spinner
        lifecycleScope.launch {
            db.goatDao().getAllCategories().collect { categories ->
                val categoryNames = categories.map { it.categoryName }.toMutableList()
                if (categoryNames.isEmpty()) {
                    // Fallback to DataManager if DB empty
                    categoryNames.addAll(DataManager.categories.map { it.name })
                }
                val adapter = ArrayAdapter(this@AddExpenseActivity, R.layout.spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spCategory.adapter = adapter
            }
        }

        // Date Picker
        btnPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$day/${month + 1}/$year"
                btnPickDate.text = selectedDate
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Time Pickers
        btnStartTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                selectedStartTime = String.format("%02d:%02d", hour, minute)
                btnStartTime.text = "Start: $selectedStartTime"
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        btnEndTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                selectedEndTime = String.format("%02d:%02d", hour, minute)
                btnEndTime.text = "End: $selectedEndTime"
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        // Photo Picker
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                ivPreview.setImageURI(it)
                ivPreview.visibility = View.VISIBLE
                // Grant permission for the URI if needed, but for prototype setImageURI is fine
            }
        }
        btnAddPhoto.setOnClickListener { pickImage.launch("image/*") }

        // Save
        btnSaveExpense.setOnClickListener {
            val description = etDescription.text.toString()
            val amountStr = etAmount.text.toString()
            val categoryName = spCategory.selectedItem?.toString() ?: ""

            if (description.isEmpty() || amountStr.isEmpty() || selectedDate.isEmpty() || categoryName.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                Log.w("AddExpenseActivity", "Save failed: Missing fields")
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull() ?: 0.0
            
            lifecycleScope.launch {
                // Get all categories to find the ID for the selected name
                val categories = db.goatDao().getAllCategories().first()
                val categoryId = categories.find { it.categoryName == categoryName }?.categoryId ?: 0

                val expense = ExpenseEntity(
                    date = selectedDate,
                    startTime = selectedStartTime,
                    endTime = selectedEndTime,
                    description = description,
                    amount = amount,
                    categoryId = categoryId,
                    imageUri = selectedImageUri?.toString()
                )
                
                db.goatDao().insertExpense(expense)
                
                // Keep DataManager in sync for legacy code
                DataManager.expenses.add(Expense(
                    date = selectedDate,
                    startTime = selectedStartTime,
                    endTime = selectedEndTime,
                    description = description,
                    category = categoryName,
                    amount = amount,
                    photoUri = selectedImageUri?.toString()
                ))

                Log.i("AddExpenseActivity", "Expense saved to Room: $description, Category: $categoryName (ID: $categoryId)")
                Toast.makeText(this@AddExpenseActivity, "Expense Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}