// CallLogAdapter.kt
package com.amigo.calllog

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CallLogAdapter(private val items: List<ListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    sealed class ListItem {
        data class HeaderItem(val title: String) : ListItem()
        data class CallItem(val call: CallLogItem) : ListItem()
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.HeaderItem -> TYPE_HEADER
            is ListItem.CallItem -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header, parent, false)
            )
            else -> CallViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_call_log, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.HeaderItem -> (holder as HeaderViewHolder).bind(item.title)
            is ListItem.CallItem -> (holder as CallViewHolder).bind(item.call)
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvHeader)
        fun bind(title: String) {
            tvTitle.text = title
        }
    }

    class CallViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val tvNumber: TextView = view.findViewById(R.id.tvNumber)
        private val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        private val btnCall: ImageButton = view.findViewById(R.id.btnCall)

        fun bind(call: CallLogItem) {
            tvName.text = call.name ?: "Unknown"
            tvNumber.text = call.number
            tvDuration.text = "${call.duration} sec"

            btnCall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${call.number}")
                }
                it.context.startActivity(intent)
            }
        }
    }
}