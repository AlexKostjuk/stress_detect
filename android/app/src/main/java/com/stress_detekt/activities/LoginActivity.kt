package com.stress_detekt.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.stress_detekt.database.AppDatabase
import com.stress_detekt.databinding.ActivityLoginBinding
import com.stress_detekt.utils.PrefsManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: AppDatabase
    private lateinit var prefsManager: PrefsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        database = AppDatabase.getDatabase(this)
        prefsManager = PrefsManager(this)
        
        setupListeners()
    }
    
    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }
        
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        return when {
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
            else -> true
        }
    }
    
    private fun performLogin(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val user = database.userDao().login(email, password)
                
                if (user != null) {
                    // Успішний логін
                    prefsManager.saveLoginSession(user.id, user.name, user.email)
                    
                    Toast.makeText(
                        this@LoginActivity,
                        "Вітаємо, ${user.name}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Переходимо на головну
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Невірний email або пароль
                    Toast.makeText(
                        this@LoginActivity,
                        "Невірний email або пароль",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnLogin.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Помилка: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnLogin.isEnabled = true
            }
        }
    }
}
