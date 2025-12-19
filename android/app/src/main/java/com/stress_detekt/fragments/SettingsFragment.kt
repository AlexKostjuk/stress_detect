package com.stress_detekt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.stress_detekt.databinding.FragmentSettingsBinding
import com.stress_detekt.services.SensorCapabilities
import com.stress_detekt.utils.SensorPreferences

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorPrefs: SensorPreferences
    private lateinit var sensorCapabilities: SensorCapabilities

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorPrefs = SensorPreferences(requireContext())
        sensorCapabilities = SensorCapabilities(requireContext())

        loadSensorStatus()
        setupListeners()
        updateEnabledCount()
    }

    private fun loadSensorStatus() {
        val allStatus = sensorCapabilities.getAllSensorsStatus()

        // Accelerometer
        setupSensorSwitch(
            switch = binding.switchAccelerometer,
            info = allStatus["accelerometer"]!!,
            statusText = binding.tvAccelStatus,
            detailsText = binding.tvAccelDetails,
            isEnabled = sensorPrefs.isAccelerometerEnabled()
        )

        // Gyroscope
        setupSensorSwitch(
            switch = binding.switchGyroscope,
            info = allStatus["gyroscope"]!!,
            statusText = binding.tvGyroStatus,
            detailsText = binding.tvGyroDetails,
            isEnabled = sensorPrefs.isGyroscopeEnabled()
        )

        // Magnetometer
        setupSensorSwitch(
            switch = binding.switchMagnetometer,
            info = allStatus["magnetometer"]!!,
            statusText = binding.tvMagStatus,
            detailsText = binding.tvMagDetails,
            isEnabled = sensorPrefs.isMagnetometerEnabled()
        )

        // Light
        setupSensorSwitch(
            switch = binding.switchLight,
            info = allStatus["light"]!!,
            statusText = binding.tvLightStatus,
            detailsText = binding.tvLightDetails,
            isEnabled = sensorPrefs.isLightEnabled()
        )

        // Proximity
        setupSensorSwitch(
            switch = binding.switchProximity,
            info = allStatus["proximity"]!!,
            statusText = binding.tvProxStatus,
            detailsText = binding.tvProxDetails,
            isEnabled = sensorPrefs.isProximityEnabled()
        )

        // Pressure
        setupSensorSwitch(
            switch = binding.switchPressure,
            info = allStatus["pressure"]!!,
            statusText = binding.tvPressureStatus,
            detailsText = binding.tvPressureDetails,
            isEnabled = sensorPrefs.isPressureEnabled()
        )

        // Temperature
        setupSensorSwitch(
            switch = binding.switchTemperature,
            info = allStatus["temperature"]!!,
            statusText = binding.tvTempStatus,
            detailsText = binding.tvTempDetails,
            isEnabled = sensorPrefs.isTemperatureEnabled()
        )

        // Humidity
        setupSensorSwitch(
            switch = binding.switchHumidity,
            info = allStatus["humidity"]!!,
            statusText = binding.tvHumidityStatus,
            detailsText = binding.tvHumidityDetails,
            isEnabled = sensorPrefs.isHumidityEnabled()
        )

        // Heart Rate
        setupSensorSwitch(
            switch = binding.switchHeartRate,
            info = allStatus["heartRate"]!!,
            statusText = binding.tvHrStatus,
            detailsText = binding.tvHrDetails,
            isEnabled = sensorPrefs.isHeartRateEnabled()
        )

        // Step Counter
        setupSensorSwitch(
            switch = binding.switchStepCounter,
            info = allStatus["stepCounter"]!!,
            statusText = binding.tvStepStatus,
            detailsText = binding.tvStepDetails,
            isEnabled = sensorPrefs.isStepCounterEnabled()
        )

        // Gravity
        setupSensorSwitch(
            switch = binding.switchGravity,
            info = allStatus["gravity"]!!,
            statusText = binding.tvGravityStatus,
            detailsText = binding.tvGravityDetails,
            isEnabled = sensorPrefs.isGravityEnabled()
        )

        // Rotation Vector
        setupSensorSwitch(
            switch = binding.switchRotation,
            info = allStatus["rotation"]!!,
            statusText = binding.tvRotationStatus,
            detailsText = binding.tvRotationDetails,
            isEnabled = sensorPrefs.isRotationEnabled()
        )
    }

    private fun setupSensorSwitch(
        switch: androidx.appcompat.widget.SwitchCompat,
        info: com.stress_detekt.services.SensorInfo,
        statusText: android.widget.TextView,
        detailsText: android.widget.TextView,
        isEnabled: Boolean
    ) {
        // Налаштування статусу
        if (info.isAvailable) {
            statusText.text = "✅ Available"
            statusText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            detailsText.text = "${info.vendor}\nPower: ${String.format("%.2f", info.power)} mA"

            switch.isEnabled = true
            switch.isChecked = isEnabled
        } else {
            statusText.text = "❌ Not Available"
            statusText.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            detailsText.text = "Датчик відсутній на пристрої"

            switch.isEnabled = false
            switch.isChecked = false
        }
    }

    private fun setupListeners() {
        // Зберігання налаштувань
        binding.switchAccelerometer.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setAccelerometerEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchGyroscope.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setGyroscopeEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchMagnetometer.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setMagnetometerEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchLight.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setLightEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchProximity.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setProximityEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchPressure.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setPressureEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchTemperature.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setTemperatureEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchHumidity.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setHumidityEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchHeartRate.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setHeartRateEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchStepCounter.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setStepCounterEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchGravity.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setGravityEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        binding.switchRotation.setOnCheckedChangeListener { _, isChecked ->
            sensorPrefs.setRotationEnabled(isChecked)
            updateEnabledCount()
            showRestartWarning()
        }

        // Кнопка скидання
        binding.btnReset.setOnClickListener {
            showResetDialog()
        }
    }

    private fun updateEnabledCount() {
        val count = sensorPrefs.getEnabledCount()
        binding.tvEnabledCount.text = "Увімкнено датчиків: $count"
    }

    private fun showRestartWarning() {
        Toast.makeText(
            requireContext(),
            "Зупиніть та запустіть моніторинг заново",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showResetDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Скинути налаштування")
            .setMessage("Увімкнути всі доступні датчики?")
            .setPositiveButton("Так") { _, _ ->
                sensorPrefs.resetToDefaults()
                loadSensorStatus()
                updateEnabledCount()
                Toast.makeText(requireContext(), "Налаштування скинуті", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Ні", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}