package com.stress_detekt.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * TerminalViewModel - ViewModel для Terminal output
 * 
 * Управляє історією виводу в термінал
 */
class TerminalViewModel : ViewModel() {
    
    // Максимальна кількість рядків в історії
    private val maxLines = 1000
    
    // Список рядків виводу
    private val _output = MutableLiveData<List<String>>(emptyList())
    val output: LiveData<List<String>> = _output
    
    // Поточна кількість рядків
    val lineCount: Int
        get() = _output.value?.size ?: 0
    
    /**
     * Додати рядок виводу
     */
    fun addOutput(line: String) {
        val currentLines = _output.value?.toMutableList() ?: mutableListOf()
        currentLines.add(line)
        
        // Обмежити історію
        if (currentLines.size > maxLines) {
            currentLines.removeAt(0)
        }
        
        _output.value = currentLines
    }
    
    /**
     * Додати кілька рядків
     */
    fun addOutputLines(lines: List<String>) {
        val currentLines = _output.value?.toMutableList() ?: mutableListOf()
        currentLines.addAll(lines)
        
        // Обмежити історію
        while (currentLines.size > maxLines) {
            currentLines.removeAt(0)
        }
        
        _output.value = currentLines
    }
    
    /**
     * Очистити термінал
     */
    fun clear() {
        _output.value = emptyList()
    }
    
    /**
     * Отримати весь текст як один String
     */
    fun getAllText(): String {
        return _output.value?.joinToString("\n") ?: ""
    }
    
    /**
     * Отримати останні N рядків
     */
    fun getLastLines(n: Int): List<String> {
        val lines = _output.value ?: emptyList()
        return if (lines.size > n) {
            lines.takeLast(n)
        } else {
            lines
        }
    }
}
