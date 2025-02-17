package com.amigo.calllog

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class AutoReplySettingsFragment : Fragment() {
    private lateinit var switchAutoReply: SwitchMaterial
    private lateinit var editTextMessage: TextInputEditText
    private lateinit var buttonSave: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        fun newInstance() = AutoReplySettingsFragment()
        private const val PERMISSION_REQUEST_CODE = 456
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS
        )
        const val PREF_AUTO_REPLY_ENABLED = "auto_reply_enabled"
        const val PREF_AUTO_REPLY_MESSAGE = "auto_reply_message"
        private const val DEFAULT_MESSAGE = "Sorry, I can't take your call right now. I'll get back to you soon."
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auto_reply_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("auto_reply_settings", Context.MODE_PRIVATE)

        switchAutoReply = view.findViewById(R.id.switchAutoReply)
        editTextMessage = view.findViewById(R.id.editTextMessage)
        buttonSave = view.findViewById(R.id.buttonSave)

        // Load saved settings
        switchAutoReply.isChecked = sharedPreferences.getBoolean(PREF_AUTO_REPLY_ENABLED, false)
        editTextMessage.setText(sharedPreferences.getString(PREF_AUTO_REPLY_MESSAGE, DEFAULT_MESSAGE))
        editTextMessage.isEnabled = switchAutoReply.isChecked
        buttonSave.isEnabled = switchAutoReply.isChecked

        setupListeners()
        checkPermissions()
    }

    private fun setupListeners() {
        switchAutoReply.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkPermissions()
            }
            editTextMessage.isEnabled = isChecked
            buttonSave.isEnabled = isChecked
        }

        buttonSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun checkPermissions() {
        context?.let { ctx ->
            val missingPermissions = REQUIRED_PERMISSIONS.filter {
                ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
            }

            if (missingPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    missingPermissions.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (!grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    switchAutoReply.isChecked = false
                    showError("Permissions required for auto-reply feature")
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun saveSettings() {
        val message = editTextMessage.text?.toString()
        if (message.isNullOrBlank()) {
            editTextMessage.error = "Please enter a message"
            return
        }

        sharedPreferences.edit().apply {
            putBoolean(PREF_AUTO_REPLY_ENABLED, switchAutoReply.isChecked)
            putString(PREF_AUTO_REPLY_MESSAGE, message)
            apply()
        }

        showError("Settings saved")
    }

    private fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }
}
