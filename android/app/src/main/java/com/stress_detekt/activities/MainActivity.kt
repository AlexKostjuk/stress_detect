package com.stress_detekt.activities
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.stress_detekt.R
import com.stress_detekt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
    }

    private fun setupNavigation() {
        // ← ЭТО ОБХОДИТ R-КЛАСС И КЭШ НАВСЕГДА
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
    }
}
