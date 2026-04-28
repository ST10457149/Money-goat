package com.fake.assignment

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- ENTITIES ---

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val username: String,
    val password: String
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val categoryId: Int = 0,
    val categoryName: String
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val expenseId: Int = 0,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val amount: Double,
    val categoryId: Int,
    val imageUri: String?
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val budgetId: Int = 0,
    val minGoal: Int,
    val maxGoal: Int,
    val month: String
)

// --- DAO ---

@Dao
interface MoneyGoatDao {
    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Query
    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>>

    @Query("SELECT categoryId, SUM(amount) as total FROM expenses GROUP BY categoryId")
    fun getTotalPerCategory(): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    // Update
    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE month = :month LIMIT 1")
    suspend fun getBudgetForMonth(month: String): BudgetEntity?
}

data class CategoryTotal(
    val categoryId: Int,
    val total: Double
)

// --- DATABASE ---

@Database(entities = [User::class, CategoryEntity::class, ExpenseEntity::class, BudgetEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goatDao(): MoneyGoatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moneygoat_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}