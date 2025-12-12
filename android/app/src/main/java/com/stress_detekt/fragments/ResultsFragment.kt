package com.stress_detekt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.stress_detekt.databinding.FragmentResultsBinding

class ResultsFragment : Fragment() {
    
    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadResults()
    }
    
    private fun loadResults() {
        // TODO: Завантажити результати з БД
        
        // Тимчасові дані для демонстрації
        binding.tvStressLevel.text = "45%"
        binding.tvStressStatus.text = "Середній рівень стресу"
        
        binding.tvHeartRate.text = "72 bpm"
        binding.tvHrvValue.text = "45 ms"
        binding.tvActivityLevel.text = "Помірна активність"
        
        // Встановлюємо колір залежно від рівня стресу
        val stressLevel = 45
        val color = when {
            stressLevel < 30 -> android.R.color.holo_green_dark
            stressLevel < 70 -> android.R.color.holo_orange_dark
            else -> android.R.color.holo_red_dark
        }
        
        binding.tvStressLevel.setTextColor(resources.getColor(color, null))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
