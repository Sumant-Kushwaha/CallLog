// CallLogAdapter.kt
package com.amigo.calllog

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.amigo.calllog.databinding.ItemCallLogBinding
import com.amigo.calllog.databinding.ItemHeaderBinding

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
                ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> CallViewHolder(
                ItemCallLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.HeaderItem -> {
                (holder as HeaderViewHolder).bind(item.title)
            }
            is ListItem.CallItem -> {
                (holder as CallViewHolder).bind(item.call)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(private val binding: ItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvHeader.text = title
        }
    }

    inner class CallViewHolder(private val binding: ItemCallLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(call: CallLogItem) {
            // Always display name with count in braces
            val displayName = "${call.name ?: call.number} (${call.count})"
            binding.tvName.text = displayName
            binding.tvNumber.text = call.number

            binding.btnCall.setOnClickListener {
                val context = it.context
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:${call.number}")
                    }
                    context.startActivity(intent)
                } else {
                    ActivityCompat.requestPermissions(context as Activity, arrayOf(android.Manifest.permission.CALL_PHONE), 1)
                }
            }

            // Set call type icon
            binding.ivContactIcon.setImageResource(
                when (call.type) {
                    CallLogItem.MISSED_CALL_TYPE -> R.drawable.ic_contact_default
                    CallLogItem.RECEIVED_CALL_TYPE -> R.drawable.ic_contact_default
                    CallLogItem.DIALED_CALL_TYPE -> R.drawable.ic_contact_default
                    else -> R.drawable.ic_contact_default
                }
            )
        }
    }
}