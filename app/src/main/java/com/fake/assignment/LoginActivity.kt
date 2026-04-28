package com.fake.assignment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        
        val db = AppDatabase.getDatabase(this)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Log.w("LoginActivity", "Login attempt failed: Empty fields")
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val user = db.goatDao().getUserByUsername(username)
                    if (user != null && user.password == password) {
                        Log.i("LoginActivity", "Login successful for user: $username")
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else if (username == "admin" && password == "password") {
                        // Fallback for first run and Seed categories
                        Log.i("LoginActivity", "Login successful (admin fallback)")
                        
                        // Seed default categories if DB is empty
                        val currentCats = db.goatDao().getAllCategories().first()
                        if (currentCats.isEmpty()) {
                            val defaults = listOf("Streaming", "Side Hustles", "Transport", "Food", "Bills")
                            defaults.forEach { 
                                db.goatDao().insertCategory(CategoryEntity(categoryName = it))
                            }
                            Log.i("LoginActivity", "Seeded default categories")
                        }

                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        Log.w("LoginActivity", "Login failed: Invalid credentials for $username")
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}