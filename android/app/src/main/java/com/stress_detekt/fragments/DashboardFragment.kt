package com.stress_detekt.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.stress_detekt.activities.LoginActivity
import com.stress_detekt.activities.MainActivity
import com.stress_detekt.databinding.FragmentDashboardBinding
import com.stress_detekt.services.MultiSensorService
import com.stress_detekt.utils.PrefsManager
import com.stress_detekt.utils.SensorPreferences

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefsManager: PrefsManager
    private lateinit var sensorPrefs: SensorPreferences

    // Отримуємо сервіс з MainActivity
    private val sensorService: MultiSensorService
        get() = (requireActivity() as MainActivity).sensorService

    private var isMonitoring = false

    companion object {
        private const val KEY_IS_MONITORING = "isMonitoring"
        private const val KEY_USER_ID = "userId"
        private const val REQUEST_ACTIVITY_RECOGNITION = 100
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

        prefsManager = PrefsManager(requireContext())
        sensorPrefs = SensorPreferences(requireContext())

        setupUI()
        setupListeners()
        observeSensorData()

        restoreStateIfNeeded(savedInstanceState)
    }

    private fun setupUI() {
        val userName = prefsManager.getUserName() ?: "Користувач"
        binding.tvWelcome.text = "Вітаємо, $userName!"

        val enabledCount = sensorPrefs.getEnabledCount()
        binding.tvInfo.text = "Увімкнено датчиків: $enabledCount\n\n" +
                "Натисніть 'Почати' для збору даних\n" +
                "або перейдіть в Settings для налаштувань"

        // Відновлюємо стан UI з сервісу
        isMonitoring = sensorService.isMonitoring.value ?: false
        updateMonitoringButton()
    }

    private fun setupListeners() {
        binding.btnMonitoring.setOnClickListener {
            if (isMonitoring) {
                stopMonitoring()
            } else {
                startMonitoring()
            }
        }

        binding.btnLogout.setOnClickListener {
            if (isMonitoring) {
                stopMonitoring()
            }
            showLogoutDialog()
        }

        binding.btnExit.setOnClickListener {
            showExitDialog()
        }
    }

    private fun observeSensorData() {
        sensorService.sensorDataLive.observe(viewLifecycleOwner) { reading ->
            if (!isMonitoring) return@observe

            binding.tvInfo.text = buildString {
                append("📊 Real-time дані:\n")
                append("Увімкнено: ${reading.enabledSensorsCount} датчиків\n\n")

                if (sensorPrefs.isAccelerometerEnabled() && reading.accelMagnitude > 0) {
                    append("Accelerometer:\n")
                    append("  X: ${String.format("%.2f", reading.accelX)} m/s²\n")
                    append("  Y: ${String.format("%.2f", reading.accelY)} m/s²\n")
                    append("  Z: ${String.format("%.2f", reading.accelZ)} m/s²\n")
                    append("  Magnitude: ${String.format("%.2f", reading.accelMagnitude)} m/s²\n")
                    append("  Activity: ${reading.activityType}\n\n")
                }

                if (sensorPrefs.isGyroscopeEnabled() && reading.gyroX != null) {
                    append("Gyroscope:\n")
                    append("  X: ${String.format("%.2f", reading.gyroX)} rad/s\n")
                    append("  Y: ${String.format("%.2f", reading.gyroY)} rad/s\n")
                    append("  Z: ${String.format("%.2f", reading.gyroZ)} rad/s\n\n")
                }

                if (sensorPrefs.isMagnetometerEnabled() && reading.magX != null) {
                    append("Magnetometer:\n")
                    append("  X: ${String.format("%.1f", reading.magX)} µT\n")
                    append("  Y: ${String.format("%.1f", reading.magY)} µT\n")
                    append("  Z: ${String.format("%.1f", reading.magZ)} µT\n\n")
                }

                if (sensorPrefs.isLightEnabled() && reading.lightLevel != null) {
                    append("Light: ${String.format("%.1f", reading.lightLevel)} lux\n")
                }

                if (sensorPrefs.isProximityEnabled() && reading.proximityDistance != null) {
                    append("Proximity: ${String.format("%.1f", reading.proximityDistance)} cm\n")
                }

                if (sensorPrefs.isPressureEnabled() && reading.pressure != null) {
                    append("Pressure: ${String.format("%.1f", reading.pressure)} hPa\n")
                }

                if (sensorPrefs.isTemperatureEnabled() && reading.temperature != null) {
                    append("Temperature: ${String.format("%.1f", reading.temperature)}°C\n")
                }

                if (sensorPrefs.isHumidityEnabled() && reading.humidity != null) {
                    append("Humidity: ${String.format("%.1f", reading.humidity)}%\n")
                }

                if (sensorPrefs.isGravityEnabled() && reading.gravityX != null) {
                    append("Gravity:\n")
                    append("  X: ${String.format("%.2f", reading.gravityX)} m/s²\n")
                    append("  Y: ${String.format("%.2f", reading.gravityY)} m/s²\n")
                    append("  Z: ${String.format("%.2f", reading.gravityZ)} m/s²\n\n")
                }

                if (sensorPrefs.isRotationEnabled() && reading.rotationX != null) {
                    append("Rotation:\n")
                    append("  X: ${String.format("%.3f", reading.rotationX)}\n")
                    append("  Y: ${String.format("%.3f", reading.rotationY)}\n")
                    append("  Z: ${String.format("%.3f", reading.rotationZ)}\n\n")
                }

                if (sensorPrefs.isStepCounterEnabled() && reading.stepsSinceStart != null) {
                    append("Steps (session): ${reading.stepsSinceStart}\n")
                    append("Steps (total): ${reading.stepCount}\n")
                }

                append("\nData points: ${reading.dataPoints}")
            }
        }

        sensorService.isMonitoring.observe(viewLifecycleOwner) { monitoring ->
            isMonitoring = monitoring
            updateMonitoringButton()

            if (!monitoring && _binding != null) {
                val enabledCount = sensorPrefs.getEnabledCount()
                binding.tvInfo.text = "Увімкнено датчиків: $enabledCount\n\n" +
                        "Натисніть 'Почати' для збору даних"
            }
        }
    }

    private fun restoreStateIfNeeded(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            val wasMonitoring = bundle.getBoolean(KEY_IS_MONITORING, false)
            val userId = bundle.getLong(KEY_USER_ID, -1L)

            if (wasMonitoring && userId != -1L && !isMonitoring) {
                sensorService.startMonitoring(userId)
                binding.tvInfo.text = "Відновлення моніторингу після повороту екрану...\n\n" +
                        "Дані збираються ✓"
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_MONITORING, isMonitoring)
        outState.putLong(KEY_USER_ID, prefsManager.getUserId())
    }

    private fun updateMonitoringButton() {
        if (isMonitoring) {
            binding.btnMonitoring.text = "⏹ ЗУПИНИТИ ВИМІРЮВАННЯ"
            binding.tvStatus.text = "Статус: Збір даних активний"
            binding.tvStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        } else {
            binding.btnMonitoring.text = "▶ ПОЧАТИ ВИМІРЮВАННЯ"
            binding.tvStatus.text = "Статус: Неактивно"
            binding.tvStatus.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }

    private fun startMonitoring() {
        val userId = prefsManager.getUserId()
        if (userId == -1L) {
            binding.tvInfo.text = "Помилка: користувач не знайдений"
            return
        }

        val enabledCount = sensorPrefs.getEnabledCount()
        if (enabledCount == 0) {
            AlertDialog.Builder(requireContext())
                .setTitle("Немає увімкнених датчиків")
                .setMessage("Увімкніть хоча б один датчик в Settings")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        // Перевірка дозволу для Step Counter
        if (sensorPrefs.isStepCounterEnabled()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                if (requireContext().checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    // Запитуємо дозвіл
                    requestPermissions(
                        arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                        REQUEST_ACTIVITY_RECOGNITION
                    )
                    return
                }
            }
        }

        sensorService.startMonitoring(userId)
        println("✓ DashboardFragment: startMonitoring() called")
    }



    private fun stopMonitoring() {
        println("✓ DashboardFragment: stopMonitoring() called")

        sensorService.stopMonitoring()

        val userId = prefsManager.getUserId()
        sensorService.getAverageActivity(userId) { activity ->
            requireActivity().runOnUiThread {
                if (_binding != null) {
                    binding.tvInfo.text = "Вимірювання зупинено.\n\n" +
                            "Середня активність за сесію:\n$activity\n\n" +
                            "Дані збережені в БД ✓"
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Дозвіл надано, запускаємо моніторинг
                val userId = prefsManager.getUserId()
                sensorService.startMonitoring(userId)
                println("✓ ACTIVITY_RECOGNITION permission granted")
            } else {
                // Дозвіл відхилено
                AlertDialog.Builder(requireContext())
                    .setTitle("Дозвіл відхилено")
                    .setMessage("Для роботи Step Counter потрібен дозвіл ACTIVITY_RECOGNITION.\n\nStep Counter буде вимкнено.")
                    .setPositiveButton("OK") { _, _ ->
                        // Вимикаємо Step Counter
                        sensorPrefs.setStepCounterEnabled(false)
                    }
                    .show()
                println("✗ ACTIVITY_RECOGNITION permission denied")
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Вихід")
            .setMessage("Ви впевнені що хочете вийти?")
            .setPositiveButton("Так") { _, _ -> performLogout() }
            .setNegativeButton("Ні", null)
            .show()
    }

    private fun performLogout() {
        if (isMonitoring) {
            stopMonitoring()
        }
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
                if (isMonitoring) {
                    sensorService.stopMonitoring()
                }
                requireActivity().finishAffinity()
            }
            .setNegativeButton("Ні", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // НЕ зупиняємо сервіс - він живе в MainActivity
        _binding = null
    }
}