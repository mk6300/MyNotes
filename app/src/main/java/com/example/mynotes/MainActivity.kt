package com.example.mynotes

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.example.mynotes.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private val sharedPreferencesKey = "KEY_SHARED_PREFS"

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        binding.saveButton.isEnabled = false
        if (noNoteSaved()) {
            binding.loadButton.visibility = View.INVISIBLE
            binding.loadButton.isEnabled = false
        } else {
            binding.loadButton.visibility = View.VISIBLE
        }

        binding.editText.addTextChangedListener {
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            val savedText = sharedPref.getString(sharedPreferencesKey, "")
            val note = binding.editText.text.toString()
            if (note.isNotEmpty() && note != savedText) {
                binding.saveButton.isEnabled = true
            }
        }
        binding.saveButton.setOnClickListener {
            val textToSave = binding.editText.text.toString()
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(sharedPreferencesKey, textToSave)
            val dataSavedOk = editor.commit()
            if (dataSavedOk) {
                Snackbar.make(binding.root, "Note Saved Successfully", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(binding.root, "Data No Saved", Snackbar.LENGTH_LONG).show()
            }
            binding.editText.setText("")
            binding.saveButton.isEnabled = false
            binding.loadButton.visibility = View.VISIBLE
            binding.loadButton.isEnabled = true
        }

        binding.loadButton.setOnClickListener {
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            val savedText = sharedPref.getString(sharedPreferencesKey, "")
            binding.editText.setText(savedText)
        }
    }

    private fun noNoteSaved(): Boolean {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val savedNote = sharedPreferences.getString(sharedPreferencesKey, "")
        return !savedNote.isNullOrEmpty()
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val yReading = event?.values?.get(1) ?: Float.MAX_VALUE
        if (yReading < 6.7) {
            binding.savedNoteText.text = ""
            binding.editText.setText("")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}

