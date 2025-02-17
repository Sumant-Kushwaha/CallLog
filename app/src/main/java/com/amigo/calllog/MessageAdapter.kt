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
    val type: Int,
    var isExpanded: Boolean = false
)

class MessageAdapter(private var messages: List<MessageItem>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view) { position ->
            // Toggle expansion
            messages = messages.mapIndexed { index, item ->
                if (index == position) {
                    item.copy(isExpanded = !item.isExpanded)
                } else {
                    item.copy(isExpanded = false) // Collapse other items
                }
            }
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(
        view: View,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        private val tvBody: TextView = view.findViewById(R.id.tvBody)
        private val tvPreview: TextView = view.findViewById(R.id.tvPreview)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvType: TextView = view.findViewById(R.id.tvType)

        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }

        fun bind(message: MessageItem) {
            // Display contact name if available, otherwise show the phone number
            tvAddress.text = message.contactName ?: message.address
            
            // Set preview (always visible) and full message (expandable)
            tvPreview.text = message.body
            tvBody.text = message.body
            tvBody.visibility = if (message.isExpanded) View.VISIBLE else View.GONE
            tvPreview.visibility = if (message.isExpanded) View.GONE else View.VISIBLE
            
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
