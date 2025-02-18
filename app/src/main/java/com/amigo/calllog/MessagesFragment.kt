package com.amigo.calllog

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.ChipGroup

class MessagesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var chipGroup: ChipGroup
    private var currentFilter = MessageFilter.ALL
    private var allMessages = listOf<MessageItem>()

    companion object {
        fun newInstance() = MessagesFragment()
        private const val PERMISSION_REQUEST_CODE = 123
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS
        )
    }

    enum class MessageFilter {
        ALL,
        RECEIVED,
        SENT
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyView = view.findViewById(R.id.emptyView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        chipGroup = view.findViewById(R.id.chipGroup)

        setupRecyclerView()
        setupSwipeRefresh()
        setupChipGroup()
        checkPermissionsAndLoadMessages()
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            loadMessages()
        }
    }

    private fun setupChipGroup() {
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            currentFilter = when (checkedId) {
                R.id.chipReceived -> MessageFilter.RECEIVED
                R.id.chipSent -> MessageFilter.SENT
                else -> MessageFilter.ALL
            }
            filterMessages()
        }
    }

    private fun filterMessages() {
        val filteredMessages = when (currentFilter) {
            MessageFilter.ALL -> allMessages
            MessageFilter.RECEIVED -> allMessages.filter { 
                it.type == Telephony.Sms.MESSAGE_TYPE_INBOX 
            }
            MessageFilter.SENT -> allMessages.filter { 
                it.type == Telephony.Sms.MESSAGE_TYPE_SENT 
            }
        }

        adapter = MessageAdapter(filteredMessages)
        recyclerView.adapter = adapter

        // Update empty view visibility
        if (filteredMessages.isEmpty()) {
            emptyView.text = "No ${currentFilter.name.lowercase()} messages found"
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun checkPermissionsAndLoadMessages() {
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
            } else {
                loadMessages()
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
                    showMessage("Permissions required for messages feature")
                } else {
                    loadMessages()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun loadMessages() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        try {
            allMessages = getMessages()
            filterMessages()
        } catch (e: Exception) {
            showMessage("Error loading messages: ${e.message}")
        } finally {
            progressBar.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getMessages(): List<MessageItem> {
        val messages = mutableListOf<MessageItem>()
        context?.contentResolver?.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE
            ),
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)
            val typeIndex = cursor.getColumnIndex(Telephony.Sms.TYPE)

            while (cursor.moveToNext()) {
                val address = cursor.getString(addressIndex)
                val body = cursor.getString(bodyIndex)
                val date = cursor.getLong(dateIndex)
                val type = cursor.getInt(typeIndex)
                val contactName = getContactName(address)

                messages.add(MessageItem(address, contactName, body, date, type))
            }
        }
        return messages
    }

    private fun getContactName(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return null
        
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        
        context?.contentResolver?.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        
        return null
    }

    private fun showMessage(message: String) {
        view?.let {
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
