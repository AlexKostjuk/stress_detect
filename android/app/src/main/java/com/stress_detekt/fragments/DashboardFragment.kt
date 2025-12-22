package com.stress_detekt.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.stress_detekt.R
import com.stress_detekt.activities.MainActivity
import com.stress_detekt.databinding.FragmentDashboardBinding
import com.stress_detekt.services.MultiSensorService
import com.stress_detekt.testing.AutoTestRunner
import com.stress_detekt.viewmodels.TerminalViewModel

/**
 * DashboardFragment - Головний екран моніторингу
 *
 * Відповідає за:
 * - Відображення даних з датчиків в реальному часі
 * - Керування моніторингом (Start/Stop)
 * - Автоматичне тестування через Terminal
 * - Статистика збору даних
 *
 * Шлях: app/src/main/java/com/stress_detekt/fragments/DashboardFragment.kt
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Sensor service (з MainActivity)
    private lateinit var sensorService: MultiSensorService

    // Test runner для автоматичних тестів
    private var testRunner: AutoTestRunner? = null

    // ViewModel для Terminal output
    private val terminalViewModel: TerminalViewModel by viewModels()

    // Поточний user ID (пізніше буде з auth)
    private val userId = 1L

    // Permission launcher для Activity Recognition (Step Counter)
    private val activityRecognitionPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            terminalViewModel.addOutput("✅ Activity Recognition permission granted")
        } else {
            terminalViewModel.addOutput("❌ Activity Recognition permission denied")
        }
    }

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

        // Ініціалізація
        initializeSensorService()
        initializeTestRunner()
        setupButtons()
        setupObservers()

        // Перевірити permissions
        checkPermissions()

        // Привітання в Terminal
        terminalViewModel.addOutput("═══════════════════════════════════════")
        terminalViewModel.addOutput("  📱 STRESS DETEKT DASHBOARD")
        terminalViewModel.addOutput("═══════════════════════════════════════")
        terminalViewModel.addOutput("Terminal ready. Press a button to start.")
        terminalViewModel.addOutput("")
    }

    /**
     * Ініціалізувати SensorService з MainActivity
     */
    private fun initializeSensorService() {
        val mainActivity = requireActivity() as MainActivity
        sensorService = mainActivity.getSensorService()
    }

    /**
     * Ініціалізувати AutoTestRunner
     */
    private fun initializeTestRunner() {
        testRunner = AutoTestRunner(sensorService) { output ->
            terminalViewModel.addOutput(output)
        }
    }

    /**
     * Налаштувати кнопки
     */
    private fun setupButtons() {
        // ═══════════════════════════════════════════════════════════════════
        // MONITORING CONTROLS
        // ═══════════════════════════════════════════════════════════════════

        binding.btnStartMonitoring.setOnClickListener {
            terminalViewModel.addOutput("▶️  Starting monitoring...")
            sensorService.startMonitoring(userId)

            // Оновити UI
            updateMonitoringStatus()
        }

        binding.btnStopMonitoring.setOnClickListener {
            terminalViewModel.addOutput("⏹️  Stopping monitoring...")
            sensorService.stopMonitoring()

            // Оновити UI
            updateMonitoringStatus()
        }

        // ═══════════════════════════════════════════════════════════════════
        // TEST CONTROLS
        // ═══════════════════════════════════════════════════════════════════

        binding.btnRunAllTests.setOnClickListener {
            terminalViewModel.clear()
            testRunner?.runAllTests()
        }

        binding.btnQuickTest.setOnClickListener {
            terminalViewModel.clear()
            testRunner?.runQuickTest()
        }

        binding.btnBufferTest.setOnClickListener {
            terminalViewModel.clear()
            testRunner?.runBufferTest()
        }

        binding.btnConfigTest.setOnClickListener {
            terminalViewModel.clear()
            testRunner?.runConfigTest()
        }

        binding.btnStatusTest.setOnClickListener {
            terminalViewModel.clear()
            testRunner?.runStatusTest()
        }

        // ═══════════════════════════════════════════════════════════════════
        // TERMINAL CONTROLS
        // ═══════════════════════════════════════════════════════════════════

        binding.btnClearTerminal.setOnClickListener {
            terminalViewModel.clear()
            terminalViewModel.addOutput("Terminal cleared.")
        }

        // ═══════════════════════════════════════════════════════════════════
        // STATISTICS
        // ═══════════════════════════════════════════════════════════════════

        binding.btnShowStats.setOnClickListener {
            showCollectionStatistics()
        }

        binding.btnShowBuffer.setOnClickListener {
            showBufferStatistics()
        }
    }

    /**
     * Налаштувати Observers
     */
    private fun setupObservers() {
        // Observe sensor data для real-time display
        sensorService.sensorDataLive.observe(viewLifecycleOwner, Observer { data ->
            updateSensorDisplay(data)
        })

        // Observe monitoring status
        sensorService.isMonitoring.observe(viewLifecycleOwner, Observer { isMonitoring ->
            updateMonitoringStatus()
        })

        // Observe terminal output
        terminalViewModel.output.observe(viewLifecycleOwner, Observer { lines ->
            // Оновити TextView
            binding.terminalTextView.text = lines.joinToString("\n")

            // Автоскрол вниз
            binding.terminalScrollView.post {
                binding.terminalScrollView.fullScroll(View.FOCUS_DOWN)
            }
        })
    }

    /**
     * Оновити відображення sensor data
     */
    private fun updateSensorDisplay(data: MultiSensorService.AllSensorReading) {
        // Accelerometer
        binding.tvAccelX.text = String.format("%.2f", data.accelX)
        binding.tvAccelY.text = String.format("%.2f", data.accelY)
        binding.tvAccelZ.text = String.format("%.2f", data.accelZ)
        binding.tvAccelMagnitude.text = String.format("%.2f m/s²", data.accelMagnitude)

        // Activity
        binding.tvActivity.text = data.activityType

        // Gyroscope
        if (data.gyroX != null) {
            binding.tvGyroX.text = String.format("%.2f", data.gyroX)
            binding.tvGyroY.text = String.format("%.2f", data.gyroY)
            binding.tvGyroZ.text = String.format("%.2f", data.gyroZ)
        } else {
            binding.tvGyroX.text = "N/A"
            binding.tvGyroY.text = "N/A"
            binding.tvGyroZ.text = "N/A"
        }

        // Magnetometer
        if (data.magX != null) {
            binding.tvMagX.text = String.format("%.2f", data.magX)
            binding.tvMagY.text = String.format("%.2f", data.magY)
            binding.tvMagZ.text = String.format("%.2f", data.magZ)
        } else {
            binding.tvMagX.text = "N/A"
            binding.tvMagY.text = "N/A"
            binding.tvMagZ.text = "N/A"
        }

        // Environment
        binding.tvLight.text = data.lightLevel?.let { String.format("%.1f lux", it) } ?: "N/A"
        binding.tvProximity.text = data.proximityDistance?.let { String.format("%.1f cm", it) } ?: "N/A"
        binding.tvPressure.text = data.pressure?.let { String.format("%.1f hPa", it) } ?: "N/A"
        binding.tvTemperature.text = data.temperature?.let { String.format("%.1f °C", it) } ?: "N/A"
        binding.tvHumidity.text = data.humidity?.let { String.format("%.1f %%", it) } ?: "N/A"

        // Biometric
        binding.tvHeartRate.text = data.heartRate?.let { String.format("%.0f BPM", it) } ?: "N/A"
        binding.tvSteps.text = data.stepsSinceStart?.toString() ?: "N/A"

        // Gravity
        if (data.gravityX != null) {
            binding.tvGravityX.text = String.format("%.2f", data.gravityX)
            binding.tvGravityY.text = String.format("%.2f", data.gravityY)
            binding.tvGravityZ.text = String.format("%.2f", data.gravityZ)
        } else {
            binding.tvGravityX.text = "N/A"
            binding.tvGravityY.text = "N/A"
            binding.tvGravityZ.text = "N/A"
        }

        // Rotation
        if (data.rotationX != null) {
            binding.tvRotationX.text = String.format("%.2f", data.rotationX)
            binding.tvRotationY.text = String.format("%.2f", data.rotationY)
            binding.tvRotationZ.text = String.format("%.2f", data.rotationZ)
        } else {
            binding.tvRotationX.text = "N/A"
            binding.tvRotationY.text = "N/A"
            binding.tvRotationZ.text = "N/A"
        }

        // Metadata
        binding.tvDataPoints.text = data.dataPoints.toString()
        binding.tvEnabledSensors.text = data.enabledSensorsCount.toString()
    }

    /**
     * Оновити статус моніторингу
     */
    private fun updateMonitoringStatus() {
        val isMonitoring = sensorService.isMonitoring.value ?: false

        if (isMonitoring) {
            // Моніторинг активний
            binding.tvMonitoringStatus.text = "🟢 ACTIVE"
            binding.tvMonitoringStatus.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
            )

            binding.btnStartMonitoring.isEnabled = false
            binding.btnStopMonitoring.isEnabled = true

        } else {
            // Моніторинг неактивний
            binding.tvMonitoringStatus.text = "🔴 INACTIVE"
            binding.tvMonitoringStatus.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            )

            binding.btnStartMonitoring.isEnabled = true
            binding.btnStopMonitoring.isEnabled = false
        }
    }

    /**
     * Показати статистику збору даних
     */
    private fun showCollectionStatistics() {
        val stats = sensorService.getCollectionStats()

        terminalViewModel.clear()
        terminalViewModel.addOutput("════════════════════════════════════════")
        terminalViewModel.addOutput("  📊 COLLECTION STATISTICS")
        terminalViewModel.addOutput("════════════════════════════════════════")
        terminalViewModel.addOutput("")
        terminalViewModel.addOutput("Status:")
        terminalViewModel.addOutput("  Monitoring: ${if (stats.isMonitoring) "✅ ACTIVE" else "❌ INACTIVE"}")
        terminalViewModel.addOutput("  Enabled sensors: ${stats.enabledSensorsCount}")
        terminalViewModel.addOutput("  Current activity: ${stats.currentActivity}")
        terminalViewModel.addOutput("")
        terminalViewModel.addOutput("Collection:")
        terminalViewModel.addOutput("  Total records: ${stats.dataPointsCollected}")

        val minutes = stats.runningTimeMs / 60000
        val seconds = (stats.runningTimeMs % 60000) / 1000
        terminalViewModel.addOutput("  Running time: ${minutes}m ${seconds}s")
        terminalViewModel.addOutput("  Interval: ${stats.aggregationIntervalMs}ms")

        if (stats.runningTimeMs > 0) {
            val recordsPerMin = (stats.dataPointsCollected.toFloat() / stats.runningTimeMs) * 60000
            terminalViewModel.addOutput("  Rate: ${String.format("%.1f", recordsPerMin)} records/min")
        }

        terminalViewModel.addOutput("")
        terminalViewModel.addOutput("════════════════════════════════════════")
    }

    /**
     * Показати статистику буфера
     */
    private fun showBufferStatistics() {
        val stats = sensorService.getBufferStats()

        terminalViewModel.clear()
        terminalViewModel.addOutput("════════════════════════════════════════")
        terminalViewModel.addOutput("  🔄 BUFFER STATISTICS")
        terminalViewModel.addOutput("════════════════════════════════════════")
        terminalViewModel.addOutput("")
        terminalViewModel.addOutput("Capacity:")
        terminalViewModel.addOutput("  Max size: ${stats.capacity} records")
        terminalViewModel.addOutput("  Current size: ${stats.currentSize} records")
        terminalViewModel.addOutput("  Fill: ${String.format("%.1f", stats.fillPercentage)}%")
        terminalViewModel.addOutput("  Status: ${if (stats.isFull) "🔴 FULL" else "🟢 AVAILABLE"}")
        terminalViewModel.addOutput("")
        terminalViewModel.addOutput("Time span:")
        terminalViewModel.addOutput("  Oldest: ${stats.oldestTimestamp}")
        terminalViewModel.addOutput("  Newest: ${stats.newestTimestamp}")
        terminalViewModel.addOutput("  Span: ${String.format("%.1f", stats.timeSpanSeconds)}s")
        terminalViewModel.addOutput("")
        terminalViewModel.addOutput("ML Inference:")
        terminalViewModel.addOutput("  Ready: ${if (sensorService.isBufferReadyForInference()) "✅ YES" else "⏳ NO"}")
        terminalViewModel.addOutput("  Required: 5 records minimum")
        terminalViewModel.addOutput("")
        terminalViewModel.addOutput("════════════════════════════════════════")
    }

    /**
     * Перевірити необхідні permissions
     */
    private fun checkPermissions() {
        // Activity Recognition для Step Counter
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            terminalViewModel.addOutput("⚠️  Activity Recognition permission not granted")
            terminalViewModel.addOutput("   (Required for Step Counter)")
        }
    }

    /**
     * Запросити Activity Recognition permission
     */
    private fun requestActivityRecognitionPermission() {
        activityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}