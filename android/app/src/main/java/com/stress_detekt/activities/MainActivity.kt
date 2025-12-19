package com.stress_detekt.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

import com.stress_detekt.R
import com.stress_detekt.databinding.ActivityMainBinding
import com.stress_detekt.services.MultiSensorService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Глобальний сервіс для всього додатку
    lateinit var sensorService: MultiSensorService
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ініціалізуємо сервіс один раз
        sensorService = MultiSensorService(this)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Зупиняємо датчики тільки при знищенні Activity
        if (sensorService.isMonitoring.value == true) {
            println("✓ MainActivity.onDestroy: Stopping sensors")
            sensorService.stopMonitoring()
        }
    }
}