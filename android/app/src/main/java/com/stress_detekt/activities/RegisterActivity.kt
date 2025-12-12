package com.stress_detekt.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.stress_detekt.database.AppDatabase
import com.stress_detekt.database.User
import com.stress_detekt.databinding.ActivityRegisterBinding
import com.stress_detekt.utils.PrefsManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: AppDatabase
    private lateinit var prefsManager: PrefsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        database = AppDatabase.getDatabase(this)
        prefsManager = PrefsManager(this)
        
        setupListeners()
    }
    
    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            
            if (validateInputs(name, email, password, confirmPassword)) {
                performRegistration(name, email, password)
            }
        }
        
        binding.tvLogin.setOnClickListener {
            finish() // Повертаємось на LoginActivity
        }
    }
    
    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            name.isEmpty() -> {
                binding.etName.error = "Ім'я не може бути порожнім"
                false
            }
            name.length < 2 -> {
                binding.etName.error = "Ім'я має бути мінімум 2 символи"
                false
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Email не може бути порожнім"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Невірний формат email"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Пароль не може бути порожнім"
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Пароль має бути мінімум 6 символів"
                false
            }
            password != confirmPassword -> {
                binding.etConfirmPassword.error = "Паролі не співпадають"
                false
            }
            else -> true
        }
    }
    
    private fun performRegistration(name: String, email: String, password: String) {
        binding.btnRegister.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // Перевіряємо чи email вже існує
                val existingUser = database.userDao().getUserByEmail(email)
                
                if (existingUser != null) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Користувач з таким email вже існує",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnRegister.isEnabled = true
                    return@launch
                }
                
                // Створюємо нового користувача
                val user = User(
                    email = email,
                    password = password,
                    name = name
                )
                
                val userId = database.userDao().insertUser(user)
                
                // Автоматичний логін після реєстрації
                prefsManager.saveLoginSession(userId, name, email)
                
                Toast.makeText(
                    this@RegisterActivity,
                    "Реєстрація успішна! Вітаємо, $name!",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Переходимо на головну
                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Помилка реєстрації: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnRegister.isEnabled = true
            }
        }
    }
}
