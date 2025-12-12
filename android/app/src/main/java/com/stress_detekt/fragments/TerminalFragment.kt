package com.stress_detekt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.stress_detekt.R
import com.stress_detekt.databinding.FragmentTerminalBinding

class TerminalFragment : Fragment() {
    
    private var _binding: FragmentTerminalBinding? = null
    private val binding get() = _binding!!
    
    private val logMessages = mutableListOf<String>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTerminalBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        initializeTerminal()
    }
    
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_terminal_to_dashboard)
        }
        
        binding.btnClear.setOnClickListener {
            clearLogs()
        }
    }
    
    private fun initializeTerminal() {
        addLog("[SYSTEM] Термінал запущено")
        addLog("[INFO] Stress Detekt v1.0")
        addLog("[INFO] Очікування команд...")
    }
    
    private fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat(
            "HH:mm:ss",
            java.util.Locale.getDefault()
        ).format(java.util.Date())
        
        val logEntry = "[$timestamp] $message"
        logMessages.add(logEntry)
        
        // Оновлюємо TextView
        binding.tvLogs.text = logMessages.joinToString("\n")
        
        // Scroll вниз
        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }
    
    private fun clearLogs() {
        logMessages.clear()
        binding.tvLogs.text = ""
        addLog("[SYSTEM] Логи очищено")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
