# MoneyGoat 🐐

MoneyGoat is a Gen Z-focused budget tracking application designed to make financial management less of a chore and more of a game. With high-contrast styling and dynamic "Goat Mode" feedback, it helps users stay on top of their spending while keeping the vibe chill.

## 🚀 Concept
The core of MoneyGoat is the **Goat Mode** gamification. Depending on your spending relative to your goals, the app's personality shifts:
- **CHILL 🐐✨**: Under your minimum goal. Everything is fine.
- **WARNING 🐐⚠️**: Passed your minimum goal. Time to be careful.
- **PANIC 🐐💢**: Over your maximum goal. Total chaos!

## ✨ Features
- **Expense Tracking**: Easily log expenses with descriptions, amounts, and dates.
- **Custom Categories**: Organize your cash flow with custom categories like "Streaming," "Side Hustles," and "Food."
- **Budget Goals**: Set dynamic minimum and maximum monthly spending goals using interactive SeekBars.
- **Photo Attachments**: Save receipts or proof of purchase directly to your expense records.
- **Analytics**: View a breakdown of spending per category with date-based filtering.
- **RoomDB Persistence**: All your data is saved locally on your device using a robust SQLite database.

## 🛠️ Tech Stack
- **Kotlin**: Primary programming language.
- **Room Database**: For offline data persistence.
- **Coroutines & Flow**: For reactive UI updates.
- **Material Design**: High-contrast theme (Black, Grey, Lime Green).

## 🏃 How to Run the App
To get MoneyGoat running locally for marking:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/MoneyGoat.git
   ```
2. **Open in Android Studio**:
   - Open Android Studio and select **File > Open**.
   - Navigate to the cloned project folder and click **OK**.
3. **Sync Gradle**:
   - Wait for the project to sync and build (Android Studio should do this automatically).
4. **Run on Emulator/Device**:
   - Select an Android Virtual Device (AVD) or connect a physical device (API 24+).
   - Click the **Run** button (green play icon) in the toolbar.
5. **Login**:
   - For first-time access, use the admin credentials:
     - **Username**: `admin`
     - **Password**: `password`
   - This will seed the initial categories and allow you to explore the features.

## 📸 Screenshots
*(Add your screenshots here later)*

---
*Developed as part of the Mobile Development assignment.*
