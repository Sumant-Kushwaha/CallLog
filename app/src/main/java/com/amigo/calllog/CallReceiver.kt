package com.amigo.calllog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import java.text.SimpleDateFormat
import java.util.*

class CallReceiver : BroadcastReceiver() {
    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private var incomingNumber: String? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                lastState = TelephonyManager.CALL_STATE_RINGING
                incomingNumber = number
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Check if this was a missed call (transition from RINGING to IDLE)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    handleMissedCall(context, incomingNumber)
                }
                lastState = TelephonyManager.CALL_STATE_IDLE
                incomingNumber = null
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                lastState = TelephonyManager.CALL_STATE_OFFHOOK
            }
        }
    }

    private fun handleMissedCall(context: Context, phoneNumber: String?) {
        if (phoneNumber == null) return

        // Check if auto-reply is enabled
        val prefs = context.getSharedPreferences("auto_reply_settings", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(AutoReplySettingsFragment.PREF_AUTO_REPLY_ENABLED, false)
        if (!isEnabled) return

        // Get the message template and replace placeholders
        val messageTemplate = prefs.getString(
            AutoReplySettingsFragment.PREF_AUTO_REPLY_MESSAGE,
            "Sorry, I can't take your call right now. I'll get back to you soon."
        ) ?: return

        val contactName = getContactName(context, phoneNumber)
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val message = messageTemplate
            .replace("{name}", contactName ?: "there")
            .replace("{time}", currentTime)

        // Send the SMS
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getContactName(context: Context, phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        
        return null
    }
}
