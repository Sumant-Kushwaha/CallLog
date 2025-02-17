package com.amigo.calllog

import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

data class MessageItem(
    val address: String,
    val contactName: String?,
    val body: String,
    val date: Long,
    val type: Int
)

class MessageAdapter(private val messages: List<MessageItem>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        private val tvBody: TextView = view.findViewById(R.id.tvBody)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvType: TextView = view.findViewById(R.id.tvType)

        fun bind(message: MessageItem) {
            // Display contact name if available, otherwise show the phone number
            tvAddress.text = message.contactName ?: message.address
            tvBody.text = message.body
            
            // Format date
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            tvDate.text = sdf.format(Date(message.date))
            
            // Set message type
            val type = when (message.type) {
                Telephony.Sms.MESSAGE_TYPE_INBOX -> "Received"
                Telephony.Sms.MESSAGE_TYPE_SENT -> "Sent"
                else -> "Unknown"
            }
            tvType.text = type
        }
    }
}
