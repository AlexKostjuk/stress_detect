package com.stress_detekt.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.stress_detekt.utils.PrefsManager

class SplashActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PrefsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefsManager = PrefsManager(this)
        
        // Затримка 2 секунди перед перевіркою
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000)
    }
    
    private fun checkLoginStatus() {
        val intent = if (prefsManager.isLoggedIn()) {
            // Користувач залогінений → переходимо на головну
            Intent(this, MainActivity::class.java)
        } else {
            // Користувач не залогінений → переходимо на логін
            Intent(this, LoginActivity::class.java)
        }
        
        startActivity(intent)
        finish()
    }
}
