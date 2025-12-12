package com.stress_detekt.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.stress_detekt.activities.LoginActivity
import com.stress_detekt.databinding.FragmentDashboardBinding
import com.stress_detekt.utils.PrefsManager

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var prefsManager: PrefsManager
    private var isMonitoring = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefsManager = PrefsManager(requireContext())
        
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {
        // Відображаємо ім'я користувача
        val userName = prefsManager.getUserName() ?: "Користувач"
        binding.tvWelcome.text = "Вітаємо, $userName!"
        
        updateMonitoringButton()
    }
    
    private fun setupListeners() {
        // Старт/Стоп вимірювання
        binding.btnMonitoring.setOnClickListener {
            isMonitoring = !isMonitoring
            updateMonitoringButton()
            
            if (isMonitoring) {
                startMonitoring()
            } else {
                stopMonitoring()
            }
        }
        
        // Логаут
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
        
        // Закрити додаток
        binding.btnExit.setOnClickListener {
            showExitDialog()
        }
    }
    
    private fun updateMonitoringButton() {
        if (isMonitoring) {
            binding.btnMonitoring.text = "⏹ ЗУПИНИТИ ВИМІРЮВАННЯ"
            binding.tvStatus.text = "Статус: Вимірювання активне"
            binding.tvStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        } else {
            binding.btnMonitoring.text = "▶ ПОЧАТИ ВИМІРЮВАННЯ"
            binding.tvStatus.text = "Статус: Неактивно"
            binding.tvStatus.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }
    
    private fun startMonitoring() {
        // TODO: Запустити збір даних з датчиків
        binding.tvInfo.text = "Збираємо дані з датчиків...\n" +
                "- Акселерометр ✓\n" +
                "- Гіроскоп ✓\n" +
                "- GPS ✓"
    }
    
    private fun stopMonitoring() {
        // TODO: Зупинити збір даних
        binding.tvInfo.text = "Вимірювання зупинено.\nНатисніть 'Почати' для старту."
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Вихід")
            .setMessage("Ви впевнені що хочете вийти?")
            .setPositiveButton("Так") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Ні", null)
            .show()
    }
    
    private fun performLogout() {
        prefsManager.logout()
        
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    private fun showExitDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Закрити додаток")
            .setMessage("Ви впевнені що хочете закрити додаток?")
            .setPositiveButton("Так") { _, _ ->
                requireActivity().finishAffinity()
            }
            .setNegativeButton("Ні", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
