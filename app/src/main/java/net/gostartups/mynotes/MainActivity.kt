package net.gostartups.mynotes

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), SensorEventListener {

    // create a constant value for the key under which the user's note will be persisted
    private val sharedPreferencesKey = "MY_NOTES"

    // create variable for the reference to the notes text field
    private var etNoteText: EditText? = null

    // create variable for the reference to the button that will save the user's note
    private var btnSaveNote: Button? = null

    // create variable for the reference to the button that will restore the last saved note
    private var btnShowSavedNote: Button? = null

    // create variable that keeps track of last text state before the user edits it
    private var lastNoteText: String? = ""

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null

    private val gravityForce = 9.8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get a reference to the text field for the note's text
        etNoteText = findViewById(R.id.et_note_text)
        // Get a reference to the button that will save the user's note
        btnSaveNote = findViewById(R.id.btn_save_note)
        // Get a reference to the button that will restore the last saved note
        btnShowSavedNote = findViewById(R.id.btn_show_saved_note)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Create a SharedPreferences class instance to save/restore the user's note
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        // check if there is any saved note
        val hasSavedNote = sharedPreferences.contains(sharedPreferencesKey)
        if (hasSavedNote) {
            // activate the button to restore last note if there is such
            activateShowSavedNoteButton()
        } else {
            // deactivate the button to restore last note if non is present
            deactivateShowSavedNoteButton()
        }
        // initially deactivate the save note button as note text field will always start empty
        deactivateSaveNoteButton()

        // Set an action that will be executed upon user's tap on the button
        btnSaveNote?.setOnClickListener {
            // Extract the text from the note's text field into a separate variable to operate with
            val noteText = etNoteText?.text?.toString()
            // Get an Editor that will be used to persist the note's text in the SharedPreferences instance
            val sharedPreferencesEditor = sharedPreferences.edit()
            // Persist the user's note in the SharedPreferences instance
            sharedPreferencesEditor.putString(sharedPreferencesKey, noteText)
            // Commit all changes to the SharedPreferences instance
            sharedPreferencesEditor.apply()
            // update the note text state
            lastNoteText = noteText
            // deactivate save note button after operation has finished
            deactivateSaveNoteButton()
            // activate show saved note button
            activateShowSavedNoteButton()
            // Notify the user that the note's text has been saved
            Snackbar.make(
                findViewById(R.id.main_layout),
                "Note successfully saved!",
                Snackbar.LENGTH_LONG
            ).show()
        }

        btnShowSavedNote?.setOnClickListener {
            // get the last saved note text or empty string if no text was saved previously
            val savedNoteText = sharedPreferences.getString(sharedPreferencesKey, "")
            // check if it's empty or missing
            if (!savedNoteText.isNullOrEmpty()) {
                // update the note text state
                lastNoteText = savedNoteText
                // set the saved note text in the text field
                etNoteText?.setText(savedNoteText)
            }
        }

        etNoteText?.addTextChangedListener {
            // Extract the text from the note's text field into a separate variable to operate with
            val newText = it?.toString()
            // check if it's empty or missing or same as initial text
            if (newText.isNullOrEmpty() || newText.contentEquals(lastNoteText)) {
                // deactivate save note button if note text is empty
                deactivateSaveNoteButton()
            } else {
                // activate save note button if note text is not empty or has been edited so the user can save iт
                activateSaveNoteButton()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        // unregister Sensor listener
        sensorManager?.unregisterListener(this)
    }

    private fun deactivateShowSavedNoteButton() {
        // make it semi-transparent
        btnShowSavedNote?.alpha = 0.3f
        // make it non-clickable
        btnShowSavedNote?.isEnabled = false
    }

    private fun activateShowSavedNoteButton() {
        // make it opaque
        btnShowSavedNote?.alpha = 1.0f
        // make it clickable
        btnShowSavedNote?.isEnabled = true
    }

    private fun deactivateSaveNoteButton() {
        // make it semi-transparent
        btnSaveNote?.alpha = 0.3f
        // make it non-clickable
        btnSaveNote?.isEnabled = false
    }

    private fun activateSaveNoteButton() {
        // make it opaque
        btnSaveNote?.alpha = 1.0f
        // make it clickable
        btnSaveNote?.isEnabled = true
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // get values for acceleration on Y-axis
        val y = event!!.values[1]
        Log.d("ACCELEROMETER_SENSOR_DATA", "Y: $y")

        // tilting the device forward reduces the magnitude of the gravity force
        // applied to the accelerometer sensor on the Y-axis so we clear the text
        // when the gravity force has been reduced in half due to tilting forward
        if (y < (gravityForce / 2)) {
            etNoteText?.setText("")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}